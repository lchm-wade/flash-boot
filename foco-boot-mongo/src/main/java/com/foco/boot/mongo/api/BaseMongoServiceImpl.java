package com.foco.boot.mongo.api;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.foco.boot.mongo.util.FormatUtils;
import com.foco.context.core.Env;
import com.foco.model.page.PageParam;
import com.foco.model.page.PageResponse;
import com.foco.model.page.ThreadPagingUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.convert.QueryMapper;
import org.springframework.data.mongodb.core.convert.UpdateMapper;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author lucoo
 * @version 1.0.0
 * @Description TODO
 * @date 2021-07-21 16:12
 */
@Slf4j
public abstract class BaseMongoServiceImpl<T> implements IBaseMongoService<T>{
    @Autowired
    protected MongoTemplate mongoTemplate;
    @Autowired
    MongoConverter mongoConverter;
    QueryMapper queryMapper;
    UpdateMapper updateMapper;

    @Value("${spring.profiles.active}")
    private  String env;
    private Boolean print;
    @PostConstruct
    public void init() {
        queryMapper = new QueryMapper(mongoConverter);
        updateMapper = new UpdateMapper(mongoConverter);
        print = StrUtil.containsAny(env, Env.DEV.getEnv(),Env.TEST.getEnv());
    }

    /**
     * 保存一个对象到mongodb
     * @param bean
     * @return
     */
    @Override
    public T save(T bean) {
        logSave(bean);
        mongoTemplate.save(bean);
        return bean;
    }

    /**
     * 根据id删除对象
     */
    @Override
    public void deleteById(String id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
        logDelete(query);
        mongoTemplate.remove(this.findById(id));
    }


    /**
     * 根据对象的属性删除
     * @param t
     */
    @Override
    public void deleteByCondition(T t) {
        Query query = buildBaseQuery(t);
        logDelete(query);
        mongoTemplate.remove(query, getEntityClass());
    }

    /**
     * 根据id进行更新
     * @param id
     * @param t
     */
    @Override
    public void updateById(String id, T t) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
        Update update = buildBaseUpdate(t);
        logUpdate(query,update,false);
        update(query, update);
    }

    /**
     * 根据对象的属性查询
     * @param t
     * @return
     */
    @Override
    public List<T> findByCondition(T t) {
        Query query = buildBaseQuery(t);
        logQuery(query);
        return mongoTemplate.find(query, getEntityClass());
    }

    /**
     * 通过条件查询实体(集合)
     * @param query
     * @return
     */
    @Override
    public List<T> find(Query query) {
        logQuery(query);
        return mongoTemplate.find(query, this.getEntityClass());
    }

    /**
     * 通过一定的条件查询一个实体
     * @param query
     * @return
     */
    @Override
    public T findOne(Query query) {
        logQuery(query);
        return mongoTemplate.findOne(query, this.getEntityClass());
    }
    @Override
    public T findOne(T t) {
        Query query = buildBaseQuery(t);
        return findOne(query);
    }
    /**
     * 通过条件查询更新数据
     * @param query
     * @param update
     */
    @Override
    public void update(Query query, Update update) {
        logUpdate(query,update,false);
        mongoTemplate.updateMulti(query, update, this.getEntityClass());
    }

    /**
     * 通过ID获取记录
     * @param id
     * @return
     */
    @Override
    public T findById(String id) {
        Class<T> entityClass = this.getEntityClass();
        logQuery( new Query(Criteria.where("id").is(id)));
        return mongoTemplate.findById(id, entityClass);
    }

    /**
     * 通过ID获取记录,并且指定了集合名(表的意思)
     * @param id
     * @param collectionName
     * @return
     */
    @Override
    public T findById(String id, String collectionName) {
        return mongoTemplate.findById(id, this.getEntityClass(), collectionName);
    }
    /**
     * 通过条件查询,查询分页结果
     * @param query
     * @return
     */
    @Override
    public PageResponse<T> findPage(Query query) {
        PageParam pageParam = ThreadPagingUtil.get();
        PageResponse<T> page=new PageResponse();
        //如果没有条件 则所有全部
        query=query==null?new Query(Criteria.where("_id").exists(true)):query;
        Integer currentPage = pageParam.getCurrent();
        Integer pageSize = pageParam.getSize();
        long count = this.count(query);
        if(count==0){
            page.setTotal(0);
            page.setCurrent(currentPage);

            page.setHasNext(false);
            page.setHasPre(false);
            page.setSize(pageSize);
            page.setPages(0);
            return page;
        }
        // 总数
        query.skip((currentPage - 1) * pageSize).limit(Convert.toInt(pageSize));
        logQuery(query);
        long pages=count%pageSize==0?count/pageSize:count/pageSize+1;
        List<T> rows = this.find(query);

        page.setTotal((int)count);
        page.setPages((int)pages);
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        page.setRecords(rows);
        page.setHasNext(currentPage < pages);
        page.setHasPre(currentPage > 1);
        return page;
    }

    @Override
    public PageResponse<T> findPageByCondition(T t){
        Query query = buildBaseQuery(t);
        return findPage(query);
    }
    /**
     * 求数据总和
     * @param query
     * @return
     */
    @Override
    public long count(Query query){
        return mongoTemplate.count(query, this.getEntityClass());
    }

    /**
     * 根据vo构建查询条件Query
     * @param t
     * @return
     */
    private Query buildBaseQuery(T t) {
        Query query = new Query();

        Field[] fields = t.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(t);
                if (value != null) {
                    query.addCriteria(Criteria.where(field.getName()).is(value));
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return query;
    }

    /**
     * 根据vo构建更新条件Query
     * @param t
     * @return
     */
    private Update buildBaseUpdate(T t) {
        Update update = new Update();

        Field[] fields = t.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(t);
                if (value != null) {
                    update.set(field.getName(), value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return update;
    }

    /**
     * 获取需要操作的实体类class
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Class<T> getEntityClass() {
        return getSuperClassGenricType(getClass(),0);
    }

    /**
     * 获取MongoDB模板操作
     * @return
     */
    @Override
    public MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    private Class getSuperClassGenricType(final Class clazz, final int index){
        Type genType = clazz.getGenericSuperclass();

        if (!(genType instanceof ParameterizedType)) {
            log.warn(clazz.getSimpleName() + "'s superclass not ParameterizedType");
            return Object.class;
        }

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (index >= params.length || index < 0) {
            log.warn("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: "
                    + params.length);
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            log.warn(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
            return Object.class;
        }

        return (Class) params[index];
    }

    /**
     * 打印查询语句
     *
     * @param query
     */
    private void logQuery( Query query) {
        if (print) {
            Class<?> clazz = this.getEntityClass();
            MongoPersistentEntity<?> entity = mongoConverter.getMappingContext().getPersistentEntity(clazz);
            Document mappedQuery = queryMapper.getMappedObject(query.getQueryObject(), entity);
            Document mappedField = queryMapper.getMappedObject(query.getFieldsObject(), entity);
            Document mappedSort = queryMapper.getMappedObject(query.getSortObject(), entity);

            String logStr = "\ndb." + StrUtil.lowerFirst(clazz.getSimpleName()) + ".find(";

            logStr += FormatUtils.bson(mappedQuery.toJson()) + ")";

            if (!query.getFieldsObject().isEmpty()) {
                logStr += ".projection(";
                logStr += FormatUtils.bson(mappedField.toJson()) + ")";
            }

            if (query.isSorted()) {
                logStr += ".sort(";
                logStr += FormatUtils.bson(mappedSort.toJson()) + ")";
            }

            if (query.getLimit() != 0l) {
                logStr += ".limit(" + query.getLimit() + ")";
            }

            if (query.getSkip() != 0l) {
                logStr += ".skip(" + query.getSkip() + ")";
            }
            logStr += ";";

            log.info(logStr);
        }
    }

    /**
     * 打印查询语句
     *
     * @param query
     */
    private void logCount( Query query) {
        if (print) {
            Class<?> clazz = this.getEntityClass();
            MongoPersistentEntity<?> entity = mongoConverter.getMappingContext().getPersistentEntity(clazz);
            Document mappedQuery = queryMapper.getMappedObject(query.getQueryObject(), entity);

            String logStr = "\ndb." + StrUtil.lowerFirst(clazz.getSimpleName()) + ".find(";
            logStr += FormatUtils.bson(mappedQuery.toJson()) + ")";
            logStr += ".count();";

            log.info(logStr);
        }
    }

    /**
     * 打印查询语句
     *
     * @param query
     */
    private void logDelete(Query query) {
        if (print) {
            Class<?> clazz = this.getEntityClass();
            MongoPersistentEntity<?> entity = mongoConverter.getMappingContext().getPersistentEntity(clazz);
            Document mappedQuery = queryMapper.getMappedObject(query.getQueryObject(), entity);

            String logStr = "\ndb." + StrUtil.lowerFirst(clazz.getSimpleName()) + ".remove(";
            logStr += FormatUtils.bson(mappedQuery.toJson()) + ")";
            logStr += ";";
            log.info(logStr);
        }
    }

    /**
     * 打印查询语句
     *
     * @param query
     */
    private void logUpdate( Query query, Update update, boolean multi) {
        if (print) {
            Class<?> clazz = this.getEntityClass();
            MongoPersistentEntity<?> entity = mongoConverter.getMappingContext().getPersistentEntity(clazz);
            Document mappedQuery = queryMapper.getMappedObject(query.getQueryObject(), entity);
            Document mappedUpdate = updateMapper.getMappedObject(update.getUpdateObject(), entity);

            String logStr = "\ndb." + StrUtil.lowerFirst(clazz.getSimpleName()) + ".update(";
            logStr += FormatUtils.bson(mappedQuery.toJson()) + ",";
            logStr += FormatUtils.bson(mappedUpdate.toJson()) + ",";
            logStr += FormatUtils.bson("{multi:" + multi + "})");
            logStr += ";";
            log.info(logStr);
        }

    }

    /**
     * 打印查询语句
     *
     * @param object
     *

     */
    private void logSave(Object object) {
        if (print) {
            String logStr = "\ndb." + StrUtil.lowerFirst(object.getClass().getSimpleName()) + ".save(";
            logStr += JSONUtil.toJsonPrettyStr(object);
            logStr += ");";
            log.info(logStr);
        }
    }
}
