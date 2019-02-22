package com.ziningmei.mybatis.builder.xml;

import com.ziningmei.mybatis.builder.BaseBuilder;
import com.ziningmei.mybatis.builder.BuilderException;
import com.ziningmei.mybatis.builder.MapperBuilderAssistant;
import com.ziningmei.mybatis.io.Resources;
import com.ziningmei.mybatis.parse.XNode;
import com.ziningmei.mybatis.parse.XPathParser;
import com.ziningmei.mybatis.session.Configuration;

import java.io.InputStream;
import java.util.Map;

/**
 * xmlMappere建造者
 */
public class XMLMapperBuilder extends BaseBuilder {

    /**
     * 可重用模块
     */
    private final Map<String, XNode> sqlFragments;

    /**
     * mapper建造者辅助类
     */
    private final MapperBuilderAssistant builderAssistant;

    /**
     * java xpath解析起
     */
    private final XPathParser parser;

    /**
     * 数据引用地址
     */
    private final String resource;

    public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource, Map<String, XNode> sqlFragments) {
        this(new XPathParser(inputStream, new XMLMapperEntityResolver()),
                configuration, resource, sqlFragments);
    }

    private XMLMapperBuilder(XPathParser parser, Configuration configuration, String resource, Map<String, XNode> sqlFragments) {
        super(configuration);
        this.builderAssistant = new MapperBuilderAssistant(configuration, resource);
        this.parser = parser;
        this.sqlFragments = sqlFragments;
        this.resource = resource;
    }

    public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource, Map<String, XNode> sqlFragments, String namespace) {
        this(inputStream, configuration, resource, sqlFragments);
        this.builderAssistant.setCurrentNamespace(namespace);
    }

    public void parse() {

        //是否已经解析
        if (!configuration.isResourceLoaded(resource)) {
            //解析mapper
            configurationElement(parser.evalNode("/mapper"));
            //添加已经解析都文件
            configuration.addLoadedResource(resource);
            //绑定namespace
            bindMapperForNamespace();
        }

    }

    private void bindMapperForNamespace() {
        String namespace = builderAssistant.getCurrentNamespace();
        if (namespace != null) {
            Class<?> boundType = null;
            try {
                boundType = Resources.classForName(namespace);
            } catch (ClassNotFoundException e) {
                //ignore, bound type is not required
            }
            if (boundType != null) {
                if (!configuration.hasMapper(boundType)) {
                    // Spring may not know the real resource name so we set a flag
                    // to prevent loading again this resource from the mapper interface
                    // look at MapperAnnotationBuilder#loadXmlResource
                    configuration.addLoadedResource("namespace:" + namespace);
                    configuration.addMapper(boundType);
                }
            }
        }

    }

    private void configurationElement(XNode context) {
        try {
            //获取namespace
            String namespace = context.getStringAttribute("namespace");
            //如果报错，抛出异常
            if (namespace == null || namespace.equals("")) {
                throw new BuilderException("Mapper's namespace cannot be empty");
            }
            //记录当前namespacee
            builderAssistant.setCurrentNamespace(namespace);

            //结果
            //resultMapElements(context.evalNodes("/mapper/resultMap"));
            //解析sql节点
            //sqlElement(context.evalNodes("/mapper/sql"));
            // 解析 <select /> <insert /> <update /> <delete /> 节点们
            //buildStatementFromContext(context.evalNodes("select|insert|update|delete"));

        } catch (Exception e) {
            throw new BuilderException("Error parsing Mapper XML. The XML location is '" + resource + "'. Cause: " + e, e);
        }
    }
}
