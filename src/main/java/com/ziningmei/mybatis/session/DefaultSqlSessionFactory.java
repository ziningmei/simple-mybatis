package com.ziningmei.mybatis.session;

/**
 * @author ziningmei
 *
 * 默认DefaultSqlSessionFactory
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {

    /**
     * 核心配置类
     */
    private final Configuration configuration;


    public DefaultSqlSessionFactory(Configuration configuration) {

        this.configuration = configuration;

    }


    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public SqlSession openSession() {
        return null;
    }
}
