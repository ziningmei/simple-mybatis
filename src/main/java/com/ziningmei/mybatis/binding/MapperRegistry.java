package com.ziningmei.mybatis.binding;

import com.ziningmei.mybatis.builder.annotation.MapperAnnotationBuilder;
import com.ziningmei.mybatis.session.Configuration;

import java.util.HashMap;
import java.util.Map;

public class MapperRegistry {

    /**
     * MapperProxyFactory 的映射
     */
    private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();

    /**
     * Configuration 对象
     */
    private final Configuration configuration;


    public MapperRegistry(Configuration configuration){
        this.configuration=configuration;
    }

    public boolean hasMapper(Class<?> boundType) {
        return knownMappers.containsKey(boundType);
    }


    public void addMapper(Class<?> type) {
        //必须是接口
        if (type.isInterface()) {
            //如果添加过报错
            if (hasMapper(type)) {
                throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
            }

            boolean loadCompleted = false;
            try {
                //添加到knownMappers
                knownMappers.put(type, new MapperProxyFactory<>(type));
                // It's important that the type is added before the parser is run
                // otherwise the binding may automatically be attempted by the
                // mapper parser. If the type is already known, it won't try.
                //解析 Mapper 的注解配置
                MapperAnnotationBuilder parser = new MapperAnnotationBuilder(configuration, type);
                parser.parse();
                //标记加载完成
                loadCompleted = true;
            } finally {
                //若加载未完成，从 knownMappers 中移除
                if (!loadCompleted) {
                    knownMappers.remove(type);
                }
            }
        }
    }
}
