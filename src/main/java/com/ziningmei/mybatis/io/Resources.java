package com.ziningmei.mybatis.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Calendar;

/**
 * 资源加载
 */
public class Resources {

    /**
     * 类加载器包装
     */
    private static ClassLoaderWrapper classLoaderWrapper = new ClassLoaderWrapper();


    /**
     * 通过资源名称加载资源
     *
     * @param resource
     * @return
     * @throws IOException
     */
    public static Reader getResourceAsReader(String resource) throws IOException {

        Reader reader = new InputStreamReader(getResourceAsStream(resource));
        return reader;
    }


    /**
     * 获取inputstream
     *
     * @param resource
     * @return
     * @throws IOException
     */
    public static InputStream getResourceAsStream(String resource) throws IOException {

        return getResourceAsStream(null, resource);
    }

    /**
     * 获取inputstream
     *
     * @param resource
     * @return
     * @throws IOException
     */
    public static InputStream getResourceAsStream(ClassLoader classLoader, String resource) throws IOException {

        InputStream in = classLoaderWrapper.getResourceAsStream(resource, classLoader);

        if (in == null) {
            throw new IOException("Could not find resource " + resource);
        }

        return in;
    }

    /*
     * Loads a class
     * 获取类
     *
     * @param className - the class to fetch
     * @return The loaded class
     * @throws ClassNotFoundException If the class cannot be found (duh!)
     */
    public static Class<?> classForName(String className) throws ClassNotFoundException {
        return classLoaderWrapper.classForName(className);
    }
}
