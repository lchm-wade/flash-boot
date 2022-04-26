package com.foco.boot.redis.support;

import cn.hutool.core.util.StrUtil;
import com.foco.context.common.RedisPrefix;
import com.foco.context.util.SerialNumberHelper;
import com.foco.context.util.SerialNumberUtil;
import com.foco.model.constant.FocoErrorCode;
import com.foco.model.constant.TimePattern;
import com.foco.model.exception.SystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * @author lucoo
 * @version 1.0.0
 * @Description TODO
 * @date 2021-07-23 17:45
 */
public class RedisSerialNumber implements SerialNumberHelper {
    @Autowired
    private RedisTemplate redisTemplate;
    private String commonPrefix = "serialNumber:";
    @Autowired
    private DefaultRedisScript redisIncr;
    private static int DEFAULT_COUNT = 8;
    private static int HOUR = 3600;
    private static int DAY = HOUR * 24;
    private static int MONTH = DAY * 31;
    @Override
    public String generateByHour(String prefix) {
        return generateByHour(prefix, DEFAULT_COUNT);
    }

    @Override
    public String generateByHour(String prefix, int count) {
        return generate(prefix, TimePattern.HOUR.getValue(), count, HOUR);
    }

    @Override
    public String generateFullByHour(String prefix) {
        return generateFullByHour(prefix, DEFAULT_COUNT);
    }

    @Override
    public String generateFullByHour(String prefix, int count) {
        return generate(prefix, TimePattern.FULL_HOUR.getValue(), count, HOUR);
    }

    @Override
    public String generateByDay(String prefix) {
        return generateByDay(prefix, DEFAULT_COUNT);
    }


    @Override
    public String generateByDay(String prefix, int count) {
        return generate(prefix, TimePattern.DAY.getValue(), count, DAY);
    }

    @Override
    public String generateFullByDay(String prefix) {
        return generateFullByDay(prefix,DEFAULT_COUNT);
    }

    @Override
    public String generateFullByDay(String prefix, int count) {
        return generate(prefix, TimePattern.FULL_DAY.getValue(), count, DAY);
    }

    @Override
    public String generateByMonth(String prefix) {
        return generateByMonth(prefix,DEFAULT_COUNT);
    }

    @Override
    public String generateByMonth(String prefix, int count) {
        return generate(prefix, TimePattern.MONTH.getValue(), count, MONTH);
    }

    @Override
    public String generateFullByMonth(String prefix) {
        return generateFullByMonth(prefix,DEFAULT_COUNT);
    }

    @Override
    public String generateFullByMonth(String prefix, int count) {
        return generate(prefix, TimePattern.FULL_MONTH.getValue(), count, MONTH);
    }

    private String generate(String prefix, String pattern, int count, int expire) {
        if (StrUtil.isBlank(pattern) || count < 0 || expire < 0) {
            SystemException.throwException(FocoErrorCode.PARAMS_VALID);
        }
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
        List<String> keys = Collections.singletonList(new StringBuilder()
                .append(commonPrefix)
                .append(prefix)
                .append(time)
                .toString());
        long result = (long) redisTemplate.execute(redisIncr, keys, expire + 600);
        return new StringBuilder().append(prefix)
                .append(time)
                .append(SerialNumberUtil.splice(result, count))
                .toString();
    }

    @Override
    public String generateNumber(String prefix, int count) {
        if (StrUtil.isBlank(prefix) || count < 0) {
            SystemException.throwException(FocoErrorCode.PARAMS_VALID);
        }
        long value = redisTemplate.opsForValue().increment( commonPrefix + prefix, 1);
        return new StringBuilder().append(prefix)
                .append(SerialNumberUtil.splice(value, count))
                .toString();
    }
}
