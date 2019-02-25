package com.ziningmei.mybatis.datasource;

import javax.sql.DataSource;
import java.util.Properties;

public class UnpooledDataSourceFactory implements DataSourceFactory {

    /**
     * 数据源
     */
    protected DataSource dataSource;

    /**
     * 构造函数
     */
    public UnpooledDataSourceFactory() {
        this.dataSource = new UnpooledDataSource();
    }

    @Override
    public void setProperties(Properties props) {

    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }
}
