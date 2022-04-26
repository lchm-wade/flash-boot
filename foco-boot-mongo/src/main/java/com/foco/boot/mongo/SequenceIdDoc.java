package com.foco.boot.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * description: 存储自增主键ID
 * @Author lucoo
 * @Date 2021/6/27 10:13
 */
@Data
@Document(collection = "sequence")
public class SequenceIdDoc {

    public static final String SEQ_ID_FIELD="seq_id";
    public static final String COLLECTION_NAME_FIELD="collection_name";

    @Id
    private String id;

    @Field(SEQ_ID_FIELD)
    private long seqId;

    @Field(COLLECTION_NAME_FIELD)
    private String collectionName;
}