/**
 * Copyright 2009-2018 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ziningmei.mybatis.session;


import com.ziningmei.mybatis.exception.ExceptionFactory;
import com.ziningmei.mybatis.exception.TooManyResultsException;
import com.ziningmei.mybatis.executor.ErrorContext;
import com.ziningmei.mybatis.executor.Executor;
import com.ziningmei.mybatis.mapping.MappedStatement;

import java.util.List;

/**
 * The default implementation for {@link SqlSession}.
 * Note that this class is not Thread-Safe.
 * <p>
 * 默认sql对话，非线程安全
 *
 * @author Clinton Begin
 */
public class DefaultSqlSession implements SqlSession {

    /**
     * 配置属性
     */
    private final Configuration configuration;
    /**
     * 执行器
     */
    private final Executor executor;


    public DefaultSqlSession(Configuration configuration, Executor executor) {
        this.configuration = configuration;
        this.executor = executor;
    }


    @Override
    public <T> T selectOne(String statement, Object parameter) {
        // Popular vote was to return null on 0 results and throw exception on too many.
        List<T> list = this.selectList(statement, parameter);
        if (list.size() == 1) {
            return list.get(0);
        } else if (list.size() > 1) {
            throw new TooManyResultsException("Expected one result (or null) to be returned by selectOne(), but found: " + list.size());
        } else {
            return null;
        }
    }


    @Override
    public <E> List<E> selectList(String statement, Object parameter) {
        try {
            MappedStatement ms = configuration.getMappedStatement(statement);
            return executor.query(ms, parameter, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error querying database.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    @Override
    public void close() {
        executor.close(true);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public <T> T getMapper(Class<T> type) {
        //configuration 获取mapper 代理
        return configuration.getMapper(type, this);
    }
}
