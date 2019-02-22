package com.ziningmei.mybatis.transaction;

import java.util.Properties;

/**
 * 事务工厂
 */
public interface TransactionFactory {

    /**
     * Sets transaction factory custom properties.
     * 设置属性
     * @param props
     */
    void setProperties(Properties props);



}
