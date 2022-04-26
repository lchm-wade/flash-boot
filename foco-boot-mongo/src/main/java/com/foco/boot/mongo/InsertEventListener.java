package com.foco.boot.mongo;

import com.foco.model.exception.SystemException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Resource;

/**
 *description：自动插入自增ID
 * @Author lucoo
 * @Date 2021/6/27 10:13
 */
public class InsertEventListener extends AbstractMongoEventListener<Object> {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> event) {
        Object source = event.getSource();
        ReflectionUtils.doWithFields(source.getClass(), field -> {
            ReflectionUtils.makeAccessible(field);
            if (field.isAnnotationPresent(MongoAutoInc.class)) {
                field.set(source, getNextId(event.getCollectionName()));
            }
        });
    }
    private Long getNextId(String collectionName) {
        Query query = new Query(Criteria.where(SequenceIdDoc.COLLECTION_NAME_FIELD).is(collectionName));
        Update update = new Update();
        update.inc(SequenceIdDoc.SEQ_ID_FIELD, 1);
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(true);
        options.returnNew(true);
        SequenceIdDoc seqId = mongoTemplate.findAndModify(query, update, options, SequenceIdDoc.class);
        if (seqId == null) {
            SystemException.throwException("Mongo无法获取自增Id");
        }
        return seqId.getSeqId();
    }
}
