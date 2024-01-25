package com.explore.automateflow.action.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.explore.automateflow.action.dao.ActionDAO;
import com.explore.automateflow.action.dto.ActionDTO;
import com.explore.automateflow.action.entity.Action;
import com.explore.automateflow.action.service.ActionService;

import reactor.core.publisher.Mono;

@Service
public class ActionServiceImpl implements ActionService{
    private ActionDAO actionDAO;

    public ActionServiceImpl(ActionDAO actionDAO) {
        this.actionDAO = actionDAO;
    }

    @Override
    public Mono<Action> getActionById(String id) {
       return actionDAO.findById(id);
    }

    @Override
    public Mono<Action> saveAction(Action action) {
       return actionDAO.save(action);
    }

    @Override
    public Mono<Void> updateAction(String id, Action action) {
        return actionDAO.updateAction(id, action).then();
    }

    @Override
    public Mono<Void> deleteAction(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAction'");
    }

    @Override
    public Mono<String> getActionUrl(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getActionUrl'");
    }

    @Override
    public Mono<List<String>> saveActions(List<ActionDTO> actionDTOs) {
        var actions = actionDTOs.stream().map(it -> it.toEntity()).collect(Collectors.toList());
        return actionDAO.saveAll(actions)
                .flatMap(it -> Mono.just(it.getActionId())).collectList();
    }
}
