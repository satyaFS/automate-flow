package com.explore.automateflow.trigger.dao;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.stereotype.Component;

import com.explore.automateflow.trigger.entity.Trigger;

import reactor.core.publisher.Mono;
@Component
public class TriggerDAO {
    private ReactiveMongoTemplate mongoTemplate;
    public TriggerDAO (ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Mono<Trigger> getTrigger(String triggerId) {
        return mongoTemplate.findById(triggerId, Trigger.class);
    }

    public Mono<Trigger> getTriggerByWorkflowId(String workflowId) {
        var criteria = Criteria.where("workflowId").is(workflowId);
        return mongoTemplate.findOne(Query.query(criteria), Trigger.class);
    }

    public Mono<Trigger> saveTrigger(Trigger trigger) {
        return mongoTemplate.save(trigger);
    }

    public Mono<Void> updateTrigger(String triggerId, Trigger trigger) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(triggerId));
        return mongoTemplate.updateFirst(query, createUpdateDefinition(trigger), Trigger.class).then();
    }

    private UpdateDefinition createUpdateDefinition(Trigger trigger) {
        return new Update()
                .set("url", trigger.getUrl())
                .set("triggerType", trigger.getTriggerType().toString())
                .set("pollingInterval", trigger.getPollingInterval());
    }

    public Mono<Void> deleteTrigger(String triggerId) {
        return mongoTemplate.remove(triggerId).then();
    }
}
