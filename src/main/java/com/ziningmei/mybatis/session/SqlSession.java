package com.ziningmei.mybatis.session;

import java.io.Closeable;

/**
 * @author ziningmei
 *
 * 会话工厂
 */
public interface SqlSession extends Closeable  {

    /**
     * 重写，不强制抛出错误
     */
    @Override
    void close();

    /**
     * 获取mapper，使用范型
     * @param type
     * @param <T>
     * @return
     */
    <T> T getMapper(Class<T> type);
}
