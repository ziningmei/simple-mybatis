package com.ziningmei.mybatis.jdbc;

import java.io.Reader;
import java.sql.Connection;

/**
 * @author ziningmei
 *
 * 脚本执行器
 */
public class ScriptRunner {

    /**
     * 是否自动提交
     */
    private boolean autoCommit;

    public ScriptRunner(Connection connection) {


    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public void runScript(Reader reader) {


    }
}
