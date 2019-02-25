/**
 * Copyright 2009-2015 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ziningmei.mybatis.scripting.xml;

import com.ziningmei.mybatis.executor.parameter.ParameterHandler;
import com.ziningmei.mybatis.mapping.BoundSql;
import com.ziningmei.mybatis.mapping.MappedStatement;
import com.ziningmei.mybatis.mapping.RawSqlSource;
import com.ziningmei.mybatis.mapping.SqlSource;
]import com.ziningmei.mybatis.parsing.PropertyParser;
import com.ziningmei.mybatis.scripting.LanguageDriver;
import com.ziningmei.mybatis.session.Configuration;

/**
 * @author Eduardo Macarron
 * <p>
 * xml语言驱动
 */
public class XMLLanguageDriver implements LanguageDriver {

    /**
     * @param mappedStatement The mapped statement that is being executed
     * @param parameterObject The input parameter object (can be null)
     * @param boundSql        The resulting SQL once the dynamic language has been executed.
     * @return
     */
    @Override
    public ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        //使用defaultParameterHandler
        //return new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
        return null;
    }


    @Override
    public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {

        //创建TextSqlNode
        TextSqlNode textSqlNode = new TextSqlNode(script);

        //返回rawsql
        return new RawSqlSource(configuration, script, parameterType);

    }

}
