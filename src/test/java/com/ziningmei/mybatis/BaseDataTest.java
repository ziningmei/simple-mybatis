/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.ziningmei.mybatis;

import com.ziningmei.mybatis.io.Resources;
import com.ziningmei.mybatis.jdbc.ScriptRunner;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class BaseDataTest {

  /**
   * 根据数据源 执行脚本
   * @param ds
   * @param resource
   * @throws IOException
   * @throws SQLException
   */
  public static void runScript(DataSource ds, String resource) throws IOException, SQLException {
    Connection connection = ds.getConnection();
    try {
      //初始化脚本执行器
      ScriptRunner runner = new ScriptRunner(connection);
      runner.setAutoCommit(true);
      runScript(runner, resource);
    } finally {
      connection.close();
    }
  }

  public static void runScript(ScriptRunner runner, String resource) throws IOException, SQLException {
    //读取资源文件
    Reader reader = Resources.getResourceAsReader(resource);
    try {
      runner.runScript(reader);
    } finally {
      reader.close();
    }
  }

}
