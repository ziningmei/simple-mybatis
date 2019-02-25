package com.ziningmei.mybatis.parsing;

import com.ziningmei.mybatis.builder.BuilderException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.*;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class XPathParser {

    /**
     * xml 文档
     */
    private final Document document;

    /**
     * dtd本地化
     */
    private EntityResolver entityResolver;

    /**
     * 解析器
     */
    private XPath xpath;

    /**
     * 根据文件，是否验证，参数，XPathParser
     *
     * @param reader
     * @param entityResolver
     */
    public XPathParser(Reader reader, EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
        // 创建XPathFactory对象
        XPathFactory factory = XPathFactory.newInstance();
        this.xpath = factory.newXPath();
        this.document = createDocument(new InputSource(reader));
    }

    /**
     * 根据文件，是否验证，参数，XPathParser
     *
     * @param inputStream
     * @param entityResolver
     */
    public XPathParser(InputStream inputStream, EntityResolver entityResolver) {
        this(new InputStreamReader(inputStream),entityResolver);
    }

    /**
     * 创建document对象
     * @param inputSource
     * @return
     */
    private Document createDocument(InputSource inputSource) {
        try {
            //创建DocumentBuilderFactory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            //设置是否校验xml
            factory.setValidating(false);

            //是否支持命名空间
            factory.setNamespaceAware(false);
            //是否忽略注释
            factory.setIgnoringComments(true);
            //是否忽略空白元素
            factory.setIgnoringElementContentWhitespace(false);
            //是否转义CDATA
            factory.setCoalescing(false);
            //是否拓展实体引用
            factory.setExpandEntityReferences(true);

            //创建DocumentBuilder
            DocumentBuilder builder = factory.newDocumentBuilder();

            //设置实体解析器
            builder.setEntityResolver(entityResolver);

            //设置错误处理
            builder.setErrorHandler(new ErrorHandler() {
                @Override
                public void error(SAXParseException exception) throws SAXException {
                    throw exception;
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    throw exception;
                }

                @Override
                public void warning(SAXParseException exception) throws SAXException {
                }
            });
            //解析xml文件
            return builder.parse(inputSource);

        } catch (Exception e) {
            throw new BuilderException("Error creating document instance.  Cause: " + e, e);
        }
    }


    public XNode evalNode(String expression) {
        return evalNode(document,expression);
    }


    /**
     * 获取指定元素或者节点的值
     *
     * @param expression 表达式
     * @param root 指定节点
     * @param returnType 返回类型
     * @return
     */
    private Object evaluate(String expression, Object root, QName returnType) {
        try {
            return xpath.evaluate(expression, root, returnType);
        } catch (Exception e) {
            throw new BuilderException("Error evaluating XPath.  Cause: " + e, e);
        }
    }

    public XNode evalNode(Object root, String expression) {
        Node node = (Node) evaluate(expression, root, XPathConstants.NODE);
        if (node == null) {
            return null;
        }
        return new XNode(this, node);
    }
}
