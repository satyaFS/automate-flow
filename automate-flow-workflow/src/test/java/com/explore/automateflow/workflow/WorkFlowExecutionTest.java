package com.explore.automateflow.workflow;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

import com.explore.automateflow.workflow.controller.WorkFlowController;
import com.explore.automateflow.workflow.dto.ActionDTO;
import com.explore.automateflow.workflow.entity.WorkFlow;
import com.explore.automateflow.workflow.repository.WorkFlowRepository;
import com.explore.automateflow.workflow.service.impl.WorkFlowServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@WebFluxTest(WorkFlowController.class)
@Import(WorkFlowServiceImpl.class)
public class WorkFlowExecutionTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private WorkFlowRepository workFlowRepository;

    // Use DEEP_STUBS to handle the fluent API of WebClient without mocking every
    // step manually
    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private WebClient webClient;

    @Test
    void testExecuteWorkFlow() {
        // 1. Setup Data
        String workflowId = "wf-1";
        String actionId1 = "action-1";

        WorkFlow mockWorkflow = new WorkFlow();
        mockWorkflow.setWorkflowId(workflowId);
        mockWorkflow.setActionIds(Arrays.asList(actionId1));

        // Note: Using java.util.Map explicitly for the new field or null
        ActionDTO action1 = new ActionDTO(actionId1, "Action 1", "Desc 1", "http://test/1", "POST", false, null, null);

        // 2. Mock Repository
        when(workFlowRepository.findById(workflowId)).thenReturn(Mono.just(mockWorkflow));

        // 3. Mock WebClient Calls
        ObjectMapper mapper = new ObjectMapper();
        JsonNode mockResponse = mapper.createObjectNode().put("status", "success");

        // Mock getting Action details (GET request)
        when(webClient.get()
                .uri(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.<Object[]>any())
                .retrieve()
                .bodyToMono(ActionDTO.class))
                .thenReturn(Mono.just(action1));

        // Also handle the case where uri might be called differently
        when(webClient.get()
                .uri(org.mockito.ArgumentMatchers.anyString())
                .retrieve()
                .bodyToMono(ActionDTO.class))
                .thenReturn(Mono.just(action1));

        // Mock executing Action (POST request)
        when(webClient.post()
                .uri(org.mockito.ArgumentMatchers.anyString())
                .bodyValue(any())
                .retrieve()
                .bodyToMono(JsonNode.class))
                .thenReturn(Mono.just(mockResponse));

        // 4. Execute Request
        webTestClient.post()
                .uri("/workflow/" + workflowId + "/execute")
                .bodyValue(mockResponse)
                .exchange()
                .expectStatus().isOk();

    }
}
