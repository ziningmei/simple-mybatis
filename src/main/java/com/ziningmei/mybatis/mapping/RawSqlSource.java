package com.ziningmei.mybatis.mapping;

import com.ziningmei.mybatis.builder.SqlSourceBuilder;
import com.ziningmei.mybatis.session.Configuration;

/**
 * 原始sqlsource
 */
public class RawSqlSource implements SqlSource {


    private final SqlSource sqlSource;

    /**
     * 构造函数
     * @param configuration
     * @param sql
     * @param parameterType
     */
    public RawSqlSource(Configuration configuration, String sql, Class<?> parameterType) {
        // SqlSource构造者
        SqlSourceBuilder sqlSourceBuilder = new SqlSourceBuilder(configuration);
        //参数为空则用Object
        Class<?> clazz = parameterType == null ? Object.class : parameterType;
        //创建sqlSource
        sqlSource = sqlSourceBuilder.build(sql, clazz);
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return sqlSource.getBoundSql(parameterObject);
    }
}
