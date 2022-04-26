package com.foco.boot.redis.cache;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foco.context.common.RedisPrefix;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * description----------
 * @Author lucoo
 * @Date 2021/6/26 13:59
 */
@Slf4j
public class DefaultRedisService implements RedisService {

    private final ObjectMapper mapper;
    /**
     * key 最大长度
     */
    private static final int MAXLENGTH = 20;

    /***针对空值的缓存设置一个过期时间***/
    @Value("${cache.nullValueExpire:10}")
    private long nullValueExpire;
    private RedisTemplate<String, Object> redisTemplate;
    private RedisPrefix redisPrefix;

    public DefaultRedisService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper,RedisPrefix redisPrefix) {
        this.redisTemplate = redisTemplate;
        this.mapper = objectMapper;
        this.redisPrefix=redisPrefix;
    }

    public long getNullValueExpire() {
        return nullValueExpire;
    }
    public Set<String> scan(String matchKey) {
        Set<String> keys = redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keysTmp = new HashSet<>();
            Cursor<byte[]> cursor = connection.scan(new ScanOptions.ScanOptionsBuilder().match(matchKey + "*").count(1000).build());
            while (cursor.hasNext()) {
                keysTmp.add(new String(cursor.next()));
            }
            return keysTmp;
        });
        return keys;
    }
    /**
     * 删除指定前缀的所有数据
     *
     * @param prefix 前缀
     * @return 成功删除的数据条数
     */
    @Override
    public int removePrefix(String prefix) {
        Set<String> keys = scan(redisPrefix.getPrefix()+prefix);
        int i=0;
        for(String key:keys){
            boolean success=redisTemplate.delete(key.substring(redisPrefix.getPrefix().length()));
            if(success){
                i++;
            }
        }
        log.info("按照前缀删除,影响条数:{}",i);
        return i;
    }
    /**
     * 批量删除对应的value
     *
     * @param keys
     */
    @Override
    public int remove(final String... keys) {
        int i=0;
        for (String key : keys) {
            boolean success=redisTemplate.delete(key);
            if(success){
                i++;
            }
        }
        log.info("按照前缀删除,影响条数:{}",i);
        return i;
    }

    /**
     * 删除对应的value
     *
     * @param key
     * @return true=操作成功，false=操作失败
     */
    @Override
    public boolean remove(final String key) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        boolean isSuccess;
        try {
            isSuccess=redisTemplate.delete(key);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return isSuccess;
    }

    /**
     * 判断缓存中是否有对应的value
     */
    @Override
    public boolean exists(final String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 读取缓存
     */
    @Override
    @SuppressWarnings("all")
    public String get(final String key) {
        Object result;
        try {
            result = redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
        return result == null ? null : String.valueOf(result);
    }

    @Override
    public <T> T getObject(String key, Class<T> type) {
        Object result;
        result = redisTemplate.opsForValue().get(key);
        if (result == null) {
            return null;
        }
        try {
            return this.mapper.readValue(this.mapper.writeValueAsBytes(result), type);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
    @Override
    public <T> List<T> getArray(String key, Class<T> clazz) {
        Object result;
        result = redisTemplate.opsForValue().get(key);
        if (result == null) {
            return null;
        }
        JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, clazz);
        try {
            return this.mapper.readValue(this.mapper.writeValueAsBytes(result), javaType);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public String get(final String key, String defaultValue) {
        Object result;
        try {
            result = redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return defaultValue;
        }
        return String.valueOf(Optional.ofNullable(result).orElse(defaultValue));
    }

    @Override
    public boolean set(String key, List<Object> value) {
        if (StrUtil.isBlank(key)|| CollectionUtil.isEmpty(value)) {
            return false;
        }
        try {
            Long ret = redisTemplate.opsForList().rightPushAll(key, value);
            return ret != null && ret > 0;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean setNX(String key, Object value) {
        if(StrUtil.isBlank(key)){
            return false;
        }
        boolean isSuccess;
        try {
            isSuccess = redisTemplate.opsForValue().setIfAbsent(key, value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return isSuccess;
    }
    @Override
    public boolean setNX(String key, Object value,long expireTime) {
       return setNX(key,value,expireTime,TimeUnit.SECONDS);
    }
    @Override
    public boolean setNX(String key, Object value, long expireTime, TimeUnit timeUnit) {
        if(StrUtil.isBlank(key)){
            return false;
        }
        boolean isSuccess;
        try {
            isSuccess = redisTemplate.opsForValue().setIfAbsent(key, value,expireTime,timeUnit);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return isSuccess;
    }

    /**
     * 给缓存key设置一个null值，并设置失效时间，预防缓存击穿
     */
    @Override
    public boolean setNull(String key) {
        if (!StringUtils.isEmpty(key)) {
            try {
                redisTemplate.opsForValue().set(key, "", this.nullValueExpire, TimeUnit.MINUTES);
                return true;
            }catch (Exception e){
                log.error(e.getMessage(), e);
            }
        }
        return false;
    }
    /**
     * 设置缓存
     *
     * @param key
     * @param value
     */
    @Override
    public boolean set(String key, Object value) {
        if (!StringUtils.isEmpty(key)) {
            try {
                redisTemplate.opsForValue().set(key, value);
                return true;
            }catch (Exception e){
                log.error(e.getMessage(), e);
            }
        }
        return false;
    }
    /**
     * 写入缓存
     *
     * @param expireTime 单位秒
     * @return true=操作成功，false=操作失败
     */
    @Override
    public boolean set(final String key, Object value, long expireTime) {
        return set(key,value,expireTime,TimeUnit.SECONDS);
    }
    /**
     * 设置缓存
     *
     * @param key
     * @param value
     * @param time     超时时间
     * @param timeUnit 单位
     */
    @Override
    public boolean set(String key, Object value, long time, TimeUnit timeUnit) {
        if (!StringUtils.isEmpty(key)) {
            try {
                redisTemplate.opsForValue().set(key, value, time, timeUnit);
                return true;
            }catch (Exception e){
                log.error(e.getMessage(), e);
            }
        }
        return false;
    }


    @Override
    @Deprecated
    public Set<String> keys(String key) {
        if (!StringUtils.isEmpty(key)) {
            return redisTemplate.keys(key);
        }
        return new HashSet<>();
    }

    @Override
    public long getExpire(String key) {
        if (!StringUtils.isEmpty(key)) {
            return redisTemplate.getExpire(key);
        }
        return 0;
    }

    @Override
    public boolean expire(String key, long time, TimeUnit timeUnit) {
        if (!StringUtils.isEmpty(key)) {
            return redisTemplate.expire(key, time, timeUnit);
        }
        return false;
    }

    @Override
    public boolean expireAt(String key, Date date) {
        if(StringUtils.isEmpty(key) || null == date){
            return false;
        }
        return redisTemplate.expireAt(key,date);
    }

    /**
     * 批量添加
     *
     * @param maps
     */
    @Override
    public boolean multiSet(Map<String, String> maps) {
        if (maps != null && !maps.isEmpty()) {
            return false;
        }
        try {
            redisTemplate.opsForValue().multiSet(maps);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 批量添加
     *
     * @param maps
     */
    @Override
    public boolean multiSetObject(Map<String, Object> maps) {
        if (maps != null && !maps.isEmpty()) {
            try {
                redisTemplate.opsForValue().multiSet(maps);
                return true;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return false;
    }

    @Override
    public Long incr(String key) {
        return redisTemplate.opsForValue().increment(key, 1);
    }
    @Override
    public Long incr(String key,Long incrValue) {
        return redisTemplate.opsForValue().increment(key, incrValue);
    }

    @Override
    public <T> T leftPop(String key, Class<T> type) {
        Object result;
        result = redisTemplate.opsForList().leftPop(key);
        if (result == null) {
            return null;
        }
        String expectClass = type.toString();
        String actualClass = result.getClass().toString();
        if (!expectClass.equals(actualClass)) {
            log.error("class not match.expect class:" + expectClass + ",actualClass:" + actualClass);
            return null;
        }
        try {
            return this.mapper.readValue(this.mapper.writeValueAsBytes(result), type);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
    @Override
    public Long leftPush(String key, Object val) {
        return redisTemplate.opsForList().leftPush(key,val);
    }
    @Override
    public Long leftPushAll(String key, Object[] val) {
        return redisTemplate.opsForList().leftPushAll(key,val);
    }
    @Override
    public Long leftPushAll(String key, List<Object> list) {
        return redisTemplate.opsForList().leftPushAll(key,list);
    }
    @Override
    public Long leftPush(String key, Object val,int maxSize) {
        Long size= redisTemplate.opsForList().leftPush(key,val);
        if(size==null){
            return null;
        }
        if(size>=maxSize){
            //移除最后一个元素
            redisTemplate.opsForList().leftPop(key);
        }
        return size;
    }
    @Override
    public List<Object> getListRange(String key,long min,long max){
        return redisTemplate.opsForList().range(key,min,max);
    }

    @Override
    public Long size(String key) {
        return redisTemplate.opsForList().size(key);
    }

    /**
     * 获取存储在哈希表中指定字段的值
     *
     * @param key
     * @param field
     * @return
     */
    @Override
    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    @Override
    public Long hSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    /**
     * 获取所有给定字段的值
     *
     * @param key
     * @return
     */
    @Override
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 获取所有给定字段的值
     *
     * @param key
     * @param fields
     * @return
     */
    @Override
    public List<Object> hMultiGet(String key, Collection<Object> fields) {
        return redisTemplate.opsForHash().multiGet(key, fields);
    }
    @Override
    public boolean hPut(String key, String hashKey, Object value) {
        if(StrUtil.isBlank(key)||
                StrUtil.isBlank(hashKey)){
            return false;
        }
        try {
            redisTemplate.opsForHash().put(key, hashKey, value);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }
    @Override
    public boolean hPutAll(String key, Map<String, String> maps) {
        if(StrUtil.isBlank(key)){
            return false;
        }
        try {
            redisTemplate.opsForHash().putAll(key, maps);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 删除一个或多个哈希表字段
     *
     * @param key
     * @param fields
     * @return
     */
    @Override
    public Long hDelete(String key, Object... fields) {
        return redisTemplate.opsForHash().delete(key, fields);
    }

    /**
     * 查看哈希表 key 中，指定的字段是否存在
     *
     * @param key
     * @param field
     * @return
     */
    @Override
    public boolean hExists(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    @Override
    public boolean addSet(String key, Object value) {
        if (value == null||value.toString().isEmpty()) {
            return false;
        }
        try {
            Long ret=redisTemplate.opsForSet().add(key, value);
            return ret > 0;
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return false;
    }
    @Override
    public boolean removeSet(String key, Object... value) {
        if (value == null||value.toString().isEmpty()) {
            return false;
        }
        try {
            Long ret=redisTemplate.opsForSet().remove(key,value);
            return ret > 0;
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return false;
    }
    @Override
    public Set<Object> getAllSet(String key) {

        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return new HashSet<>();

    }

    @Override
    public Boolean addZSet(String key, Object value, double score) {
        if (value == null || value.toString().isEmpty()) {
            return false;
        }
        try {
            return redisTemplate.opsForZSet().add(key, value, score);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public Long removeZSet(String key, Object... value) {
        return redisTemplate.opsForSet().remove(key,value);
    }
    @Override
    public Double incrZSet(String key, Object value, double delta) {
        return redisTemplate.opsForZSet().incrementScore(key, value, delta);
    }

    @Override
    public Double incrZSet(String key, Object value) {
        return redisTemplate.opsForZSet().incrementScore(key, value, 1);
    }

    @Override
    public Set<Object> zSetReverseRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().reverseRangeByScore(key, min, max);
    }

    @Override
    public Set<Object> zSetRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }
}
