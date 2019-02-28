package com.ziningmei.mybatis.builder;

import com.ziningmei.mybatis.annotation.Delete;
import com.ziningmei.mybatis.annotation.Insert;
import com.ziningmei.mybatis.annotation.Select;
import com.ziningmei.mybatis.annotation.Update;
import com.ziningmei.mybatis.binding.MapperMethod;
import com.ziningmei.mybatis.mapping.ResultSetType;
import com.ziningmei.mybatis.mapping.SqlCommandType;
import com.ziningmei.mybatis.mapping.SqlSource;
import com.ziningmei.mybatis.mapping.StatementType;
import com.ziningmei.mybatis.reflection.TypeParameterResolver;
import com.ziningmei.mybatis.scripting.LanguageDriver;
import com.ziningmei.mybatis.scripting.xml.XMLLanguageDriver;
import com.ziningmei.mybatis.session.Configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * mapper 注解构造器
 */
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

    /**
     * 通过configuration和类初始化
     *
     * @param configuration
     * @param type
     */
    public MapperAnnotationBuilder(Configuration configuration, Class<?> type) {
        //修改resource的名字
        String resource = type.getName().replace('.', '/') + ".java (best guess)";

        this.configuration = configuration;

        //初始化帮助类
        assistant = new MapperBuilderAssistant(configuration, resource);
        this.type = type;

    }


    /**
     * 解析注解
     */
    public void parse() {
        // 获取类型名称
        String resource = type.toString();
        // 老套路，判断有没有加载过,判断的是接口
        if (!configuration.isResourceLoaded(resource)) {

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
                    // 如果不是桥接方法，则解析
                    if (!method.isBridge()) {

                        //解析语句
                        parseStatement(method);
                    }
                } catch (IncompleteElementException e) {

                }
            }
        }

    }


    /**
     * 解析语句
     *
     * @param method
     */
    public void parseStatement(Method method) {
        //获取参数类
        Class<?> parameterTypeClass = getParameterType(method);
        //获取语言驱动
        LanguageDriver languageDriver = getLanguageDriver();
        //获取SqlSource
        SqlSource sqlSource = getSqlSourceFromAnnotations(method, parameterTypeClass, languageDriver);
        if (sqlSource != null) {
            //语句id
            final String mappedStatementId = type.getName() + "." + method.getName();
            //获取数量
            Integer fetchSize = null;
            //超时时间
            Integer timeout = null;
            //语句类型
            StatementType statementType = StatementType.PREPARED;
            //结果集类型
            ResultSetType resultSetType = ResultSetType.FORWARD_ONLY;
            //命令类型 SELECT
            SqlCommandType sqlCommandType = getSqlCommandType(method);
            //判断类型是不是select
            boolean isSelect = sqlCommandType == SqlCommandType.SELECT;

            //获得 resultMapId 编号字符串
            String resultMapId = null;
            if (isSelect) {
                //如果是select，需要解析返回值
                resultMapId = parseResultMap(method);
            }
            //添加语句
            assistant.addMappedStatement(mappedStatementId, sqlSource, statementType, sqlCommandType, fetchSize, timeout, parameterTypeClass, resultMapId, getReturnType(method), resultSetType, false, languageDriver);
        }

    }

    /**
     * 根据方法获取 resultMapId
     *
     * @param method
     * @return
     */
    private String parseResultMap(Method method) {
        //获取返回类型
        Class<?> returnType = getReturnType(method);
        //生成resultMapId
        String resultMapId = generateResultMapName(method);
        //生成ResultMap对象
        assistant.addResultMap(resultMapId, returnType, new ArrayList<>(), null);
        //返回resultMapId
        return resultMapId;
    }

    /**
     * 生成resultMapId
     * <p>
     * 类名+方法名+参数名（空为void）
     *
     * @param method
     * @return
     */
    private String generateResultMapName(Method method) {

        StringBuilder suffix = new StringBuilder();
        for (Class<?> c : method.getParameterTypes()) {
            suffix.append("-");
            suffix.append(c.getSimpleName());
        }
        if (suffix.length() < 1) {
            suffix.append("-void");
        }
        return type.getName() + "." + method.getName() + suffix;
    }

    private SqlCommandType getSqlCommandType(Method method) {
        Class<? extends Annotation> type = getSqlAnnotationType(method);

        if (type == null) {
            return SqlCommandType.UNKNOWN;
        }

        return SqlCommandType.valueOf(type.getSimpleName().toUpperCase(Locale.ENGLISH));
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
            //获取注解
            Annotation annotation = method.getAnnotation(getSqlAnnotationType(method));
            //如果注解为空
            if (annotation != null) {
                //获取注解的值
                final String[] strings = (String[]) annotation.getClass().getMethod("value").invoke(annotation);
                //创建SqlSource
                return buildSqlSourceFromStrings(strings, parameterType, languageDriver);
            }
            return null;
        } catch (Exception e) {
            throw new BuilderException("Could not find value method on SQL annotation.  Cause: " + e, e);
        }
    }

    private Class<? extends Annotation> getSqlAnnotationType(Method method) {
        return chooseAnnotationType(method, SQL_ANNOTATION_TYPES);
    }


    /**
     * 创建SqlSource
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
     * 获取注解
     *
     * @param method
     * @param types
     * @return
     */
    private Class<? extends Annotation> chooseAnnotationType(Method method, Set<Class<? extends Annotation>> types) {
        for (Class<? extends Annotation> type : types) {
            Annotation annotation = method.getAnnotation(type);
            if (annotation != null) {
                return type;
            }
        }
        return null;
    }


    /**
     * 获取语言驱动
     *
     * @return
     */
    private LanguageDriver getLanguageDriver() {

        return new XMLLanguageDriver();

    }

    /**
     * 获取返回类型，比较复杂，暂且忽略
     *
     * @param method
     * @return
     */
    private Class<?> getReturnType(Method method) {
        //获取返回值
        Class<?> returnType = method.getReturnType();
        //获取实际类型
        Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, type);
        //如果是一个类
        if (resolvedReturnType instanceof Class) {
            //那么直接赋值
            returnType = (Class<?>) resolvedReturnType;
        }

        return returnType;
    }

    /**
     * 获取参数
     *
     * @param method
     * @return
     */
    private Class<?> getParameterType(Method method) {
        Class<?> parameterType = null;

        Class<?>[] parameterTypes = method.getParameterTypes();
        //遍历参数类型数组
        for (Class<?> currentParameterType : parameterTypes) {

            //如果为空，设置
            if (parameterType == null) {
                //如果是单参数，则是该参数的类型
                parameterType = currentParameterType;
            } else {
                // issue #135
                // 如果是多参数，则是 ParamMap 类型
                parameterType = MapperMethod.ParamMap.class;
            }

        }
        return parameterType;
    }


}
