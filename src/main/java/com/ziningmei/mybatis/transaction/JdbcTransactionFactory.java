package com.ziningmei.mybatis.transaction;

import com.ziningmei.mybatis.session.TransactionIsolationLevel;

import javax.sql.DataSource;
import java.util.Properties;

public class JdbcTransactionFactory implements TransactionFactory {

    @Override
    public void setProperties(Properties props) {

    }

    @Override
    public Transaction newTransaction(DataSource ds, TransactionIsolationLevel level) {
        return new JdbcTransaction(ds, level);
    }
}
