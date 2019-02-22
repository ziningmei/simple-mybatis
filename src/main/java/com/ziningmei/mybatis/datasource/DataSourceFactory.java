package com.ziningmei.mybatis.datasource;

import javax.sql.DataSource;
import java.util.Properties;

public interface DataSourceFactory {

    /**
     * 设置属性
     *
     * @param props
     */
    void setProperties(Properties props);

    /**
     * 获取数据源
     *
     * @return
     */
    DataSource getDataSource();

}
