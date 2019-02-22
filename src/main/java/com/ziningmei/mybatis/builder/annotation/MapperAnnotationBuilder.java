package com.ziningmei.mybatis.builder.annotation;

import com.ziningmei.mybatis.annotation.Delete;
import com.ziningmei.mybatis.annotation.Insert;
import com.ziningmei.mybatis.annotation.Select;
import com.ziningmei.mybatis.annotation.Update;
import com.ziningmei.mybatis.builder.IncompleteElementException;
import com.ziningmei.mybatis.builder.MapperBuilderAssistant;
import com.ziningmei.mybatis.builder.xml.XMLMapperBuilder;
import com.ziningmei.mybatis.io.Resources;
import com.ziningmei.mybatis.session.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class MapperAnnotationBuilder {

    /**
     * sql注册类型
     */
    private static final Set<Class<? extends Annotation>> SQL_ANNOTATION_TYPES = new HashSet<>();

    private final Configuration configuration;

    private final MapperBuilderAssistant assistant;

    private final Class<?> type;

    static {
        SQL_ANNOTATION_TYPES.add(Select.class);
        SQL_ANNOTATION_TYPES.add(Insert.class);
        SQL_ANNOTATION_TYPES.add(Update.class);
        SQL_ANNOTATION_TYPES.add(Delete.class);
    }

    public MapperAnnotationBuilder(Configuration configuration, Class<?> type) {
        String resource = type.getName().replace('.', '/') + ".java (best guess)";

        this.configuration = configuration;
        assistant = new MapperBuilderAssistant(configuration, resource);
        this.type = type;

    }


    /**
     * 解析注解
     */
    public void parse() {
        //获取类型名称
        String resource = type.toString();
        if (!configuration.isResourceLoaded(resource)) {
            //
            loadXmlResource();
            configuration.addLoadedResource(resource);
            assistant.setCurrentNamespace(type.getName());
            Method[] methods = type.getMethods();
            for (Method method : methods) {
                try {
                    // issue #237
                    if (!method.isBridge()) {
                        parseStatement(method);
                    }
                } catch (IncompleteElementException e) {
                    configuration.addIncompleteMethod(new MethodResolver(this, method));
                }
            }
        }
        parsePendingMethods();


    }

    private void parsePendingMethods() {


    }

    public void parseStatement(Method method) {
        // Class<?> parameterTypeClass = getParameterType(method);


    }

    /**
     * 加载xml资源
     */
    private void loadXmlResource() {
        // Spring may not know the real resource name so we check a flag
        // to prevent loading again a resource twice
        // this flag is set at XMLMapperBuilder#bindMapperForNamespace
        // 如果没有被加载
        if (!configuration.isResourceLoaded("namespace:" + type.getName())) {
            String xmlResource = type.getName().replace('.', '/') + ".xml";
            InputStream inputStream = null;
            try {
                inputStream = Resources.getResourceAsStream(type.getClassLoader(), xmlResource);
            } catch (IOException e) {
                // ignore, resource is not required
            }
            if (inputStream != null) {
                XMLMapperBuilder xmlParser = new XMLMapperBuilder(inputStream, assistant.getConfiguration(), xmlResource, configuration.getSqlFragments(), type.getName());
                xmlParser.parse();
            }
        }
    }


}
