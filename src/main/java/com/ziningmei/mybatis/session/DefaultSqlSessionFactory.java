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
        return openSessionFromDataSource(configuration.getDefaultExecutorType(), null, false);
    }

    private SqlSession openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {
        Transaction tx = null;
        try {
            //获取环境
            final Environment environment = configuration.getEnvironment();
            //根据环境获取事务工厂
            final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
            //获取事务
            tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
            //获取执行器
            final Executor executor = configuration.newExecutor(tx);
            return new DefaultSqlSession(configuration, executor, autoCommit);
        } catch (Exception e) {
            closeTransaction(tx); // may have fetched a connection so lets call close()
            throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    private TransactionFactory getTransactionFactoryFromEnvironment(Environment environment) {
        return environment.getTransactionFactory();
    }

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
