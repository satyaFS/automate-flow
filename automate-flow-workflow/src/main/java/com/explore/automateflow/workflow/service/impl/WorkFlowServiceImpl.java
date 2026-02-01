package com.explore.automateflow.workflow.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.explore.automateflow.workflow.config.RetryConfig;
import com.explore.automateflow.workflow.dto.ActionDTO;
import com.explore.automateflow.workflow.dto.TriggerDTO;
import com.explore.automateflow.workflow.dto.WorkFlowDTO;
import com.explore.automateflow.workflow.repository.WorkFlowExecutionRepository;
import com.explore.automateflow.workflow.repository.WorkFlowRepository;
import com.explore.automateflow.workflow.service.WorkFlowService;
import com.explore.automateflow.workflow.util.DataMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class WorkFlowServiceImpl implements WorkFlowService {
    private final WorkFlowExecutionRepository workFlowExecutionRepository;
    private final WorkFlowRepository workFlowRepository;
    private final WebClient webClient;
    private final RetryConfig retryConfig;
    private final DataMapper dataMapper = new DataMapper();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(WorkFlowServiceImpl.class);

    public WorkFlowServiceImpl(WorkFlowRepository workFlowRepository, WebClient webClient,
            WorkFlowExecutionRepository workFlowExecutionRepository, RetryConfig retryConfig) {
        this.workFlowRepository = workFlowRepository;
        this.webClient = webClient;
        this.workFlowExecutionRepository = workFlowExecutionRepository;
        this.retryConfig = retryConfig;
    }

    @Override
    public Mono<WorkFlowDTO> createWorkFlow(WorkFlowDTO workFlowDTO) {
        Mono<WorkFlowDTO> persistedWorkflowDTO = workFlowRepository.save(workFlowDTO.toEntity())
                .map(workFlowEntity -> WorkFlowDTO.fromEntity(workFlowEntity))
                .cache();
        var trigger = persistedWorkflowDTO
                .flatMap(it -> webClient.post().uri("http://localhost:8083/triggers").bodyValue(it.getWorkflowId())
                        .retrieve().bodyToMono(TriggerDTO.class));
        return trigger.then(persistedWorkflowDTO);
    }

    @Override
    public Mono<WorkFlowDTO> updateWorkFlow(WorkFlowDTO workFlow) {
        return workFlowRepository.save(workFlow.toEntity())
                .flatMap(workFlowEntity -> Mono.just(WorkFlowDTO.fromEntity(workFlowEntity)));
    }

    @Override
    public Mono<WorkFlowDTO> getWorkFlow(String workflowId) {
        return workFlowRepository.findById(workflowId)
                .flatMap(workFlowEntity -> Mono.just(WorkFlowDTO.fromEntity(workFlowEntity)));
    }

    @Override
    public Mono<Void> deleteWorkFlow(String workflowId) {
        return workFlowRepository.deleteById(workflowId);
    }

    @Override
    public Flux<WorkFlowDTO> getAllWorkFlows() {
        return workFlowRepository.findAll()
                .flatMap(workFlowEntity -> Mono.just(WorkFlowDTO.fromEntity(workFlowEntity)));
    }

    @Override
    public Flux<WorkFlowDTO> getAllWorkFlowsByUserId(String userId) {
        return workFlowRepository.findByUserId(userId)
                .flatMap(workFlowEntity -> Mono.just(WorkFlowDTO.fromEntity(workFlowEntity)));
    }

    @Override
    public Mono<Void> updateActions(String workflowId, List<String> actionIds) {
        // Implement the logic to update actions
        return workFlowRepository.findById(workflowId).flatMap(workFlow -> {
            workFlow.setActionIds(actionIds);
            return workFlowRepository.save(workFlow).then();
        });
    }

    @Override
    public Mono<Void> executeWorkFlow(String workflowId, JsonNode triggerResponse) {
        logger.info("Starting execution for workflow: {}", workflowId);
        return workFlowRepository.findById(workflowId)
                .flatMap(workFlow -> {
                    logger.info("Found workflow: {}", workFlow.getWorkflowId());
                    com.explore.automateflow.workflow.entity.WorkFlowExecution execution = new com.explore.automateflow.workflow.entity.WorkFlowExecution();
                    execution.setWorkflowId(workflowId);
                    execution.setStartTime(java.time.LocalDateTime.now());
                    execution.setStatus("RUNNING");

                    return workFlowExecutionRepository.save(execution)
                            .flatMap(savedExecution -> {
                                logger.info("Created execution record: {}", savedExecution.getId());
                                return executeActionsSequentially(workFlow.getActionIds(), triggerResponse,
                                        savedExecution)
                                        .then(Mono.defer(() -> {
                                            logger.info("Execution completed successfully");
                                            savedExecution.setStatus("COMPLETED");
                                            savedExecution.setEndTime(java.time.LocalDateTime.now());
                                            return workFlowExecutionRepository.save(savedExecution);
                                        }))
                                        .onErrorResume(e -> {
                                            logger.error("Execution failed", e);
                                            savedExecution.setStatus("FAILED");
                                            savedExecution.setEndTime(java.time.LocalDateTime.now());
                                            return workFlowExecutionRepository.save(savedExecution).then(Mono.error(e));
                                        });
                            });
                })
                .doOnError(e -> logger.error("Fatal error starting execution", e))
                .then();
    }

    private Mono<JsonNode> executeActionsSequentially(List<String> actionIds, JsonNode previousResponse,
            com.explore.automateflow.workflow.entity.WorkFlowExecution execution) {
        String actionId = actionIds.get(0);
        logger.info("previousResponse {}", previousResponse);
        List<String> remainingActionIds = actionIds.subList(1, actionIds.size());

        long stepStartTime = System.currentTimeMillis();
        AtomicInteger retryCounter = new AtomicInteger(0);

        return getAction(actionId)
                .flatMap(action -> {
                    com.explore.automateflow.workflow.entity.ExecutionStep step = new com.explore.automateflow.workflow.entity.ExecutionStep();
                    step.setActionId(action.getActionId());
                    step.setActionName(action.getName());
                    step.setInput(toMap(previousResponse));

                    return executeAction(action, previousResponse)
                            .retryWhen(retryConfig.getRetrySpec()
                                    .doBeforeRetry(signal -> {
                                        retryCounter.incrementAndGet();
                                        logger.warn("Retry {} for action {}: {}",
                                                retryCounter.get(), action.getName(), signal.failure().getMessage());
                                    }))
                            .flatMap(response -> {
                                logger.info("response {}", response);
                                step.setOutput(toMap(response));
                                step.setStatus("SUCCESS");
                                step.setDurationMs(System.currentTimeMillis() - stepStartTime);
                                step.setRetryCount(retryCounter.get());
                                execution.addStep(step);

                                return workFlowExecutionRepository.save(execution)
                                        .then(remainingActionIds.isEmpty() ? Mono.just(response)
                                                : executeActionsSequentially(remainingActionIds, response, execution));
                            })
                            .onErrorResume(e -> {
                                logger.error("Action {} failed after {} retries: {}",
                                        action.getName(), retryCounter.get(), e.getMessage());
                                step.setErrorMessage(e.getMessage());
                                step.setStatus("FAILURE");
                                step.setDurationMs(System.currentTimeMillis() - stepStartTime);
                                step.setRetryCount(retryCounter.get());
                                execution.addStep(step);
                                return workFlowExecutionRepository.save(execution)
                                        .then(Mono.error(e));
                            });
                });
    }

    private Mono<String> getActionUrl(String actionId) {
        return webClient.get().uri("/action/{actionId}", actionId).retrieve().bodyToMono(String.class);
    }

    private Mono<ActionDTO> getAction(String actionId) {
        return webClient.get().uri("http://localhost:8084/actions/{actionId}", actionId).retrieve()
                .bodyToMono(ActionDTO.class);
    }

    private Mono<JsonNode> executeAction(ActionDTO actionDTO, JsonNode previousResponse) {
        String uri = actionDTO.getUrl();
        Mono<JsonNode> response;
        switch (actionDTO.getMethod()) {
            case "GET":
                response = webClient.get().uri(uri).retrieve().bodyToMono(JsonNode.class);
                break;
            case "POST":
                response = webClient.post().uri(uri).bodyValue(previousResponse).retrieve().bodyToMono(JsonNode.class);
                break;
            case "PUT":
                response = webClient.put().uri(uri).bodyValue(previousResponse).retrieve().bodyToMono(JsonNode.class);
                break;
            case "DELETE":
                response = webClient.delete().uri(uri).retrieve().bodyToMono(JsonNode.class);
                break;
            default:
                JsonNode requestBody = previousResponse;
                if (actionDTO.getMapping() != null && !actionDTO.getMapping().isEmpty()) {
                    requestBody = dataMapper.mapData(previousResponse, actionDTO.getMapping());
                }
                response = webClient.post().uri(uri).bodyValue(requestBody).retrieve().bodyToMono(JsonNode.class);
                break;
        }
        return response;
    }

    @Override
    public Flux<com.explore.automateflow.workflow.entity.WorkFlowExecution> getExecutions(String workflowId) {
        return workFlowExecutionRepository.findByWorkflowId(workflowId);
    }

    private Map<String, Object> toMap(JsonNode jsonNode) {
        if (jsonNode == null) {
            return null;
        }
        return objectMapper.convertValue(jsonNode, new TypeReference<Map<String, Object>>() {
        });
    }
}
