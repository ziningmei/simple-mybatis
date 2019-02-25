package com.ziningmei.mybatis.builder.xml;

import com.ziningmei.mybatis.builder.BaseBuilder;
import com.ziningmei.mybatis.builder.BuilderException;
import com.ziningmei.mybatis.datasource.DataSourceFactory;
import com.ziningmei.mybatis.io.Resources;
import com.ziningmei.mybatis.parsing.XNode;
import com.ziningmei.mybatis.parsing.XPathParser;
import com.ziningmei.mybatis.session.Configuration;
import com.ziningmei.mybatis.session.Environment;
import com.ziningmei.mybatis.transaction.TransactionFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

public class XMLConfigBuilder extends BaseBuilder {

    /**
     * 判断是否已经解析，一个XMLConfigBuilder只能用一次
     */
    private boolean parsed;
    /**
     * Xpath解析器
     */
    private final XPathParser parser;

    /**
     * 环境
     */
    private String environment;

    /**
     * 反射器工厂
     */
    //private final ReflectorFactory localReflectorFactory = new DefaultReflectorFactory();


    /**
     * 通过文件，环境，属性创建xml配置建造者
     *
     * @param reader
     */
    public XMLConfigBuilder(Reader reader) {
        this(new XPathParser(reader, new XMLMapperEntityResolver()));
    }

    public XMLConfigBuilder(XPathParser parser) {
        super(new Configuration());
        this.parsed = false;
        this.parser = parser;

    }

    public Configuration parse() {

        if (parsed) {
            throw new BuilderException("Each XMLConfigBuilder can only be used once.");
        }

        parsed = true;
        parseConfiguration(parser.evalNode("/configuration"));
        return configuration;

    }

    /**
     * 解析配置文件，获取Configuration
     * @param root
     */
    private void parseConfiguration(XNode root) {
        try {
            //环境解析
            environmentsElement(root.evalNode("environments"));
            // SQL 映射语句
            mapperElement(root.evalNode("mappers"));
        } catch (Exception e) {
            throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
        }
    }

    /**
     * 解析环境属性
     *
     * @param context
     */
    private void environmentsElement(XNode context) throws Exception {
        if (context != null) {
            if (environment == null) {
                environment = context.getStringAttribute("default", null);
            }
        }
        for (XNode child : context.getChildren()) {
            String id = child.getStringAttribute("id", null);
            //环境必须指定
            if (isSpecifiedEnvironment(id)) {
                TransactionFactory txFactory = transactionManagerElement(child.evalNode("transactionManager"));
                DataSourceFactory dsFactory = dataSourceElement(child.evalNode("dataSource"));
                DataSource dataSource = dsFactory.getDataSource();
                Environment.Builder environmentBuilder = new Environment.Builder(id)
                        .transactionFactory(txFactory)
                        .dataSource(dataSource);
                configuration.setEnvironment(environmentBuilder.build());
            }
        }

    }

    /**
     * 解析datasource
     *
     * @param context
     * @return
     */
    private DataSourceFactory dataSourceElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type");
            Properties props = context.getChildrenAsProperties();
            DataSourceFactory factory = (DataSourceFactory) resolveClass(type).newInstance();
            factory.setProperties(props);
            return factory;
        }
        throw new BuilderException("Environment declaration requires a DataSourceFactory.");
    }

    /**
     * 解析transactionManager
     *
     * @param context
     * @return
     */
    private TransactionFactory transactionManagerElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type");
            Properties props = context.getChildrenAsProperties();
            TransactionFactory factory = (TransactionFactory) resolveClass(type).newInstance();
            factory.setProperties(props);
            return factory;
        }
        throw new BuilderException("Environment declaration requires a TransactionFactory.");
    }

    private boolean isSpecifiedEnvironment(String id) {
        if (environment == null) {
            throw new BuilderException("No environment specified.");
        } else if (id == null) {
            throw new BuilderException("Environment requires an id attribute.");
        } else if (environment.equals(id)) {
            return true;
        }
        return false;
    }

    /**
     * 解析mapper
     * @param parent
     * @throws Exception
     */
    private void mapperElement(XNode parent) throws Exception {
        //节点不为空
        if (parent != null) {
            //循环遍历解析mapper
            for (XNode child : parent.getChildren()) {
                //获取资源路径
                String resource = child.getStringAttribute("resource");

                //如果资源路径不为空，就循环解析一次喽
                if (resource != null) {
                    //获取xml文件
                    InputStream inputStream = Resources.getResourceAsStream(resource);
                    //xml解析器
                    XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
                    //解析文件
                    mapperParser.parse();
                }

            }

        }

    }
}
