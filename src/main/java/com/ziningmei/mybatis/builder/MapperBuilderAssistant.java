package com.ziningmei.mybatis.builder;

import com.ziningmei.mybatis.scripting.LanguageDriver;
import com.ziningmei.mybatis.session.Configuration;

public class MapperBuilderAssistant extends BaseBuilder{

    private String currentNamespace;

    private final String resource;

    public MapperBuilderAssistant(Configuration configuration, String resource) {
        super(configuration);
        this.resource=resource;
    }

    public void setCurrentNamespace(String currentNamespace) {
        if (currentNamespace == null) {
            throw new BuilderException("The mapper element requires a namespace attribute to be specified.");
        }

        if (this.currentNamespace != null && !this.currentNamespace.equals(currentNamespace)) {
            throw new BuilderException("Wrong namespace. Expected '"
                    + this.currentNamespace + "' but found '" + currentNamespace + "'.");
        }

        this.currentNamespace = currentNamespace;
    }

    public String getCurrentNamespace() {
        return currentNamespace;
    }

    public String getResource() {
        return resource;
    }

    /**
     * 获取语言驱动
     * @param langClass
     * @return
     */
    public LanguageDriver getLanguageDriver(Class<? extends LanguageDriver> langClass) {
        if (langClass != null) {
            //如果不为空，则直接注册
            configuration.getLanguageRegistry().register(langClass);
        } else {
            //否则使用默认的
            langClass = configuration.getLanguageRegistry().getDefaultDriverClass();
        }
        //获取默认的语言驱动
        return configuration.getLanguageRegistry().getDriver(langClass);
    }
}

