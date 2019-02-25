package com.ziningmei.mybatis.builder.annotation;

import com.ziningmei.mybatis.annotation.Delete;
import com.ziningmei.mybatis.annotation.Insert;
import com.ziningmei.mybatis.annotation.Select;
import com.ziningmei.mybatis.annotation.Update;
import com.ziningmei.mybatis.binding.BindingException;
import com.ziningmei.mybatis.binding.MapperMethod;
import com.ziningmei.mybatis.builder.BuilderException;
import com.ziningmei.mybatis.builder.IncompleteElementException;
import com.ziningmei.mybatis.builder.MapperBuilderAssistant;
import com.ziningmei.mybatis.builder.xml.XMLMapperBuilder;
import com.ziningmei.mybatis.io.Resources;
import com.ziningmei.mybatis.mapping.SqlSource;
import com.ziningmei.mybatis.scripting.LanguageDriver;
import com.ziningmei.mybatis.session.Configuration;
import com.ziningmei.mybatis.session.RowBounds;
import sun.plugin2.main.server.ResultHandler;

import javax.crypto.KeyGenerator;
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
            //加载xml格式
            loadXmlResource();
            //添加到已经加载到资源
            configuration.addLoadedResource(resource);
            //设置当前到空间
            assistant.setCurrentNamespace(type.getName());
            //获取所有方法
            Method[] methods = type.getMethods();
            //遍历方法，解析
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

    private String nullOrEmpty(String value) {
        return value == null || value.trim().length() == 0 ? null : value;
    }

    /**
     * 解析语句
     * @param method
     */
    public void parseStatement(Method method) {
        Class<?> parameterTypeClass = getParameterType(method);
        LanguageDriver languageDriver = getLanguageDriver(method);
        SqlSource sqlSource = getSqlSourceFromAnnotations(method, parameterTypeClass, languageDriver);
        if (sqlSource != null) {
            Options options = method.getAnnotation(Options.class);
            final String mappedStatementId = type.getName() + "." + method.getName();
            Integer fetchSize = null;
            Integer timeout = null;
            StatementType statementType = StatementType.PREPARED;
            ResultSetType resultSetType = ResultSetType.FORWARD_ONLY;
            SqlCommandType sqlCommandType = getSqlCommandType(method);
            boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
            boolean flushCache = !isSelect;

            KeyGenerator keyGenerator;
            String keyProperty = null;
            String keyColumn = null;
            if (SqlCommandType.INSERT.equals(sqlCommandType) || SqlCommandType.UPDATE.equals(sqlCommandType)) {
                // first check for SelectKey annotation - that overrides everything else
                SelectKey selectKey = method.getAnnotation(SelectKey.class);
                if (selectKey != null) {
                    keyGenerator = handleSelectKeyAnnotation(selectKey, mappedStatementId, getParameterType(method), languageDriver);
                    keyProperty = selectKey.keyProperty();
                } else if (options == null) {
                    keyGenerator = configuration.isUseGeneratedKeys() ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE;
                } else {
                    keyGenerator = options.useGeneratedKeys() ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE;
                    keyProperty = options.keyProperty();
                    keyColumn = options.keyColumn();
                }
            } else {
                keyGenerator = NoKeyGenerator.INSTANCE;
            }

            if (options != null) {
                if (FlushCachePolicy.TRUE.equals(options.flushCache())) {
                    flushCache = true;
                } else if (FlushCachePolicy.FALSE.equals(options.flushCache())) {
                    flushCache = false;
                }
                useCache = options.useCache();
                fetchSize = options.fetchSize() > -1 || options.fetchSize() == Integer.MIN_VALUE ? options.fetchSize() : null; //issue #348
                timeout = options.timeout() > -1 ? options.timeout() : null;
                statementType = options.statementType();
                resultSetType = options.resultSetType();
            }

            String resultMapId = null;
            ResultMap resultMapAnnotation = method.getAnnotation(ResultMap.class);
            if (resultMapAnnotation != null) {
                String[] resultMaps = resultMapAnnotation.value();
                StringBuilder sb = new StringBuilder();
                for (String resultMap : resultMaps) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(resultMap);
                }
                resultMapId = sb.toString();
            } else if (isSelect) {
                resultMapId = parseResultMap(method);
            }

            assistant.addMappedStatement(
                    mappedStatementId,
                    sqlSource,
                    statementType,
                    sqlCommandType,
                    fetchSize,
                    timeout,
                    // ParameterMapID
                    null,
                    parameterTypeClass,
                    resultMapId,
                    getReturnType(method),
                    resultSetType,
                    flushCache,
                    useCache,
                    // TODO gcode issue #577
                    false,
                    keyGenerator,
                    keyProperty,
                    keyColumn,
                    // DatabaseID
                    null,
                    languageDriver,
                    // ResultSets
                    options != null ? nullOrEmpty(options.resultSets()) : null);
        }


    }

    /**
     * 通过方法，返回值和语言驱动获取SqlSource
     *
     * @param method
     * @param parameterType
     * @param languageDriver
     * @return
     */
    private SqlSource getSqlSourceFromAnnotations(Method method, Class<?> parameterType, LanguageDriver languageDriver) {
        try {
            Annotation annotation = getSqlAnnotationType(method);
            if (annotation != null) {
                final String[] strings = (String[]) annotation.getClass().getMethod("value").invoke(annotation);
                return buildSqlSourceFromStrings(strings, parameterType, languageDriver);
            }
            return null;
        } catch (Exception e) {
            throw new BuilderException("Could not find value method on SQL annotation.  Cause: " + e, e);
        }
    }

    /**
     * 获取注解类
     * @param method
     * @return
     */
    private Annotation getSqlAnnotationType(Method method) {

        for (Class<? extends Annotation> type : SQL_ANNOTATION_TYPES) {
            Annotation annotation = method.getAnnotation(type);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    /**
     * 
     * @param strings
     * @param parameterTypeClass
     * @param languageDriver
     * @return
     */
    private SqlSource buildSqlSourceFromStrings(String[] strings, Class<?> parameterTypeClass, LanguageDriver languageDriver) {
        final StringBuilder sql = new StringBuilder();
        for (String fragment : strings) {
            sql.append(fragment);
            sql.append(" ");
        }
        return languageDriver.createSqlSource(configuration, sql.toString().trim(), parameterTypeClass);
    }

    /**
     * 选择注解
     * @param method
     * @param types
     * @return
     */
    private Class<? extends Annotation> chooseAnnotationType(Method method, Set<Class<? extends Annotation>> types) {

    }

    private LanguageDriver getLanguageDriver(Method method) {

        return assistant.getLanguageDriver(null);

    }

    private Object getReturnType(Method method) {
        return null;
    }

    /**
     * 获取返回值
     * @param method
     * @return
     */
    private Class<?> getParameterType(Method method) {
        Class<?> parameterType = null;

        Class<?>[] parameterTypes = method.getParameterTypes();
        //遍历参数类型数组


        for (Class<?> currentParameterType : parameterTypes) {
            //排除 RowBounds 和 ResultHandler 两种参数
            if (!RowBounds.class.isAssignableFrom(currentParameterType) && !ResultHandler.class.isAssignableFrom(currentParameterType)) {
                if (parameterType == null) {
                    //如果是单参数，则是该参数的类型
                    parameterType = currentParameterType;
                } else {
                    // issue #135
                    // 如果是多参数，则是 ParamMap 类型
                    parameterType = MapperMethod.ParamMap.class;
                }
            }
        }
        return parameterType;
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
