package com.explore.automateflow.action.dao;


import java.util.List;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.stereotype.Component;

import com.explore.automateflow.action.entity.Action;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ActionDAO {
    private ReactiveMongoTemplate reactiveMongoTemplate;
    

    public ActionDAO(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public Mono<Action> save(Action action ) {
        return this.reactiveMongoTemplate.save(action);
    }

    public Flux<Action> saveAll(List<Action> actions) {
        return this.reactiveMongoTemplate.insertAll(actions);
    }
    
    public Mono<Action> findById(String id) {
        return this.reactiveMongoTemplate.findById(id, Action.class);
    }
    
    public Mono<Void> delete(Action action) {
        return this.reactiveMongoTemplate.remove(action).then();
    }
    
    public Mono<Void> updateAction(String id, Action action) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
        return reactiveMongoTemplate.updateFirst(query, createUpdateDefinition(action), Action.class).then();
    }

    private UpdateDefinition createUpdateDefinition(Action action) {
        Update updateDefinition = new Update()
            .set("name", action.getName())
            .set("description", action.getDescription())
            .set("url", action.getUrl());
        return updateDefinition;
    }
}
    