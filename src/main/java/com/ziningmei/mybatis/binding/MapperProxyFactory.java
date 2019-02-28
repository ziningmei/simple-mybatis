package com.ziningmei.mybatis.binding;

import com.ziningmei.mybatis.session.SqlSession;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * mapper代理工厂
 */
public class MapperProxyFactory<T> {

    /**
     * mapper接口
     */
    private final Class<T> mapperInterface;

    /**
     * 方法缓存
     */
    private final Map<Method, MapperMethod> methodCache = new ConcurrentHashMap<>();


    /**
     * 通过接口 初始化MapperProxyFactory
     * @param mapperInterface
     */
    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public T newInstance(SqlSession sqlSession) {
        final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface, methodCache);

        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, mapperProxy);
    }

}
