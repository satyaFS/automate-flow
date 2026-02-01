package com.explore.automateflow.workflow.util;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DataMapperTest {

    private final DataMapper dataMapper = new DataMapper();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testMapData_Success() throws Exception {
        // 1. Source Data (Complex Nested JSON)
        String jsonDta = """
                {
                    "trigger": {
                        "body": {
                            "name": "John Doe",
                            "email": "john@example.com",
                            "address": {
                                "city": "New York"
                            }
                        }
                    },
                    "meta": {
                        "id": 123
                    }
                }
                """;
        JsonNode source = objectMapper.readTree(jsonDta);

        // 2. Mapping Rules
        Map<String, String> mapping = new HashMap<>();
        mapping.put("recipientName", "$.trigger.body.name");
        mapping.put("recipientEmail", "$.trigger.body.email");
        mapping.put("city", "$.trigger.body.address.city");
        mapping.put("triggerId", "$.meta.id");

        // 3. Execute
        JsonNode result = dataMapper.mapData(source, mapping);

        // 4. Verify
        Assertions.assertEquals("John Doe", result.get("recipientName").asText());
        Assertions.assertEquals("john@example.com", result.get("recipientEmail").asText());
        Assertions.assertEquals("New York", result.get("city").asText());
        Assertions.assertEquals(123, result.get("triggerId").asInt());
    }
}
