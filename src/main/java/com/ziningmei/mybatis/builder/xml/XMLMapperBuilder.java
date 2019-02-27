package com.ziningmei.mybatis.builder.xml;

import com.ziningmei.mybatis.builder.BaseBuilder;
import com.ziningmei.mybatis.builder.BuilderException;
import com.ziningmei.mybatis.builder.MapperBuilderAssistant;
import com.ziningmei.mybatis.io.Resources;
import com.ziningmei.mybatis.parsing.XNode;
import com.ziningmei.mybatis.parsing.XPathParser;
import com.ziningmei.mybatis.session.Configuration;

import java.io.InputStream;

/**
 * xmlMappere建造者
 */
public class XMLMapperBuilder extends BaseBuilder {


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

    /**
     * 对外开放的 constructor
     * @param inputStream
     * @param configuration
     * @param resource
     */
    public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource) {
        this(new XPathParser(inputStream, new XMLMapperEntityResolver()),
                configuration, resource);
    }

    /**
     * 实际上调用的constructor
     * @param parser
     * @param configuration
     * @param resource
     */
    private XMLMapperBuilder(XPathParser parser, Configuration configuration, String resource) {
        super(configuration);
        //设置默认的帮助类
        this.builderAssistant = new MapperBuilderAssistant(configuration, resource);
        //解析起
        this.parser = parser;
        //资源地址
        this.resource = resource;
    }

    /**
     * 解析mapper文件
     */
    public void parse() {

        //是否已经解析资源,该处判断的是xml文件
        if (!configuration.isResourceLoaded(resource)) {
            //解析mapper
            configurationElement(parser.evalNode("/mapper"));
            //添加已经解析都文件
            configuration.addLoadedResource(resource);
            //将mapper绑定到namespace
            bindMapperForNamespace();
        }

    }

    /**
     * 将mapper绑定到namespace
     */
    private void bindMapperForNamespace() {
        //获取当前的namespace
        String namespace = builderAssistant.getCurrentNamespace();
        //当然要有namespce才能继续解析了
        if (namespace != null) {
            //获取绑定的class，必须要存在把，不然解析怎么继续下去
            Class<?> boundType = null;
            try {
                //就是判断类存不存在
                boundType = Resources.classForName(namespace);
            } catch (ClassNotFoundException e) {
                //ignore, bound type is not required
            }

            //如果类是存在的，继续解析
            if (boundType != null) {
                //就是判断有没有解析过，解析过的是不要再解析的

                if (!configuration.hasMapper(boundType)) {
                    // Spring may not know the real resource name so we set a flag
                    // to prevent loading again this resource from the mapper interface
                    // look at MapperAnnotationBuilder#loadXmlResource
                    // spring继承的时候，避免重复加载
                    configuration.addLoadedResource("namespace:" + namespace);
                    // 添加mapper，和上面的contains对应
                    configuration.addMapper(boundType);
                }
            }
        }

    }

    /**
     * 解析mapper，设置namespace
     * @param context
     */
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

            //因为只解析注解，所以这边不继续解析下去

        } catch (Exception e) {
            throw new BuilderException("Error parsing Mapper XML. The XML location is '" + resource + "'. Cause: " + e, e);
        }
    }
}
