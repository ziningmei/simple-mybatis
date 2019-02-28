package com.ziningmei.mybatis.builder;

import com.ziningmei.mybatis.mapping.ParameterMapping;
import com.ziningmei.mybatis.mapping.SqlSource;
import com.ziningmei.mybatis.parsing.GenericTokenParser;
import com.ziningmei.mybatis.parsing.TokenHandler;
import com.ziningmei.mybatis.reflection.MetaClass;
import com.ziningmei.mybatis.session.Configuration;
import com.ziningmei.mybatis.type.TypeHandler;
import com.ziningmei.mybatis.type.TypeHandlerRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SqlSourceBuilder extends BaseBuilder {


    /**
     * 类型处理
     */
    public final TypeHandlerRegistry typeHandlerRegistry;

    public SqlSourceBuilder(Configuration configuration) {
        super(configuration);
        typeHandlerRegistry=configuration.getTypeHandlerRegistry();
    }

    /**
     * 创建SqlSource
     * @param originalSql
     * @param parameterType
     * @return
     */
    public SqlSource build(String originalSql, Class<?> parameterType) {
        //获取参数签名处理器
        ParameterMappingTokenHandler handler = new ParameterMappingTokenHandler(configuration, parameterType);

        //通用都签名解析器
        GenericTokenParser parser = new GenericTokenParser("#{", "}", handler);

        //解析sql，替换#{},并且存储参数都typehandler
        String sql = parser.parse(originalSql);

        //返回StaticSqlSource
        return new StaticSqlSource(configuration, sql, handler.getParameterMappings());

    }

    /**
     * 参数映射签名处理类
     */
    private class ParameterMappingTokenHandler extends BaseBuilder implements TokenHandler {

        private List<ParameterMapping> parameterMappings = new ArrayList<>();
        private Class<?> parameterType;

        /**
         * 初始化参数映射处理器
         * @param configuration
         * @param parameterType
         */
        public ParameterMappingTokenHandler(Configuration configuration, Class<?> parameterType) {
            super(configuration);
            this.parameterType = parameterType;
        }

        public List<ParameterMapping> getParameterMappings() {
            return parameterMappings;
        }

        @Override
        public String handleToken(String content) {
            parameterMappings.add(buildParameterMapping(content));
            return "?";
        }

        /**
         * 添加参数映射
         * @param content
         * @return
         */
        private ParameterMapping buildParameterMapping(String content) {

            //解析参数为property，注解的话，就只有一个参数，先抄过来，这段有点复杂
            Map<String, String> propertiesMap = parseParameterMapping(content);
            String property = propertiesMap.get("property");
            Class<?> propertyType;

            //如果是简单类型，则直接从typeHandlerRegistry取就好
            if (typeHandlerRegistry.hasTypeHandler(parameterType)) {
                propertyType = parameterType;
            }else {
                //否则，需要自己实现了
                MetaClass metaClass = MetaClass.forClass(parameterType, configuration.getReflectorFactory());
                //如果有getter方法
                if (metaClass.hasGetter(property)) {
                    //那就是get方法
                    propertyType = metaClass.getGetterType(property);
                } else {
                    //否则就是Object
                    propertyType = Object.class;
                }
            }

            //创建Builder
            ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, property, propertyType);

            //解析参数，主要是类型处理器
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
