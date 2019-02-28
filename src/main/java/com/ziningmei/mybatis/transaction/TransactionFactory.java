package com.ziningmei.mybatis.transaction;

import com.ziningmei.mybatis.session.TransactionIsolationLevel;

import javax.sql.DataSource;
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


    /**
     * 创建事务
     * @param ds
     * @param level
     * @param autoCommit
     * @return
     */
    Transaction newTransaction(DataSource ds, TransactionIsolationLevel level);
}
