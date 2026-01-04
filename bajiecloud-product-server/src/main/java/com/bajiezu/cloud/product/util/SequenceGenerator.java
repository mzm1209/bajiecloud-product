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

    /**
     *  快递模版
     */
    private static final String EXPRESS_TEMPLATE_SEQUENCE_KEY = "express_template:sequence:";
    public static final String EXPRESS_TEMPLATE_PARTNER_CODE_FORMAT = "%06d";

    /**
     * 标准商品
     */
    private static final String STANDARD_PRODUCT_SEQUENCE_KEY = "standard_product:sequence:";
    public static final String STANDARD_PRODUCT_PARTNER_CODE_FORMAT = "%06d";

    /**
     * 租赁商品
     */
    private static final String RENTAL_PRODUCT_SEQUENCE_KEY = "rental_product:sequence:";
    public static final String RENTAL_PRODUCT_PARTNER_CODE_FORMAT = "%06d";

    /**
     * 售卖商品
     */
    private static final String SALE_PRODUCT_SEQUENCE_KEY = "sale_product:sequence:";
    public static final String SALE_PRODUCT_PARTNER_CODE_FORMAT = "%06d";

    /**
     * 回收商品
     */
    private static final String RECYCLE_PRODUCT_SEQUENCE_KEY = "recycle_product:sequence:";
    public static final String RECYCLE_PRODUCT_PARTNER_CODE_FORMAT = "%06d";

    /**
     * 实物商品
     */
    private static final String PHYSICAL_PRODUCT_SEQUENCE_KEY = "physical_product:sequence:";
    public static final String PHYSICAL_PRODUCT_PARTNER_CODE_FORMAT = "%06d";

     /**
     * 虚拟商品
     */
     private static final String VIRTUAL_PRODUCT_SEQUENCE_KEY = "virtual_product:sequence:";
     public static final String VIRTUAL_PRODUCT_PARTNER_CODE_FORMAT = "%06d";

    @Resource
    private RedissonClient redissonClient;

    public String getValueAddedSequence() {
        return generateSequence(VALUE_ADD_SEQUENCE_KEY, VALUE_ADD_PARTNER_CODE_FORMAT);
    }

    public String getExpressTemplateSequence() {
        return generateSequence(EXPRESS_TEMPLATE_SEQUENCE_KEY, EXPRESS_TEMPLATE_PARTNER_CODE_FORMAT);
    }

    public String getStandardProductSequence() {
        return generateSequence(STANDARD_PRODUCT_SEQUENCE_KEY, STANDARD_PRODUCT_PARTNER_CODE_FORMAT);
    }

    public String getRentalProductSequence() {
        return generateSequence(RENTAL_PRODUCT_SEQUENCE_KEY, RENTAL_PRODUCT_PARTNER_CODE_FORMAT);
    }

    public String getSaleProductSequence() {
        return generateSequence(SALE_PRODUCT_SEQUENCE_KEY, SALE_PRODUCT_PARTNER_CODE_FORMAT);
    }

    public String getRecycleProductSequence() {
        return generateSequence(RECYCLE_PRODUCT_SEQUENCE_KEY, RECYCLE_PRODUCT_PARTNER_CODE_FORMAT);
    }

    public String getPhysicalProductSequence() {
        return generateSequence(PHYSICAL_PRODUCT_SEQUENCE_KEY, PHYSICAL_PRODUCT_PARTNER_CODE_FORMAT);
    }

    public String getVirtualProductSequence() {
        return generateSequence(VIRTUAL_PRODUCT_SEQUENCE_KEY, VIRTUAL_PRODUCT_PARTNER_CODE_FORMAT);
    }

    private String generateSequence(String sequenceKey, String codeFormat) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(sequenceKey);

        // 如果是第一次使用，设置初始值为 0
        if (!atomicLong.isExists()) {
            atomicLong.set(0);
        }

        // 获取下一个值
        long nextValue = atomicLong.incrementAndGet();

        // 格式化为指定的格式，不足前面补0
        return String.format(codeFormat, nextValue);
    }
}