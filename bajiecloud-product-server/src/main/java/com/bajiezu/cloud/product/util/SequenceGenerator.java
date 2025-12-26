package com.bajiezu.cloud.product.util;

import jakarta.annotation.Resource;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
public class SequenceGenerator {

    /**
     * 增值服务
     */
    private static final String VALUE_ADD_SEQUENCE_KEY = "value_added:sequence:";
    public static final String VALUE_ADD_PARTNER_CODE_FORMAT = "%04d";

    @Resource
    private RedissonClient redissonClient;

    public String getValueAddedSequence() {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(VALUE_ADD_SEQUENCE_KEY);

        // 如果是第一次使用，设置初始值为 0
        if (!atomicLong.isExists()) {
            atomicLong.set(0);
        }

        // 获取下一个值
        long nextValue = atomicLong.incrementAndGet();

        // 格式化为指定的格式，不足前面补0
        return String.format(VALUE_ADD_PARTNER_CODE_FORMAT, nextValue);
    }
}