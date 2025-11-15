package com.mainstream.email.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Service for processing email templates
 */
@Slf4j
@Service
public class EmailTemplateService {

    private static final String TEMPLATE_PATH = "templates/email/";

    /**
     * Process an email template with the given variables
     *
     * @param templateName the name of the template (without .html extension)
     * @param variables the variables to replace in the template
     * @return the processed template as HTML string
     */
    public String processTemplate(String templateName, Map<String, Object> variables) {
        try {
            String template = loadTemplate(templateName);
            return replaceVariables(template, variables);
        } catch (IOException e) {
            log.error("Failed to process template: {}", templateName, e);
            return getDefaultTemplate(variables);
        }
    }

    /**
     * Load template from resources
     */
    private String loadTemplate(String templateName) throws IOException {
        String templateFile = templateName.endsWith(".html") ? templateName : templateName + ".html";
        ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH + templateFile);

        if (!resource.exists()) {
            throw new IOException("Template not found: " + templateFile);
        }

        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    /**
     * Replace variables in template using simple string replacement
     * Format: {{variableName}}
     */
    private String replaceVariables(String template, Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return template;
        }

        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }

        return result;
    }

    /**
     * Generate a default template if the requested template cannot be loaded
     */
    private String getDefaultTemplate(Map<String, Object> variables) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'>");
        html.append("<style>body{font-family:Arial,sans-serif;padding:20px;}</style>");
        html.append("</head><body>");
        html.append("<h2>E-Mail von MainStream</h2>");

        if (variables != null) {
            variables.forEach((key, value) ->
                html.append("<p><strong>").append(key).append(":</strong> ")
                    .append(value).append("</p>"));
        }

        html.append("</body></html>");

        return html.toString();
    }
}
