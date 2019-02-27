package com.ziningmei.mybatis.session;

import com.ziningmei.mybatis.builder.xml.XMLConfigBuilder;
import com.ziningmei.mybatis.exception.ExceptionFactory;

import java.io.IOException;
import java.io.Reader;

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
            // 根据configuration返回默认的sqlsession工厂
            return new DefaultSqlSessionFactory(parser.parse());
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


}
