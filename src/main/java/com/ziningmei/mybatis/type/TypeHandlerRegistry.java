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
package com.ziningmei.mybatis.type;

import com.ziningmei.mybatis.binding.MapperMethod;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Clinton Begin
 * @author Kazuki Shimizu
 * <p>
 * 类型处理注册器
 */
public final class TypeHandlerRegistry {

    /**
     * {@link TypeHandler} 的映射
     */
    private final Map<Type, Map<JdbcType, TypeHandler<?>>> TYPE_HANDLER_MAP = new ConcurrentHashMap<>();

    /**
     * 空类型
     */
    private static final Map<JdbcType, TypeHandler<?>> NULL_TYPE_HANDLER_MAP = Collections.emptyMap();


    /**
     * JDBC Type 和 {@link TypeHandler} 的映射
     */
    private final Map<JdbcType, TypeHandler<?>> JDBC_TYPE_HANDLER_MAP = new EnumMap<>(JdbcType.class);


    private final TypeHandler<Object> UNKNOWN_TYPE_HANDLER = new UnknownTypeHandler(this);


    /**
     * 所有类型处理对象
     */
    private final Map<Class<?>, TypeHandler<?>> ALL_TYPE_HANDLERS_MAP = new HashMap<>();

    public TypeHandlerRegistry()

    {
        register(Integer.class, new IntegerTypeHandler());
        register(int.class, new IntegerTypeHandler());
        register(JdbcType.INTEGER, new IntegerTypeHandler());
    }

    public void register(JdbcType jdbcType, TypeHandler<?> handler) {
        JDBC_TYPE_HANDLER_MAP.put(jdbcType, handler);
    }

    private <T> void register(Type javaType, TypeHandler<? extends T> typeHandler) {

        register(javaType, null, typeHandler);

    }

    /**
     * 注册
     * @param javaType
     * @param jdbcType
     * @param handler
     */
    private void register(Type javaType, JdbcType jdbcType, TypeHandler<?> handler) {
        //如果javaType 为空，添加到TYPE_HANDLER_MAP
        if (javaType != null) {
            Map<JdbcType, TypeHandler<?>> map = TYPE_HANDLER_MAP.get(javaType);
            if (map == null || map == NULL_TYPE_HANDLER_MAP) {
                map = new HashMap<>();
                TYPE_HANDLER_MAP.put(javaType, map);
            }
            map.put(jdbcType, handler);
        }
        //添加handler 到ALL_TYPE_HANDLERS_MAP
        ALL_TYPE_HANDLERS_MAP.put(handler.getClass(), handler);
    }

    /**
     * 根据类和jdbcType获取handler
     *
     * @param type
     * @param jdbcType
     * @param <T>
     * @return
     */
    public  <T> TypeHandler<T> getTypeHandler(Type type, JdbcType jdbcType) {
        // 忽略 ParamMap 的情况
        if (MapperMethod.ParamMap.class.equals(type)) {
            return null;
        }
        // 获得 Java Type 对应的 TypeHandler 集合
        Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = getJdbcHandlerMap(type);
        TypeHandler<?> handler = null;
        if (jdbcHandlerMap != null) {
            // 使用 jdbcType 获取对应的 TypeHandler
            handler = jdbcHandlerMap.get(jdbcType);
            if (handler == null) {

                //使用 null 获取对应的 TypeHandler
                handler = jdbcHandlerMap.get(null);

            }
            //从 TypeHandler 集合中选择一个唯一的 TypeHandler
            if (handler == null) {
                // #591
                handler = pickSoleHandler(jdbcHandlerMap);
            }
        }
        // type drives generics here
        return (TypeHandler) handler;

    }

    private TypeHandler<?> pickSoleHandler(Map<JdbcType, TypeHandler<?>> jdbcHandlerMap) {
        TypeHandler<?> soleHandler = null;
        for (TypeHandler<?> handler : jdbcHandlerMap.values()) {
            if (soleHandler == null) {
                soleHandler = handler;
            } else if (!handler.getClass().equals(soleHandler.getClass())) {
                // More than one type handlers registered.
                return null;
            }
        }
        return soleHandler;
    }

    private Map<JdbcType, TypeHandler<?>> getJdbcHandlerMap(Type type) {

        //获得 Java Type 对应的 TypeHandler 集合
        Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = TYPE_HANDLER_MAP.get(type);
        //如果为 NULL_TYPE_HANDLER_MAP ，意味着为空，直接返回
        if (NULL_TYPE_HANDLER_MAP.equals(jdbcHandlerMap)) {

            return null;
        }

        return jdbcHandlerMap;
    }

    public boolean hasTypeHandler(Class<?> javaType) {
        return hasTypeHandler(javaType, null);
    }

    public boolean hasTypeHandler(Class<?> javaType, JdbcType jdbcType) {
        return javaType != null && getTypeHandler((Type) javaType, jdbcType) != null;
    }

    public TypeHandler<?> getMappingTypeHandler(Class<? extends TypeHandler<?>> handlerType) {
        return ALL_TYPE_HANDLERS_MAP.get(handlerType);
    }

    public <T> TypeHandler<T> getInstance(Class<?> javaTypeClass, Class<?> typeHandlerClass) {
        if (javaTypeClass != null) {
            try {
                Constructor<?> c = typeHandlerClass.getConstructor(Class.class);
                return (TypeHandler<T>) c.newInstance(javaTypeClass);
            } catch (NoSuchMethodException ignored) {
                // ignored
            } catch (Exception e) {
                throw new TypeException("Failed invoking constructor for handler " + typeHandlerClass, e);
            }
        }
        try {
            Constructor<?> c = typeHandlerClass.getConstructor();
            return (TypeHandler<T>) c.newInstance();
        } catch (Exception e) {
            throw new TypeException("Unable to find a usable constructor for " + typeHandlerClass, e);
        }
    }

    public TypeHandler<Object> getUnknownTypeHandler() {
        return UNKNOWN_TYPE_HANDLER;
    }


}
