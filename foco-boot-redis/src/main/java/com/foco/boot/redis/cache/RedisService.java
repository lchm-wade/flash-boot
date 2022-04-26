package com.foco.boot.redis.cache;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * description----------
 *
 * @Author lucoo
 * @Date 2021/6/26 13:59
 */
public interface RedisService {
    int removePrefix(String prefix);

    int remove(String... keys);

    boolean remove(String key);

    boolean exists(String key);

    String get(String key);

    <T> T getObject(String key, Class<T> type);

    <T> List<T> getArray(String key, Class<T> clazz);

    String get(String key, String defaultValue);

    boolean set(String key, List<Object> value);

    boolean setNX(String key, Object value);
    boolean setNX(String key, Object value,long expireTime);
    boolean setNX(String key, Object value,long expireTime,TimeUnit timeUnit);



    boolean setNull(String key);

    boolean set(String key, Object value);

    boolean set(String key, Object value, long expireTime);
    boolean set(String key, Object value, long expireTime, TimeUnit timeUnit);

    Set<String> keys(String key);

    long getExpire(String key);

    boolean expire(String key, long time, TimeUnit timeUnit);

    boolean expireAt(String key, Date date);

    boolean multiSet(Map<String, String> maps);

    boolean multiSetObject(Map<String, Object> maps);

    Long incr(String key);


    Long incr(String key, Long incValue);

    <T> T leftPop(String key, Class<T> type);

    Long leftPush(String key, Object val);

    Long leftPushAll(String key, Object[] val);

    Long leftPushAll(String key, List<Object> list);

    Long leftPush(String key, Object val, int maxSize);
    List<Object> getListRange(String key,long min,long max);
    Long size(String key);
    /**
     * 获取存储在哈希表中指定字段的值
     *
     * @param key
     * @param field
     * @return
     */
    Object hGet(String key, String field);
    Long hSize(String key);

    /**
     * 获取所有给定字段的值
     *
     * @param key
     * @return
     */
    Map<Object, Object> hGetAll(String key);

    /**
     * 获取所有给定字段的值
     *
     * @param key
     * @param fields
     * @return
     */
    List<Object> hMultiGet(String key, Collection<Object> fields);

    boolean hPut(String key, String hashKey, Object value);

    boolean hPutAll(String key, Map<String, String> maps);

    /**
     * 删除一个或多个哈希表字段
     *
     * @param key
     * @param fields
     * @return
     */
    Long hDelete(String key, Object... fields);

    /**
     * 查看哈希表 key 中，指定的字段是否存在
     *
     * @param key
     * @param field
     * @return
     */
    boolean hExists(String key, String field);


    boolean addSet(String key, Object value);

    boolean removeSet(String key, Object... value);

    Set<Object> getAllSet(String key);

    /**
     * 添加ZSet类型数据
     *
     * @param key
     * @param value
     * @param score
     * @return
     */
    Boolean addZSet(String key, Object value, double score);
    Long removeZSet(String key,Object... value);
    /**
     * ZSet数据增加分数
     *
     * @param key
     * @param value
     * @param delta
     * @return
     */
    Double incrZSet(String key, Object value, double delta);

    /**
     * ZSet数据增加分数(默认增加1)
     *
     * @param key
     * @param value
     * @return
     */
    Double incrZSet(String key, Object value);

    /**
     * 获取Zset 指定分数内数据集
     * 降序排序
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    Set<Object> zSetReverseRangeByScore(String key, double min, double max);

    /**
     * 获取Zset 指定分数内数据集
     * 升序排序
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    Set<Object> zSetRangeByScore(String key, double min, double max);

}
