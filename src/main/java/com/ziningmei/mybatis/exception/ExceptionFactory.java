package com.ziningmei.mybatis.exception;

public class ExceptionFactory {

    public static RuntimeException wrapException(String msg, Exception e) {
        return new PersistenceException(msg,e);
    }
}
