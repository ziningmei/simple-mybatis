package com.ziningmei.mybatis.session;

import com.ziningmei.mybatis.transaction.TransactionFactory;

import javax.sql.DataSource;

/**
 * @author ziningmei
 *
 * 环境
 */
public final class Environment {

    /**
     * 数据源
     */
    private final DataSource dataSource;

    /**
     * 事务工厂
     */
    private final TransactionFactory transactionFactory;

    /**
     * id
     */
    private final String id;

    /**
     * 构造函数
     * @param id
     * @param transactionFactory
     * @param dataSource
     */
    public Environment(String id, TransactionFactory transactionFactory, DataSource dataSource) {
        if (id == null) {
            throw new IllegalArgumentException("Parameter 'id' must not be null");
        }
        if (transactionFactory == null) {
            throw new IllegalArgumentException("Parameter 'transactionFactory' must not be null");
        }
        this.id = id;
        if (dataSource == null) {
            throw new IllegalArgumentException("Parameter 'dataSource' must not be null");
        }
        this.transactionFactory = transactionFactory;
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    /**
     * 静态内部类
     */
    public static class Builder {
        private String id;
        private TransactionFactory transactionFactory;
        private DataSource dataSource;

        public Builder(String id) {
            this.id = id;
        }

        public Builder transactionFactory(TransactionFactory transactionFactory) {
            this.transactionFactory = transactionFactory;
            return this;
        }

        public Builder dataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        public String id() {
            return this.id;
        }

        public Environment build() {

            return new Environment(this.id, this.transactionFactory, this.dataSource);

        }
    }

    public TransactionFactory getTransactionFactory() {
        return transactionFactory;
    }

    public String getId() {
        return id;
    }
}

