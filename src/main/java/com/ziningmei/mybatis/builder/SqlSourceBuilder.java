package com.ziningmei.mybatis.builder;

import com.ziningmei.mybatis.mapping.ParameterMapping;
import com.ziningmei.mybatis.mapping.SqlSource;
import com.ziningmei.mybatis.parsing.GenericTokenParser;
import com.ziningmei.mybatis.parsing.TokenHandler;
import com.ziningmei.mybatis.reflection.MetaClass;
import com.ziningmei.mybatis.reflection.MetaObject;
import com.ziningmei.mybatis.session.Configuration;
import com.ziningmei.mybatis.type.TypeHandler;
import com.ziningmei.mybatis.type.TypeHandlerRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SqlSourceBuilder extends BaseBuilder {

    private static final String parameterProperties = "javaType,jdbcType,mode,numericScale,resultMap,typeHandler,jdbcTypeName";


    /**
     * 类型处理
     */
    public final TypeHandlerRegistry typeHandlerRegistry;

    public SqlSourceBuilder(Configuration configuration) {
        super(configuration);
        typeHandlerRegistry=configuration.getTypeHandlerRegistry();
    }

    public SqlSource parse(String originalSql, Class<?> parameterType, Map<String, Object> additionalParameters) {
        ParameterMappingTokenHandler handler = new ParameterMappingTokenHandler(configuration, parameterType, additionalParameters);
        GenericTokenParser parser = new GenericTokenParser("#{", "}", handler);
        String sql = parser.parse(originalSql);
        return new StaticSqlSource(configuration, sql, handler.getParameterMappings());
    }

    private class ParameterMappingTokenHandler extends BaseBuilder implements TokenHandler {

        private List<ParameterMapping> parameterMappings = new ArrayList<>();
        private Class<?> parameterType;
        private MetaObject metaParameters;

        public ParameterMappingTokenHandler(Configuration configuration, Class<?> parameterType, Map<String, Object> additionalParameters) {
            super(configuration);
            this.parameterType = parameterType;
            this.metaParameters = configuration.newMetaObject(additionalParameters);
        }

        public List<ParameterMapping> getParameterMappings() {
            return parameterMappings;
        }

        @Override
        public String handleToken(String content) {
            parameterMappings.add(buildParameterMapping(content));
            return "?";
        }

        private ParameterMapping buildParameterMapping(String content) {
            Map<String, String> propertiesMap = parseParameterMapping(content);
            String property = propertiesMap.get("property");
            Class<?> propertyType;
            if (typeHandlerRegistry.hasTypeHandler(parameterType)) {
                propertyType = parameterType;
            }else {
                MetaClass metaClass = MetaClass.forClass(parameterType, configuration.getReflectorFactory());
                if (metaClass.hasGetter(property)) {
                    propertyType = metaClass.getGetterType(property);
                } else {
                    propertyType = Object.class;
                }
            }


            ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, property, propertyType);
            Class<?> javaType = propertyType;
            String typeHandlerAlias = null;
            for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue();
                if ("javaType".equals(name)) {
                    javaType = resolveClass(value);
                    builder.javaType(javaType);
                } else if ("jdbcType".equals(name)) {
                    builder.jdbcType(resolveJdbcType(value));
                } else if ("mode".equals(name)) {
                    builder.mode(resolveParameterMode(value));
                } else if ("numericScale".equals(name)) {
                    builder.numericScale(Integer.valueOf(value));
                } else if ("resultMap".equals(name)) {
                    builder.resultMapId(value);
                } else if ("typeHandler".equals(name)) {
                    typeHandlerAlias = value;
                } else if ("jdbcTypeName".equals(name)) {
                    builder.jdbcTypeName(value);
                } else if ("property".equals(name)) {
                    // Do Nothing
                } else if ("expression".equals(name)) {
                    throw new BuilderException("Expression based parameters are not supported yet");
                } else {
                    throw new BuilderException("An invalid property '" + name + "' was found in mapping #{" + content + "}.  Valid properties are " + parameterProperties);
                }
            }
            if (typeHandlerAlias != null) {
                builder.typeHandler(resolveTypeHandler(javaType, typeHandlerAlias));
            }
            return builder.build();
        }

        private Map<String, String> parseParameterMapping(String content) {
            try {
                return new ParameterExpression(content);
            } catch (BuilderException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new BuilderException("Parsing error was found in mapping #{" + content + "}.  Check syntax #{property|(expression), var1=value1, var2=value2, ...} ", ex);
            }
        }

        /**
         * 从 typeHandlerRegistry 中获得或创建对应的 TypeHandler 对象
         * @param javaType
         * @param typeHandlerAlias
         * @return
         */
        protected TypeHandler<?> resolveTypeHandler(Class<?> javaType, String typeHandlerAlias) {
            if (typeHandlerAlias == null) {
                return null;
            }
            Class<?> type = resolveClass(typeHandlerAlias);
            if (type != null && !TypeHandler.class.isAssignableFrom(type)) {
                throw new BuilderException("Type " + type.getName() + " is not a valid TypeHandler because it does not implement TypeHandler interface");
            }
            @SuppressWarnings( "unchecked" ) // already verified it is a TypeHandler
                    Class<? extends TypeHandler<?>> typeHandlerType = (Class<? extends TypeHandler<?>>) type;
            return resolveTypeHandler(javaType, typeHandlerType);
        }

        /**
         * 获取获得或创建对应的 TypeHandler 对象
         * @param javaType
         * @param typeHandlerType
         * @return
         */
        protected TypeHandler<?> resolveTypeHandler(Class<?> javaType, Class<? extends TypeHandler<?>> typeHandlerType) {
            if (typeHandlerType == null) {
                return null;
            }
            // javaType ignored for injected handlers see issue #746 for full detail
            TypeHandler<?> handler = typeHandlerRegistry.getMappingTypeHandler(typeHandlerType);
            if (handler == null) {
                // not in registry, create a new one
                handler = typeHandlerRegistry.getInstance(javaType, typeHandlerType);
            }
            return handler;
        }

    }
}
