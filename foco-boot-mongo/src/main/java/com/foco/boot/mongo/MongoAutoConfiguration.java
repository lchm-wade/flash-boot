package com.foco.boot.mongo;

import com.foco.boot.mongo.properties.MongoProperties;
import com.foco.context.util.BootStrapPrinter;
import com.foco.model.constant.FocoConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import javax.annotation.PostConstruct;

/**
 * description----------
 *
 * @Author lucoo
 * @Date 2021/6/27 10:13
 */
@Slf4j
@EnableConfigurationProperties(value = MongoProperties.class)
public class MongoAutoConfiguration {
    @PostConstruct
    public void init() {
        BootStrapPrinter.log("foco-boot-mongo",this.getClass());
    }
    @Bean
    @ConditionalOnProperty(prefix = MongoProperties.PREFIX, name = FocoConstants.ENABLED, matchIfMissing = true)
    InsertEventListener createInsertEventListener() {
        return new InsertEventListener();
    }

    @Autowired
    private MongoMappingContext mongoMappingContext;
    @Autowired
    private MongoDatabaseFactory mongoDatabaseFactory;

    @Bean
    public MappingMongoConverter mappingMongoConverter() {
        mongoMappingContext.setAutoIndexCreation(true);
        mongoMappingContext.afterPropertiesSet();
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDatabaseFactory);
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return converter;
    }
}

