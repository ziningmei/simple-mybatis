package com.ziningmei.mybatis.builder;

import com.ziningmei.mybatis.mapping.*;
import com.ziningmei.mybatis.scripting.LanguageDriver;
import com.ziningmei.mybatis.session.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * mapper初始化帮助类
 */
public class MapperBuilderAssistant extends BaseBuilder {

    private String currentNamespace;

    private final String resource;

    public MapperBuilderAssistant(Configuration configuration, String resource) {
        super(configuration);
        this.resource = resource;
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

    public MappedStatement addMappedStatement(
            String id,
            SqlSource sqlSource,
            StatementType statementType,
            SqlCommandType sqlCommandType,
            Integer fetchSize,
            Integer timeout,
            Class<?> parameterType,
            String resultMap,
            Class<?> resultType,
            ResultSetType resultSetType,
            boolean resultOrdered,
            LanguageDriver lang) {

        //获取命名空间
        id = applyCurrentNamespace(id, false);

        //创建MappedStatement构造器
        MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, id, sqlSource, sqlCommandType)
                .resource(resource)
                .fetchSize(fetchSize)
                .timeout(timeout)
                .statementType(statementType)
                .lang(lang)
                .resultOrdered(resultOrdered)
                .resultMaps(getStatementResultMaps(resultMap, resultType, id))
                .resultSetType(resultSetType);

        //创建ParameterMap
        ParameterMap statementParameterMap = getStatementParameterMap(parameterType, id);
        if (statementParameterMap != null) {
            //将参数map放到创建MappedStatement里面去
            statementBuilder.parameterMap(statementParameterMap);
        }
        //创建MappedStatement
        MappedStatement statement = statementBuilder.build();

        //添加到configuration里面
        configuration.addMappedStatement(statement);
        return statement;
    }

    public String applyCurrentNamespace(String base, boolean isReference) {
        if (base == null) {
            return null;
        }
        if (isReference) {
            // is it qualified with any namespace yet?
            if (base.contains(".")) {
                return base;
            }
        } else {
            // is it qualified with this namespace yet?
            if (base.startsWith(currentNamespace + ".")) {
                return base;
            }
            if (base.contains(".")) {
                throw new BuilderException("Dots are not allowed in element names, please remove it from " + base);
            }
        }
        return currentNamespace + "." + base;
    }

    private List<ResultMap> getStatementResultMaps(
            String resultMap,
            Class<?> resultType,
            String statementId) {
        resultMap = applyCurrentNamespace(resultMap, true);

        List<ResultMap> resultMaps = new ArrayList<>();
        if (resultMap != null) {
            String[] resultMapNames = resultMap.split(",");
            for (String resultMapName : resultMapNames) {
                try {
                    resultMaps.add(configuration.getResultMap(resultMapName.trim()));
                } catch (IllegalArgumentException e) {
                    throw new IncompleteElementException("Could not find result map " + resultMapName, e);
                }
            }
        } else if (resultType != null) {
            ResultMap inlineResultMap = new ResultMap.Builder(
                    configuration,
                    statementId + "-Inline",
                    resultType,
                    new ArrayList<ResultMapping>(),
                    null).build();
            resultMaps.add(inlineResultMap);
        }
        return resultMaps;
    }

    /**
     * 获取StatementParameterMap
     * @param parameterTypeClass
     * @param statementId
     * @return
     */
    private ParameterMap getStatementParameterMap(
            Class<?> parameterTypeClass,
            String statementId) {

        ParameterMap parameterMap = null;

        //如果参数类型不为空，创建ParameterMap
        if (parameterTypeClass != null) {
            parameterMap = new ParameterMap.Builder(
                    statementId + "-Inline",
                    parameterTypeClass).build();
        }
        return parameterMap;
    }

    public ResultMap addResultMap(String id, Class<?> type, List<ResultMapping> resultMappings, Boolean autoMapping) {
        id = applyCurrentNamespace(id, false);
        //创建ResultMap
        ResultMap resultMap = new ResultMap.Builder(configuration, id, type, resultMappings, autoMapping)
                .build();
        //添加到configuration
        configuration.addResultMap(resultMap);
        return resultMap;
    }
}

