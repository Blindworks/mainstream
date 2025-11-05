# Universal FIT File Processing System

## 📋 Overview

The **Universal FIT File Processing System** is a complete rewrite of the FIT file processing logic designed to address the critical flaws in the original implementation. This new system provides:

- ✅ **Zero Data Loss** - Captures ALL FIT message types, even unknown ones
- ✅ **Future-Proof** - Automatically handles new FIT message types
- ✅ **Developer Field Support** - Full support for ConnectIQ custom fields
- ✅ **Extensible Architecture** - Easy to add new message processors
- ✅ **Robust Error Handling** - Partial processing recovery
- ✅ **Full FIT Protocol Compliance** - Based on Garmin FIT SDK 21.176.0

---

## 🏗️ Architecture

### Component Overview

```
┌─────────────────────────────────────────────────────────┐
│            FitFileController                             │
│  REST endpoints for FIT file upload/management          │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│         UniversalFitFileServiceImpl                      │
│  • File validation & integrity check                     │
│  • Orchestrates message processing                       │
│  • Manages transactions                                  │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│         Message Processors (Pluggable)                   │
│  • FileIdMessageProcessor (Priority: 1000)               │
│  • SessionMessageProcessor (Priority: 900)               │
│  • LapMessageProcessor (Priority: 500)                   │
│  • RecordMessageProcessor (Priority: 100)                │
│  • GenericMessageProcessor (Priority: MIN - Fallback)   │
│  • [Future: DeviceInfo, Event, HRV, Zone processors]    │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│         Data Storage Layer                               │
│  • fit_file_uploads - Session summary                    │
│  • fit_messages - Generic JSONB storage                  │
│  • fit_unknown_messages - Zero-loss safety net           │
│  • fit_track_points - Time-series data                   │
│  • fit_lap_data - Lap summaries                          │
│  • [Other specialized tables]                            │
└─────────────────────────────────────────────────────────┘
```

---

## 🔧 Key Components

### 1. Message Processors

#### `MessageProcessor` Interface
All message processors implement this interface:

```java
public interface MessageProcessor {
    String getMessageType();           // e.g., "session", "record"
    Integer getGlobalMessageNumber();  // FIT global message number
    ProcessingResult process(Mesg mesg, FitFileUpload upload, int seq);
    boolean canProcess(Mesg mesg);
    int getPriority();                 // Higher = processed first
}
```

#### Available Processors

| Processor | Message Type | Global Msg # | Priority | Purpose |
|-----------|--------------|--------------|----------|---------|
| `FileIdMessageProcessor` | file_id | 0 | 1000 | File metadata (manufacturer, device, serial) |
| `SessionMessageProcessor` | session | 18 | 900 | Activity summary (distance, time, HR, etc.) |
| `LapMessageProcessor` | lap | 19 | 500 | Lap-level summaries |
| `RecordMessageProcessor` | record | 20 | 100 | GPS track points, time-series data |
| `GenericMessageProcessor` | * | * | MIN | Fallback for any message |

#### Adding New Processors

Create a new processor by extending `AbstractMessageProcessor`:

```java
@Component
public class DeviceInfoMessageProcessor extends AbstractMessageProcessor {

    @Override
    public String getMessageType() {
        return "device_info";
    }

    @Override
    public Integer getGlobalMessageNumber() {
        return 23; // device_info is message 23
    }

    @Override
    public ProcessingResult process(Mesg mesg, FitFileUpload upload, int seq) {
        // Extract fields
        Map<String, Object> fields = extractAllFields(mesg);

        // Add custom processing logic
        // ...

        return ProcessingResult.success(fields);
    }

    @Override
    public int getPriority() {
        return 800; // High priority
    }
}
```

The service will automatically discover and use the new processor via Spring's component scanning.

---

### 2. Data Storage

#### Generic Message Storage (`fit_messages`)

All FIT messages are stored in a flexible JSONB format:

```sql
CREATE TABLE fit_messages (
    id BIGINT PRIMARY KEY,
    fit_file_upload_id BIGINT,
    message_type VARCHAR(100),      -- "session", "record", etc.
    message_number INT,              -- Global FIT message number
    message_timestamp DATETIME,      -- Extracted timestamp
    message_data JSON,               -- All fields as JSON
    developer_fields JSON,           -- ConnectIQ custom fields
    sequence_number INT,             -- Order in file
    fully_parsed BOOLEAN,            -- Was it fully parsed?
    parsing_notes TEXT              -- Any warnings
);
```

**Example message_data:**
```json
{
    "timestamp": "2024-01-15T10:30:45",
    "position_lat": 40.7128,
    "position_long": -74.0060,
    "altitude": 10.5,
    "heart_rate": 145,
    "cadence": 180,
    "speed": 3.5,
    "distance": 5000.0
}
```

#### Unknown Message Safety Net (`fit_unknown_messages`)

Messages that couldn't be processed are stored for future reprocessing:

```sql
CREATE TABLE fit_unknown_messages (
    id BIGINT PRIMARY KEY,
    fit_file_upload_id BIGINT,
    global_message_number INT,       -- Identifies message type
    raw_data JSON,                   -- All available data
    completely_unknown BOOLEAN,      -- Truly unknown vs. not implemented
    unknown_reason TEXT,             -- Why couldn't we process it?
    reprocess_status ENUM(...)       -- For batch reprocessing
);
```

This ensures **zero data loss** - even if we don't know how to process a message today, we can reprocess it later when support is added.

---

## 🔍 Processing Flow

### 1. File Upload

```
User uploads .fit file
    ↓
FitFileController receives file
    ↓
UniversalFitFileServiceImpl.uploadFitFile()
    ↓
Calculate SHA-256 hash (duplicate detection)
    ↓
Create FitFileUpload entity (status: PENDING)
    ↓
Call processFitFile()
```

### 2. File Processing

```
Check file integrity (CRC validation)
    ↓
Decode FIT file with Garmin SDK
    ↓
For each message in file:
    ↓
    Find appropriate MessageProcessor
    ↓
    If processor found:
        Process message → Store in fit_messages
    ↓
    Else:
        Store in fit_unknown_messages
    ↓
Update FitFileUpload (status: COMPLETED)
    ↓
Log processing statistics
```

### 3. Data Access

Applications can access FIT data in multiple ways:

#### A. Via Main Entity
```java
FitFileUpload upload = fitFileUploadRepository.findById(id);
upload.getTotalDistance();  // Summary data
upload.getAvgHeartRate();
```

#### B. Via Generic Messages
```java
List<FitMessage> messages = fitMessageRepository
    .findByFitFileUploadIdAndMessageType(uploadId, "record");

for (FitMessage msg : messages) {
    Object hr = msg.getField("heart_rate");
    Object lat = msg.getField("position_lat");
}
```

#### C. Via Specialized Repositories
```java
List<FitTrackPoint> trackPoints = fitTrackPointRepository
    .findByFitFileUploadIdOrderBySequenceNumber(uploadId);
```

---

## 📊 Database Schema

### Views

#### `v_fit_message_stats`
Message statistics per upload:
```sql
SELECT * FROM v_fit_message_stats WHERE fit_file_upload_id = 123;
```

| fit_file_upload_id | message_type | message_count | first_timestamp | last_timestamp | partially_parsed_count |
|--------------------|--------------|---------------|-----------------|----------------|------------------------|
| 123 | session | 1 | 2024-01-15 10:00:00 | 2024-01-15 10:00:00 | 0 |
| 123 | record | 1523 | 2024-01-15 10:00:05 | 2024-01-15 11:25:18 | 0 |
| 123 | lap | 5 | 2024-01-15 10:15:00 | 2024-01-15 11:20:00 | 0 |

#### `v_fit_unknown_stats`
Unknown message statistics across all uploads:
```sql
SELECT * FROM v_fit_unknown_stats;
```

| global_message_number | affected_uploads | total_occurrences | completely_unknown_count | last_seen |
|-----------------------|------------------|-------------------|--------------------------|-----------|
| 141 | 5 | 23 | 23 | 2024-01-15 |
| 78 | 2 | 8 | 0 | 2024-01-14 |

#### `v_fit_upload_summary`
Upload processing summary:
```sql
SELECT * FROM v_fit_upload_summary WHERE user_id = 1;
```

---

## 🔧 Configuration

### Application Properties

```properties
# FIT File Processing
fit.processing.max-file-size=100MB
fit.processing.allowed-types=activity,settings,workout
fit.processing.strict-validation=true

# Async Processing (future)
fit.processing.async.enabled=false
fit.processing.async.thread-pool-size=4
```

### Service Selection

The `UniversalFitFileServiceImpl` is marked as `@Primary`, so it will be used by default. To switch back to the old implementation:

```java
@Qualifier("enhancedFitFileService")
private FitFileService fitFileService;
```

---

## 🧪 Testing

### Unit Tests

```java
@Test
void testProcessSessionMessage() {
    SessionMesg sessionMesg = new SessionMesg();
    sessionMesg.setTotalDistance(5000.0f);
    sessionMesg.setTotalTimerTime(1800.0f);

    SessionMessageProcessor processor = new SessionMessageProcessor();
    ProcessingResult result = processor.process(sessionMesg, upload, 0);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getData()).containsKey("total_distance");
}
```

### Integration Tests

```java
@Test
void testUploadRealFitFile() {
    MultipartFile file = new MockMultipartFile(
        "file", "activity.fit", "application/fit",
        Files.readAllBytes(Paths.get("test-files/activity.fit"))
    );

    FitFileUploadResponseDto response = service.uploadFitFile(file, userId, request);

    assertThat(response.getProcessingStatus())
        .isEqualTo(FitFileUpload.ProcessingStatus.COMPLETED);
}
```

---

## 📈 Performance Considerations

### Batch Operations

The service uses batch inserts for performance:

```java
// Instead of individual saves:
fitMessageRepository.saveAll(messages);  // Single batch insert
```

### Indexing Strategy

Critical indexes for query performance:

```sql
INDEX idx_fit_msg_upload_type (fit_file_upload_id, message_type)
INDEX idx_fit_msg_timestamp (message_timestamp)
INDEX idx_track_point_timestamp (fit_file_upload_id, timestamp)
```

### JSONB Performance

MariaDB's JSON support enables:
- Efficient storage of variable-schema data
- JSON path queries: `JSON_EXTRACT(message_data, '$.heart_rate')`
- Minimal storage overhead vs. wide tables

---

## 🐛 Troubleshooting

### Issue: File Processing Fails

**Check:**
1. File integrity: Is the CRC valid?
2. Logs: Look for processor errors
3. Unknown messages table: Are messages being stored as unknown?

```sql
SELECT * FROM fit_unknown_messages WHERE fit_file_upload_id = ?;
```

### Issue: Missing Data

**Check:**
1. Generic messages table:
```sql
SELECT message_type, COUNT(*)
FROM fit_messages
WHERE fit_file_upload_id = ?
GROUP BY message_type;
```

2. Parsing notes:
```sql
SELECT * FROM fit_messages
WHERE fit_file_upload_id = ?
AND fully_parsed = FALSE;
```

### Issue: Performance Degradation

**Check:**
1. Number of messages per upload:
```sql
SELECT fit_file_upload_id, COUNT(*) as msg_count
FROM fit_messages
GROUP BY fit_file_upload_id
ORDER BY msg_count DESC
LIMIT 10;
```

2. Missing indexes:
```sql
SHOW INDEX FROM fit_messages;
```

---

## 🚀 Future Enhancements

### Phase 2: Additional Processors

- [ ] `DeviceInfoMessageProcessor` - Device metadata
- [ ] `EventMessageProcessor` - Training events
- [ ] `HrvMessageProcessor` - Heart rate variability
- [ ] `ZoneMessageProcessor` - Training zones
- [ ] `WorkoutMessageProcessor` - Planned workouts
- [ ] `SegmentMessageProcessor` - Strava segments

### Phase 3: Advanced Features

- [ ] **Async Processing** - Spring `@Async` for large files
- [ ] **Streaming Processing** - Handle files > 100MB
- [ ] **Message Queue Integration** - RabbitMQ/Kafka for background processing
- [ ] **Partial File Recovery** - Resume processing after errors
- [ ] **Multi-Sport Support** - Swimming, cycling, triathlon
- [ ] **Developer Field Registry** - Catalog known ConnectIQ fields

### Phase 4: Analytics

- [ ] **Message Pattern Analysis** - Identify common message sequences
- [ ] **Unknown Message Learning** - Auto-suggest processor implementations
- [ ] **Data Quality Metrics** - Track parsing success rates
- [ ] **Performance Profiling** - Per-processor performance metrics

---

## 📚 References

- [Garmin FIT Protocol](https://developer.garmin.com/fit/protocol/)
- [FIT SDK Documentation](https://developer.garmin.com/fit/)
- [FIT File Types](https://developer.garmin.com/fit/file-types/)
- [GitHub: FIT Java SDK](https://github.com/garmin/fit-java-sdk)

---

## 📝 Migration Guide

### From Old Implementation

1. **Database Migration**: Run `V007__add_universal_fit_processing.sql`
2. **Service Configuration**: `UniversalFitFileServiceImpl` is automatically used (`@Primary`)
3. **Reprocess Existing Files** (optional):
   ```java
   fitFileService.processPendingUploads();
   ```

### API Compatibility

The new service implements the same `FitFileService` interface, so **no API changes are required** for existing clients.

---

## 🤝 Contributing

To add a new message processor:

1. Create new class extending `AbstractMessageProcessor`
2. Add `@Component` annotation (auto-discovery)
3. Implement required methods
4. Write unit tests
5. Update this documentation

---

## 📄 License

Part of the MainStream running platform.

---

**Version**: 1.0.0
**Last Updated**: 2025-01-05
**Author**: Claude (AI Assistant)
