package com.explore.automateflow.workflow.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import com.explore.automateflow.workflow.dto.ActionDTO;
import com.explore.automateflow.workflow.dto.TriggerDTO;
import com.explore.automateflow.workflow.dto.WorkFlowDTO;
import com.explore.automateflow.workflow.entity.WorkFlow;
import com.explore.automateflow.workflow.repository.WorkFlowRepository;
import com.explore.automateflow.workflow.service.impl.WorkFlowServiceImpl;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class WorkFlowServiceTest {

    @Mock
    private WorkFlowRepository workFlowRepository;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private WorkFlowServiceImpl workFlowService;

    @BeforeEach
    void setUp() {
        workFlowService = new WorkFlowServiceImpl(workFlowRepository, webClient);
        ReflectionTestUtils.setField(workFlowService, "triggerServiceUrl", "http://localhost:8083");
        ReflectionTestUtils.setField(workFlowService, "actionServiceUrl", "http://localhost:8084");
    }

    @Test
    void createWorkFlow_ShouldPersistWorkflowActionsAndTrigger() {
        WorkFlowDTO inputDto = new WorkFlowDTO();
        inputDto.setWorkflowName("Test Workflow");
        inputDto.setActions(List.of(new ActionDTO("1", "Action1", "Desc", "url", "POST")));

        WorkFlow savedEntity = new WorkFlow();
        savedEntity.setWorkflowId("wf-1");
        savedEntity.setWorkflowName("Test Workflow");

        when(workFlowRepository.save(any(WorkFlow.class))).thenReturn(Mono.just(savedEntity));

        // Mock WebClient calls
        // Since mocking WebClient chain is complex, I will focus on unit testing logic flow if possible
        // But here I need to mock the chain: webClient.post().uri(...).bodyValue(...).retrieve().bodyToMono(...)

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // Return Action IDs
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(Mono.just(new TriggerDTO("tr-1", "url", null, 1000L)));
        when(responseSpec.bodyToMono(any(org.springframework.core.ParameterizedTypeReference.class))).thenReturn(Mono.just(List.of("ac-1")));

        StepVerifier.create(workFlowService.createWorkFlow(inputDto))
            .expectNextMatches(dto -> dto.getWorkflowId().equals("wf-1"))
            .verifyComplete();
    }
}
