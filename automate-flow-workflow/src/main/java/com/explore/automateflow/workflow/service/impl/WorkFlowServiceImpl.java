package com.explore.automateflow.workflow.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.explore.automateflow.workflow.entity.Action;
import com.explore.automateflow.workflow.entity.Trigger;
import com.explore.automateflow.workflow.entity.WebhookTrigger;
import com.explore.automateflow.workflow.entity.WorkFlow;
import com.explore.automateflow.workflow.repository.WorkFlowRepository;
import com.explore.automateflow.workflow.service.WorkFlowService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;


@Service
public class WorkFlowServiceImpl implements WorkFlowService {
    @Autowired
    private final WorkFlowRepository workFlowRepository;


    @Override
    public Mono<WorkFlow> createWorkFlow(WorkFlow workFlow) {
        return workFlowRepository.save(workFlow);
    }

    @Override
    public Mono<WorkFlow> updateWorkFlow(WorkFlow workFlow) {
        return workFlowRepository.save(workFlow);
    }

    @Override
    public Mono<WorkFlow> getWorkFlow(String workflowId) {
        return workFlowRepository.findById(workflowId);
    }

    @Override
    public Mono<Void> deleteWorkFlow(String workflowId) {
        return workFlowRepository.deleteById(workflowId);
    }

    @Override
    public Flux<WorkFlow> getAllWorkFlows() {
        return workFlowRepository.findAll();
    }

    @Override
    public Flux<WorkFlow> getAllWorkFlowsByUserId(String userId) {
        return workFlowRepository.findByUserId(userId);
    }

    @Override
    public Mono<Void> updateActions(String workflowId, List<String> actionIds) {
        // Implement the logic to update actions
        return workFlowRepository.findById(workflowId).flatMap(workFlow -> {
            workFlow.setActionIds(actionIds);
            return workFlowRepository.save(workFlow).then();
        });
    }
}
