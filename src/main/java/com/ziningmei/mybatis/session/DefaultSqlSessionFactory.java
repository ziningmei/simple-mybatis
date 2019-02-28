package com.ziningmei.mybatis.session;

import com.ziningmei.mybatis.exception.ExceptionFactory;
import com.ziningmei.mybatis.executor.ErrorContext;
import com.ziningmei.mybatis.executor.Executor;
import com.ziningmei.mybatis.transaction.Transaction;
import com.ziningmei.mybatis.transaction.TransactionFactory;

import java.sql.SQLException;

/**
 * @author ziningmei
 * <p>
 * 默认DefaultSqlSessionFactory
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {

    /**
     * 核心配置类
     */
    private final Configuration configuration;


    public DefaultSqlSessionFactory(Configuration configuration) {

        this.configuration = configuration;

    }


    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public SqlSession openSession() {
        return openSessionFromDataSource();
    }

    /**
     * 通过数据源获取sqlsession
     * @return
     */
    private SqlSession openSessionFromDataSource() {
        Transaction tx = null;
        try {
            //获取环境
            final Environment environment = configuration.getEnvironment();
            //根据环境获取事务工厂
            final TransactionFactory transactionFactory = environment.getTransactionFactory();
            //获取事务
            tx = transactionFactory.newTransaction(environment.getDataSource(), null);
            //获取执行器
            final Executor executor = configuration.newExecutor(tx);
            return new DefaultSqlSession(configuration, executor);
        } catch (Exception e) {
            // may have fetched a connection so lets call close()
            closeTransaction(tx);
            throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    /**
     * 关闭事务
     * @param tx
     */
    private void closeTransaction(Transaction tx) {
        if (tx != null) {
            try {
                tx.close();
            } catch (SQLException ignore) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }
}
