/**
 * Copyright 2009-2016 the original author or authors.
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
package com.ziningmei.mybatis.mapping;


import com.ziningmei.mybatis.session.Configuration;
import com.ziningmei.mybatis.type.JdbcType;
import com.ziningmei.mybatis.type.TypeHandler;
import com.ziningmei.mybatis.type.TypeHandlerRegistry;

/**
 * @author Clinton Begin
 * <p>
 * 参数映射
 */
public class ParameterMapping {

    /**
     * 配置，一般都有这个，工具类
     */
    private Configuration configuration;

    /**
     * 属性名称
     */
    private String property;

    /**
     * 参数类型
     */
    private ParameterMode mode;

    /**
     * java类型
     */
    private Class<?> javaType = Object.class;

    /**
     * jdbcType
     */
    private JdbcType jdbcType;

    /**
     * 类型处理器
     */
    private TypeHandler<?> typeHandler;


    private ParameterMapping() {
    }

    public static class Builder {
        private ParameterMapping parameterMapping = new ParameterMapping();

        public Builder(Configuration configuration, String property, Class<?> javaType) {
            parameterMapping.configuration = configuration;
            parameterMapping.property = property;
            parameterMapping.javaType = javaType;
            parameterMapping.mode = ParameterMode.IN;
        }

        public Builder jdbcType(JdbcType jdbcType) {
            parameterMapping.jdbcType = jdbcType;
            return this;
        }

        /**
         * 通过构造器构造ParameterMapping
         *
         * @return
         */
        public ParameterMapping build() {

            /**
             * 设置类型处理器
             */
            resolveTypeHandler();

            //如果没有类型解析器，报错
            if (parameterMapping.typeHandler == null) {
                throw new IllegalStateException("Type handler was null on parameter mapping for property '"
                        + parameterMapping.property + "'. It was either not specified and/or could not be found for the javaType ("
                        + parameterMapping.javaType.getName() + ") : jdbcType (" + parameterMapping.jdbcType + ") combination.");
            }
            return parameterMapping;
        }


        /**
         * 获得类型解析器
         */
        private void resolveTypeHandler() {
            //类型处理器为空，且javatype不为空
            if (parameterMapping.typeHandler == null && parameterMapping.javaType != null) {
                //获取TypeHandlerRegistry
                TypeHandlerRegistry typeHandlerRegistry = parameterMapping.configuration.getTypeHandlerRegistry();
                //根据javaType和jdbcType获取处理器
                parameterMapping.typeHandler = typeHandlerRegistry.getTypeHandler(parameterMapping.javaType, parameterMapping.jdbcType);
            }
        }

    }

    public String getProperty() {
        return property;
    }

    /**
     * Used for handling output of callable statements
     *
     * @return
     */
    public ParameterMode getMode() {
        return mode;
    }

    /**
     * Used for handling output of callable statements
     *
     * @return
     */
    public Class<?> getJavaType() {
        return javaType;
    }

    /**
     * Used in the UnknownTypeHandler in case there is no handler for the property type
     *
     * @return
     */
    public JdbcType getJdbcType() {
        return jdbcType;
    }


    /**
     * Used when setting parameters to the PreparedStatement
     *
     * @return
     */
    public TypeHandler<?> getTypeHandler() {
        return typeHandler;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ParameterMapping{");
        //sb.append("configuration=").append(configuration); // configuration doesn't have a useful .toString()
        sb.append("property='").append(property).append('\'');
        sb.append(", mode=").append(mode);
        sb.append(", javaType=").append(javaType);
        sb.append(", jdbcType=").append(jdbcType);
        sb.append('}');
        return sb.toString();
    }
}
