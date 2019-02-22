package com.ziningmei.mybatis.session;

import com.ziningmei.mybatis.builder.xml.XMLConfigBuilder;
import com.ziningmei.mybatis.exception.ExceptionFactory;
import com.ziningmei.mybatis.exception.PersistenceException;

import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

/**
 * @author ziningmei
 */
public class SqlSessionFactoryBuilder {

    /**
     * 根据reader获取SqlSessionFactory
     *
     * @param reader
     * @return
     */
    public SqlSessionFactory build(Reader reader) {
        try {
            // 解析XMLConfigBuilder
            XMLConfigBuilder parser = new XMLConfigBuilder(reader);
            // 解析
            return build(parser.parse());
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error building SqlSession.", e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }

    /**
     * 根据reader获取SqlSessionFactory
     *
     * @param configuration
     * @return
     */
    public SqlSessionFactory build(Configuration configuration) {
        return new DefaultSqlSessionFactory(configuration);
    }
}
