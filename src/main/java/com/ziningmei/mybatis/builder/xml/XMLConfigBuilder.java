package com.ziningmei.mybatis.builder.xml;

import com.ziningmei.mybatis.builder.BaseBuilder;
import com.ziningmei.mybatis.builder.BuilderException;
import com.ziningmei.mybatis.datasource.DataSourceFactory;
import com.ziningmei.mybatis.executor.ErrorContext;
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
     * 通过文件，环境，属性创建xml配置建造者
     *
     * @param reader
     */
    public XMLConfigBuilder(Reader reader) {
        //初始化Configuration
        super(new Configuration());
        //是否解析过，默认false
        this.parsed = false;
        //获取XPathParser
        this.parser = new XPathParser(reader, new XMLMapperEntityResolver());
    }


    /**
     * 通过配置文件获取Configuration
     * @return
     */
    public Configuration parse() {

        /**
         * 如果解析过报错，避免重复解析
         */
        if (parsed) {
            throw new BuilderException("Each XMLConfigBuilder can only be used once.");
        }
        parsed = true;

        try {
            //解析configuration
            XNode root=parser.evalNode("/configuration");
            //环境解析 重点是要解析出数据源
            environmentsElement(root.evalNode("environments"));
            // SQL 映射语句，主要解析mapper，这里使用注解的方式，重中之重
            mapperElement(root.evalNode("mappers"));
        } catch (Exception e) {
            throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
        }

        return configuration;

    }

    /**
     * 解析环境属性
     *
     * @param context
     */
    private void environmentsElement(XNode context) throws Exception {
        /**
         * 获取environments上下问，如果为空，取默认
         */
        if (context != null) {
            if (environment == null) {
                environment = context.getStringAttribute("default", null);
            }
        }
        // 遍历子节点解析
        // 即遍历每个environment
        for (XNode child : context.getChildren()) {
            //获取environment的id，
            String id = child.getStringAttribute("id", null);
            //当前environment和配置的id匹配的时候再解析
            if (isSpecifiedEnvironment(id)) {
                //解析事务工厂
                TransactionFactory txFactory = transactionManagerElement(child.evalNode("transactionManager"));
                //解析数据源工厂
                DataSourceFactory dsFactory = dataSourceElement(child.evalNode("dataSource"));
                //获取数据源
                DataSource dataSource = dsFactory.getDataSource();
                //创建environment
                Environment environment = new Environment.Builder(id)
                        .transactionFactory(txFactory)
                        .dataSource(dataSource).build();
                //将environment存在configuration
                configuration.setEnvironment(environment);
            }
        }

    }

    /**
     * 解析transactionManager
     *
     * @param context
     * @return
     */
    private TransactionFactory transactionManagerElement(XNode context) throws Exception {
        //事务必须指定的说，不指定就报错
        if (context != null) {
            //获取事务类型，这里默认JDBC
            String type = context.getStringAttribute("type");
            //获取子属性，根据name，value生成Properties
            Properties props = context.getChildrenAsProperties();
            //创建事务工厂了
            TransactionFactory factory = (TransactionFactory) resolveClass(type).newInstance();
            //将属性扔进去
            factory.setProperties(props);
            return factory;
        }
        throw new BuilderException("Environment declaration requires a TransactionFactory.");
    }

    /**
     * 解析datasource
     *
     * @param context
     * @return
     */
    private DataSourceFactory dataSourceElement(XNode context) throws Exception {
        //解析数据源，那么先判个空
        if (context != null) {
            //获取数据源类型
            String type = context.getStringAttribute("type");
            //获取子属性，根据name，value生成Properties
            Properties props = context.getChildrenAsProperties();
            //创建数据源工厂
            DataSourceFactory factory = (DataSourceFactory) resolveClass(type).newInstance();
            //将属性扔进去
            factory.setProperties(props);
            return factory;
        }
        throw new BuilderException("Environment declaration requires a DataSourceFactory.");
    }


    /**
     * 判断当前environment和default相同
     * @param id
     * @return
     */
    private boolean isSpecifiedEnvironment(String id) {
        //必须有默认的  environment
        if (environment == null) {
            throw new BuilderException("No environment specified.");
            //必须有id
        } else if (id == null) {
            throw new BuilderException("Environment requires an id attribute.");
            //判断是否匹配
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
                //和解析mybatis.xml一模一样
                String resource = child.getStringAttribute("resource");

                //如果资源路径不为空，那就开始解析
                if (resource != null) {
                    //错误上下文
                    ErrorContext.instance().resource(resource);
                    //获取xml文件
                    InputStream inputStream = Resources.getResourceAsStream(resource);
                    //xml解析器
                    XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource);
                    //解析文件
                    mapperParser.parse();
                }

            }

        }

    }
}
