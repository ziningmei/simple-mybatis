package com.ziningmei.mybatis.session;

import java.io.Closeable;
import java.util.List;

/**
 * @author ziningmei
 *
 * 会话工厂
 */
public interface SqlSession extends Closeable  {

    /**
     * Retrieve a single row mapped from the statement key and parameter.
     *
     * 通过声明和参数获取结构，调用selectList，返回第一个
     * @param <T> the returned object type
     * @param statement Unique identifier matching the statement to use.
     * @param parameter A parameter object to pass to the statement.
     * @return Mapped object
     */
    <T> T selectOne(String statement, Object parameter);

    /**
     * Retrieve a list of mapped objects from the statement key and parameter.
     *
     * 通过声明和参数获取结果
     * @param <E> the returned list element type
     * @param statement Unique identifier matching the statement to use.
     * @param parameter A parameter object to pass to the statement.
     * @return List of mapped object
     */
    <E> List<E> selectList(String statement, Object parameter);

    /**
     * 返回Configuration
     * @return
     */
    Configuration getConfiguration();

    /**
     * 获取一个mapper
     *
     * @param <T> the mapper type
     * @param type Mapper interface class
     * @return a mapper bound to this SqlSession
     */
    <T> T getMapper(Class<T> type);

    /**
     * Closes the session
     */
    @Override
    void close();

}
