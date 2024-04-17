package com.explore.automateflow.workflow.controller;

import com.explore.automateflow.workflow.dto.WorkFlowDTO;
import com.explore.automateflow.workflow.service.WorkFlowService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/workflow")
public class WorkFlowController {
    private final WorkFlowService workFlowService;

    public WorkFlowController(WorkFlowService workFlowService) {
        this.workFlowService = workFlowService;
    }

    @PostMapping
    public Mono<WorkFlowDTO> createWorkFlow(@RequestBody WorkFlowDTO workFlow) {
        return this.workFlowService.createWorkFlow(workFlow);
    }

    @PutMapping
    public Mono<WorkFlowDTO> updateWorkFlow(@RequestBody WorkFlowDTO workFlow) {
        return this.workFlowService.updateWorkFlow(workFlow);
    }

    @GetMapping("/{id}")
    public Mono<WorkFlowDTO> getWorkFlow(@PathVariable String id) {
        return this.workFlowService.getWorkFlow(id);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteWorkFlow(@PathVariable String id) {
        return this.workFlowService.deleteWorkFlow(id);
    }

    @GetMapping
    public Flux<WorkFlowDTO> getAllWorkFlows() {
        return this.workFlowService.getAllWorkFlows();
    }

    @GetMapping("/user/{userId}")
    public Flux<WorkFlowDTO> getAllWorkFlowsByUserId(@PathVariable String userId) {
        return this.workFlowService.getAllWorkFlowsByUserId(userId);
    }

    @PutMapping("/{id}/actions")
    public Mono<Void> updateActions(@PathVariable String id, @RequestBody List<String> actionIds) {
        return this.workFlowService.updateActions(id, actionIds);
    }

    // @PostMapping("/{id}/execute")
    // public Mono<Void> executeWorkFlow(@PathVariable String id, @RequestBody JsonNode triggerResponse) {
    //     return this.workFlowService.executeWorkFlow(id, triggerResponse);
    // }
}