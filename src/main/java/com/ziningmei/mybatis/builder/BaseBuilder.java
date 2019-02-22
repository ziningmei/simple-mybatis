package com.ziningmei.mybatis.builder;

import com.ziningmei.mybatis.session.Configuration;
import com.ziningmei.mybatis.type.TypeAliasRegistry;

/**
 * @author jiangqi372
 */
public class BaseBuilder {

    /**
     * 配置综合类
     */
    protected final Configuration configuration;

    /**
     * 别名注册
     */
    protected final TypeAliasRegistry typeAliasRegistry;

    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
        this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();

    }

    /**
     * 创建对象
     * @param alias
     * @param <T>
     * @return
     */
    protected <T> Class<? extends T> resolveClass(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return resolveAlias(alias);
        } catch (Exception e) {
            throw new BuilderException("Error resolving class. Cause: " + e, e);
        }
    }

    /**
     * 解析别名
     * @param alias
     * @param <T>
     * @return
     */
    protected <T> Class<? extends T> resolveAlias(String alias) {

        return typeAliasRegistry.resolveAlias(alias);
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
