package com.ziningmei.mybatis.io;

import java.io.InputStream;

/**
 * @author ziningmei
 * <p>
 * 类加载器包装
 */
public class ClassLoaderWrapper {

    private ClassLoader defaultClassLoader;

    private ClassLoader systemClassLoader;


    ClassLoaderWrapper() {
        try {
            //初始化系统类加载器
            systemClassLoader = ClassLoader.getSystemClassLoader();
        } catch (SecurityException ignored) {
            // AccessControlException on Google App Engine
        }
    }

    /**
     * 通过资源获取InputStream
     *
     * @param resource
     * @param classLoader
     * @return
     */

    public InputStream getResourceAsStream(String resource, ClassLoader classLoader) {
        return getResourceAsStream(resource, getClassLoaders(classLoader));
    }

    /**
     * 通过资源获取InputStream
     *
     * @param resource
     * @param classLoader
     * @return
     */
    InputStream getResourceAsStream(String resource, ClassLoader[] classLoader) {
        //遍历ClassLoader
        for (ClassLoader cl : classLoader) {
            if (null != cl) {
                //尝试使用当前加载器加载资源
                InputStream returnValue = cl.getResourceAsStream(resource);

                //如果没找到，加个/再找一次
                if (null == returnValue) {
                    returnValue = cl.getResourceAsStream("/" + resource);
                }

                if (returnValue != null) {
                    return returnValue;
                }
            }
        }
        return null;
    }

    /**
     * 获取所有的ClassLoader
     *
     * @param classLoader
     * @return
     */
    private ClassLoader[] getClassLoaders(ClassLoader classLoader) {
        //按顺序创建加载器
        return new ClassLoader[]{
                classLoader,
                defaultClassLoader,
                Thread.currentThread().getContextClassLoader(),
                getClass().getClassLoader(),
                systemClassLoader};
    }

    public Class<?> classForName(String className)throws ClassNotFoundException {
        return classForName(className, getClassLoaders(null));
    }

    /*
     * Attempt to load a class from a group of classloaders
     * 循环遍历初始化类
     *
     * @param name        - the class to load
     * @param classLoader - the group of classloaders to examine
     * @return the class
     * @throws ClassNotFoundException - Remember the wisdom of Judge Smails: Well, the world needs ditch diggers, too.
     */
    Class<?> classForName(String name, ClassLoader[] classLoader) throws ClassNotFoundException {

        //循环遍历初始化类
        for (ClassLoader cl : classLoader) {

            if (null != cl) {

                try {

                    Class<?> c = Class.forName(name, true, cl);

                    if (null != c) {
                        return c;
                    }

                } catch (ClassNotFoundException e) {
                    // we'll ignore this until all classloaders fail to locate the class
                }

            }

        }

        throw new ClassNotFoundException("Cannot find class: " + name);
    }
}
