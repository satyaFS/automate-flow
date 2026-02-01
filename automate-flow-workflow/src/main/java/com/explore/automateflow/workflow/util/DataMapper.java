package com.explore.automateflow.workflow.util;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;

public class DataMapper {

    private final ObjectMapper objectMapper;

    public DataMapper() {
        this.objectMapper = new ObjectMapper();
    }

    public JsonNode mapData(JsonNode sourceData, Map<String, String> mappingRules) {
        if (mappingRules == null || mappingRules.isEmpty()) {
            return sourceData;
        }

        // Convert JsonNode to String for JsonPath processing if needed,
        // but Jayway can work with objects. However, standard approach is using the
        // document context.
        // For simplicity with JsonPath's read, passing JSON string or Map is common.
        Object document = com.jayway.jsonpath.Configuration.defaultConfiguration().jsonProvider()
                .parse(sourceData.toString());

        ObjectNode resultNode = objectMapper.createObjectNode();

        mappingRules.forEach((targetField, jsonPathParams) -> {
            try {
                Object value = JsonPath.read(document, jsonPathParams);
                // Basic type handling - this can be expanded
                if (value instanceof String) {
                    resultNode.put(targetField, (String) value);
                } else if (value instanceof Integer) {
                    resultNode.put(targetField, (Integer) value);
                } else if (value instanceof Boolean) {
                    resultNode.put(targetField, (Boolean) value);
                } else if (value instanceof Double) {
                    resultNode.put(targetField, (Double) value);
                } else {
                    // Fallback for objects/arrays -> putPOJO
                    resultNode.putPOJO(targetField, value);
                }
            } catch (Exception e) {
                // If path not found, ignore or set null.
                // For now, we'll just log/ignore to avoid breaking execution
                // resultNode.putNull(targetField);
            }
        });

        // If no mappings produced a result (e.g. all failed), technically we return
        // empty object or the original?
        // Requirement implies we construct a NEW payload based on mappings.
        return resultNode;
    }
}
