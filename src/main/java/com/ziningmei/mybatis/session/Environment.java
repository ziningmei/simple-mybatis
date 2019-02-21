package com.ziningmei.mybatis.session;

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

    public Environment(DataSource dataSource) {

        if (dataSource == null) {
            throw new IllegalArgumentException("Parameter 'dataSource' must not be null");
        }

        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }
}
