package com.mainstream.fitfile.processor;

import com.garmin.fit.FileIdMesg;
import com.garmin.fit.Mesg;
import com.mainstream.fitfile.entity.FitFileUpload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Processor for FIT file_id messages
 * Contains metadata about the FIT file itself
 */
@Slf4j
@Component
public class FileIdMessageProcessor extends AbstractMessageProcessor {

    @Override
    public String getMessageType() {
        return "file_id";
    }

    @Override
    public Integer getGlobalMessageNumber() {
        return 0; // file_id is message number 0
    }

    @Override
    public ProcessingResult process(Mesg mesg, FitFileUpload fitFileUpload, int sequenceNumber) {
        if (!(mesg instanceof FileIdMesg)) {
            return ProcessingResult.failure("Message is not a FileIdMesg");
        }

        try {
            FileIdMesg fileIdMesg = (FileIdMesg) mesg;
            Map<String, Object> fields = new HashMap<>();

            // Extract all file_id fields
            if (fileIdMesg.getType() != null) {
                fields.put("type", fileIdMesg.getType().toString());
            }
            if (fileIdMesg.getManufacturer() != null) {
                fields.put("manufacturer", fileIdMesg.getManufacturer().toString());
                // Also update the main upload entity
                fitFileUpload.setFitManufacturer(fileIdMesg.getManufacturer().toString());
            }
            if (fileIdMesg.getProduct() != null) {
                fields.put("product", fileIdMesg.getProduct().toString());
                fitFileUpload.setFitProduct(fileIdMesg.getProduct().toString());
            }
            if (fileIdMesg.getSerialNumber() != null) {
                fields.put("serial_number", fileIdMesg.getSerialNumber());
                fitFileUpload.setFitDeviceSerial(fileIdMesg.getSerialNumber().toString());
            }
            if (fileIdMesg.getTimeCreated() != null) {
                fields.put("time_created", convertToLocalDateTime(fileIdMesg.getTimeCreated()));
            }
            if (fileIdMesg.getNumber() != null) {
                fields.put("number", fileIdMesg.getNumber());
            }
            if (fileIdMesg.getProductName() != null) {
                fields.put("product_name", fileIdMesg.getProductName());
            }

            // Extract any additional fields not covered above
            Map<String, Object> additionalFields = extractAllFields(mesg);
            fields.putAll(additionalFields);

            log.info("Processed file_id: manufacturer={}, product={}, serial={}",
                fields.get("manufacturer"), fields.get("product"), fields.get("serial_number"));

            return ProcessingResult.success(fields);

        } catch (Exception e) {
            log.error("Error processing file_id message: {}", e.getMessage(), e);
            return ProcessingResult.failure(e.getMessage());
        }
    }

    @Override
    public int getPriority() {
        return 1000; // High priority - should be processed first
    }
}
