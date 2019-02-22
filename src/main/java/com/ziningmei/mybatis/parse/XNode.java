package com.ziningmei.mybatis.parse;

import com.sun.corba.se.spi.orb.PropertyParser;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class XNode {

    private final Node node;
    private final String name;

    /**
     * 属性值
     */
    private final Properties attributes;
    private final XPathParser xpathParser;

    public XNode(XPathParser xpathParser, Node node) {
        this.xpathParser = xpathParser;
        this.node = node;
        this.attributes = parseAttributes(node);
        this.name = node.getNodeName();
    }

    private Properties parseAttributes(Node n) {
        Properties attributes = new Properties();
        NamedNodeMap attributeNodes = n.getAttributes();
        if (attributeNodes != null) {
            for (int i = 0; i < attributeNodes.getLength(); i++) {
                Node attribute = attributeNodes.item(i);
                String value = attribute.getNodeValue();
                attributes.put(attribute.getNodeName(), value);
            }
        }
        return attributes;
    }

    public XNode evalNode(String expression) {
        return xpathParser.evalNode(node, expression);
    }

    public String getStringAttribute(String name) {
        return getStringAttribute(name,null);
    }

    public String getStringAttribute(String name, String def) {
        String value = attributes.getProperty(name);
        if (value == null) {
            return def;
        } else {
            return value;
        }
    }

    public List<XNode> getChildren() {
        List<XNode> children = new ArrayList<>();
        NodeList nodeList = node.getChildNodes();
        if (nodeList != null) {
            for (int i = 0, n = nodeList.getLength(); i < n; i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    children.add(new XNode(xpathParser, node));
                }
            }
        }
        return children;
    }

    /**
     * 获取子节点Properties
     *
     * @return
     */
    public Properties getChildrenAsProperties() {
        Properties properties = new Properties();
        for (XNode child : getChildren()) {
            String name = child.getStringAttribute("name");
            String value = child.getStringAttribute("value");
            if (name != null && value != null) {
                properties.setProperty(name, value);
            }
        }
        return properties;
    }
}
