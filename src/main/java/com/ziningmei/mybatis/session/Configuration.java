package com.ziningmei.mybatis.session;

import com.ziningmei.mybatis.binding.MapperRegistry;
import com.ziningmei.mybatis.builder.ResultMapResolver;
import com.ziningmei.mybatis.datasource.UnpooledDataSourceFactory;
import com.ziningmei.mybatis.executor.Executor;
import com.ziningmei.mybatis.executor.SimpleExecutor;
import com.ziningmei.mybatis.executor.StatementHandler;
import com.ziningmei.mybatis.executor.ParameterHandler;
import com.ziningmei.mybatis.executor.resultSet.DefaultResultSetHandler;
import com.ziningmei.mybatis.executor.resultSet.ResultSetHandler;
import com.ziningmei.mybatis.executor.statement.RoutingStatementHandler;
import com.ziningmei.mybatis.mapping.BoundSql;
import com.ziningmei.mybatis.mapping.MappedStatement;
import com.ziningmei.mybatis.mapping.ParameterMap;
import com.ziningmei.mybatis.mapping.ResultMap;
import com.ziningmei.mybatis.parsing.XNode;
import com.ziningmei.mybatis.reflection.MetaObject;
import com.ziningmei.mybatis.reflection.factory.DefaultObjectFactory;
import com.ziningmei.mybatis.reflection.factory.DefaultReflectorFactory;
import com.ziningmei.mybatis.reflection.factory.ObjectFactory;
import com.ziningmei.mybatis.reflection.factory.ReflectorFactory;
import com.ziningmei.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;
import com.ziningmei.mybatis.reflection.wrapper.ObjectWrapperFactory;
import com.ziningmei.mybatis.transaction.JdbcTransactionFactory;
import com.ziningmei.mybatis.transaction.Transaction;
import com.ziningmei.mybatis.type.TypeAliasRegistry;
import com.ziningmei.mybatis.type.TypeHandlerRegistry;

import java.util.*;

/**
 * @author ziningmei
 * <p>
 * 核心配置类
 */
public class Configuration {

    /**
     * 环境
     */
    protected Environment environment;

    /**
     * 默认获取条数
     */
    protected Integer defaultFetchSize;

    /**
     * sql片段
     */
    protected final Map<String, XNode> sqlFragments = new StrictMap<>("XML fragments parsed from previous mappers");

    /**
     * 已加载资源( Resource )集合
     */
    protected final Set<String> loadedResources = new HashSet<>();


    /**
     * mapper注册器
     */
    protected final MapperRegistry mapperRegistry = new MapperRegistry(this);


    /**
     * 类型处理器注册器
     */
    protected final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();

    /**
     * 默认反射工厂
     */
    protected ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    /**
     * 默认对象工厂
     */
    protected ObjectFactory objectFactory = new DefaultObjectFactory();
    /**
     * 默认对象包装器工厂
     */
    protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

    /**
     * 返回map
     */
    protected final Map<String, ResultMap> resultMaps = new StrictMap<>("Result Maps collection");

    /**
     * 参数map
     */
    protected final Map<String, ParameterMap> parameterMaps = new StrictMap<>("Parameter Maps collection");

    /**
     * 映射map
     */
    protected final Map<String, MappedStatement> mappedStatements = new StrictMap<>("Mapped Statements collection");

    /**
     * 未完成的结果map
     */
    protected final Collection<ResultMapResolver> incompleteResultMaps = new LinkedList<>();

    /**
     * 默认超时时间
     */
    protected Integer defaultStatementTimeout;

    /**
     * 配置工厂
     */
    protected Class<?> configurationFactory;

    /**
     * 设置自动映射
     */
    protected AutoMappingBehavior autoMappingBehavior = AutoMappingBehavior.PARTIAL;

    /**
     * 果集中值为 null 的时候是否调用映射对象的 setter（map 对象时为 put）方法
     */
    boolean callSettersOnNulls = false;

    /**
     * 是否开启自动驼峰命名规则
     */
    boolean mapUnderscoreToCamelCase=false;


    public Configuration() {
        typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);

        typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);

    }

    public TypeHandlerRegistry getTypeHandlerRegistry() {
        return typeHandlerRegistry;
    }

    public Map<String, XNode> getSqlFragments() {
        return sqlFragments;
    }

    public Environment getEnvironment() {
        return environment;
    }

    protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();

    public TypeAliasRegistry getTypeAliasRegistry() {
        return typeAliasRegistry;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * 判断资源是否已经加载
     *
     * @param resource
     * @return
     */
    public boolean isResourceLoaded(String resource) {
        return loadedResources.contains(resource);
    }

    public void addLoadedResource(String resource) {
        loadedResources.add(resource);
    }

    public boolean isCallSettersOnNulls() {
        return callSettersOnNulls;
    }

    /**
     * 判断mapper是否已经注册
     *
     * @param boundType
     * @return
     */
    public boolean hasMapper(Class<?> boundType) {
        return mapperRegistry.hasMapper(boundType);
    }

    /**
     * mapperRegistry添加mapper
     *
     * @param boundType
     * @param <T>
     */
    public <T> void addMapper(Class<T> boundType) {
        mapperRegistry.addMapper(boundType);
    }

    protected static class StrictMap<V> extends HashMap<String, V> {

        private static final long serialVersionUID = -4950446264854982944L;
        private final String name;


        public StrictMap(String name) {
            super();
            this.name = name;
        }

        @Override
        @SuppressWarnings("unchecked")
        public V put(String key, V value) {
            if (containsKey(key)) {
                throw new IllegalArgumentException(name + " already contains value for " + key);
            }
            if (key.contains(".")) {
                final String shortKey = getShortName(key);
                if (super.get(shortKey) == null) {
                    super.put(shortKey, value);
                } else {
                    super.put(shortKey, (V) new Ambiguity(shortKey));
                }
            }
            return super.put(key, value);
        }

        @Override
        public V get(Object key) {
            V value = super.get(key);
            if (value == null) {
                throw new IllegalArgumentException(name + " does not contain value for " + key);
            }
            if (value instanceof Ambiguity) {
                throw new IllegalArgumentException(((Ambiguity) value).getSubject() + " is ambiguous in " + name
                        + " (try using the full name including the namespace, or rename one of the entries)");
            }
            return value;
        }

        private String getShortName(String key) {
            final String[] keyParts = key.split("\\.");
            return keyParts[keyParts.length - 1];
        }

        protected static class Ambiguity {
            final private String subject;

            public Ambiguity(String subject) {
                this.subject = subject;
            }

            public String getSubject() {
                return subject;
            }
        }
    }

    /**
     * 新建元数据对象
     *
     * @param object
     * @return
     */
    public MetaObject newMetaObject(Object object) {
        return MetaObject.forObject(object, objectFactory, objectWrapperFactory, reflectorFactory);
    }

    public ResultMap getResultMap(String id) {
        return resultMaps.get(id);
    }

    public void addResultMap(ResultMap rm) {
        resultMaps.put(rm.getId(), rm);
    }

    public void addMappedStatement(MappedStatement ms) {
        mappedStatements.put(ms.getId(), ms);
    }

    public ReflectorFactory getReflectorFactory() {
        return reflectorFactory;
    }


    public Executor newExecutor(Transaction transaction) {

        //创建执行器
        Executor executor = new SimpleExecutor(this, transaction);

        return executor;
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }


    public ObjectWrapperFactory getObjectWrapperFactory() {
        return objectWrapperFactory;
    }

    public Collection<MappedStatement> getMappedStatements() {
        buildAllStatements();
        return mappedStatements.values();
    }

    protected void buildAllStatements() {
        if (!incompleteResultMaps.isEmpty()) {
            synchronized (incompleteResultMaps) {
                // This always throws a BuilderException.
                incompleteResultMaps.iterator().next().resolve();
            }
        }
    }

    public MappedStatement getMappedStatement(String id) {
        return mappedStatements.get(id);
    }

    /**
     * 获取mapper
     * @param type
     * @param sqlSession
     * @param <T>
     * @return
     */
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        //从mapper注册器中获得
        return mapperRegistry.getMapper(type, sqlSession);
    }

    public boolean hasStatement(String statementName) {
        return hasStatement(statementName, true);
    }

    public boolean hasStatement(String statementName, boolean validateIncompleteStatements) {
        if (validateIncompleteStatements) {
            buildAllStatements();
        }
        return mappedStatements.containsKey(statementName);
    }

    public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        StatementHandler statementHandler = new RoutingStatementHandler(executor, mappedStatement, parameterObject, rowBounds, resultHandler, boundSql);
        return statementHandler;
    }

    public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        ParameterHandler parameterHandler = mappedStatement.getLang().createParameterHandler(mappedStatement, parameterObject, boundSql);
        return parameterHandler;
    }

    public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, RowBounds rowBounds, ParameterHandler parameterHandler,
                                                ResultHandler resultHandler, BoundSql boundSql) {
        ResultSetHandler resultSetHandler = new DefaultResultSetHandler(executor, mappedStatement, parameterHandler, resultHandler, boundSql, rowBounds);
        return resultSetHandler;
    }

    public Integer getDefaultStatementTimeout() {
        return defaultStatementTimeout;
    }

    public void setDefaultStatementTimeout(Integer defaultStatementTimeout) {
        this.defaultStatementTimeout = defaultStatementTimeout;
    }

    public Integer getDefaultFetchSize() {
        return defaultFetchSize;
    }

    public void setDefaultFetchSize(Integer defaultFetchSize) {
        this.defaultFetchSize = defaultFetchSize;
    }

    public Class<?> getConfigurationFactory() {
        return configurationFactory;
    }

    public AutoMappingBehavior getAutoMappingBehavior() {
        return autoMappingBehavior;
    }

    public boolean isMapUnderscoreToCamelCase() {
        return mapUnderscoreToCamelCase;
    }


}
