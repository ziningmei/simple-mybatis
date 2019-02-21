package com.ziningmei.mybatis.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

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

        InputStream in = classLoaderWrapper.getResourceAsStream(resource, (ClassLoader) null);

        if (in == null) {
            throw new IOException("Could not find resource " + resource);
        }

        Reader reader = new InputStreamReader(in);
        return reader;
    }


}
