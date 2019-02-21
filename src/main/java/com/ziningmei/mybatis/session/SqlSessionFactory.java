package com.ziningmei.mybatis.session;

import java.io.IOException;

/**
 * @author ziningmei
 *
 * 会话工厂
 */
public interface SqlSessionFactory {

    /**
     * 获取配置类
     * @return
     */
    Configuration getConfiguration();


    /**
     * 获取SqlSession
     *
     * @return
     * @throws IOException
     */
    SqlSession openSession();
}
