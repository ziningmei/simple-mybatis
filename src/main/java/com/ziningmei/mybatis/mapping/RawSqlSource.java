package com.ziningmei.mybatis.mapping;

import com.ziningmei.mybatis.builder.SqlSourceBuilder;
import com.ziningmei.mybatis.session.Configuration;

import java.util.HashMap;

public class RawSqlSource implements SqlSource {


    private final SqlSource sqlSource;

    public RawSqlSource(Configuration configuration, String sql, Class<?> parameterType) {
        SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
        Class<?> clazz = parameterType == null ? Object.class : parameterType;
        sqlSource = sqlSourceParser.parse(sql, clazz, new HashMap<>());
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return sqlSource.getBoundSql(parameterObject);
    }
}
