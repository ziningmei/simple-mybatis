package com.ziningmei.mybatis.session;

import com.ziningmei.mybatis.binding.MapperRegistry;
import com.ziningmei.mybatis.builder.annotation.MethodResolver;
import com.ziningmei.mybatis.datasource.UnpooledDataSourceFactory;
import com.ziningmei.mybatis.parsing.XNode;
import com.ziningmei.mybatis.scripting.LanguageDriverRegistry;
import com.ziningmei.mybatis.scripting.xml.XMLLanguageDriver;
import com.ziningmei.mybatis.transaction.JdbcTransactionFactory;
import com.ziningmei.mybatis.type.TypeAliasRegistry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
     * sql片段
     */
    protected final Map<String, XNode> sqlFragments = new StrictMap<>("XML fragments parsed from previous mappers");

    /**
     * 初始化语言驱动注册
     */
    protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();

    /**
     * 已加载资源( Resource )集合
     */
    protected final Set<String> loadedResources = new HashSet<>();


    /**
     * mapper注册器
     */
    protected final MapperRegistry mapperRegistry = new MapperRegistry(this);


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

    public Configuration() {
        typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
        //typeAliasRegistry.registerAlias("MANAGED", ManagedTransactionFactory.class);

        //typeAliasRegistry.registerAlias("JNDI", JndiDataSourceFactory.class);
        //typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);
        typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);

        //typeAliasRegistry.registerAlias("PERPETUAL", PerpetualCache.class);

        //typeAliasRegistry.registerAlias("DB_VENDOR", VendorDatabaseIdProvider.class);

        //ypeAliasRegistry.registerAlias("XML", XMLLanguageDriver.class);
        //typeAliasRegistry.registerAlias("RAW", RawLanguageDriver.class);


        //typeAliasRegistry.registerAlias("CGLIB", CglibProxyFactory.class);
        //typeAliasRegistry.registerAlias("JAVASSIST", JavassistProxyFactory.class);

        languageRegistry.setDefaultDriverClass(XMLLanguageDriver.class);
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

    /**
     * 判断mapper是否已经注册
     *
     * @param boundType
     * @return
     */
    public boolean hasMapper(Class<?> boundType) {
        return mapperRegistry.hasMapper(boundType);
    }

    public <T> void addMapper(Class<T> boundType) {
        mapperRegistry.addMapper(boundType);
    }

    public void addIncompleteMethod(MethodResolver methodResolver) {

    }

    public MapperRegistry getMapperRegistry() {
        return mapperRegistry;
    }

    public LanguageDriverRegistry getLanguageRegistry() {
        return languageRegistry;
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
}
