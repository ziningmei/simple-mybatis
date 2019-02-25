package com.ziningmei.mybatis.scripting;

import java.util.HashMap;
import java.util.Map;

public class LanguageDriverRegistry {

    private static final Map<Class<? extends LanguageDriver>, LanguageDriver> LANGUAGE_DRIVER_MAP = new HashMap<>();

    /**
     * 默认驱动
     */
    private Class<? extends LanguageDriver> defaultDriverClass;

    /**
     * 注册class注册
     *
     * @param cls
     */
    public void register(Class<? extends LanguageDriver> cls) {

        if (cls == null) {
            throw new IllegalArgumentException("null is not a valid Language Driver");
        }
        //如果不存在，则添加
        if (!LANGUAGE_DRIVER_MAP.containsKey(cls)) {
            try {
                LANGUAGE_DRIVER_MAP.put(cls, cls.newInstance());
            } catch (Exception ex) {
                throw new ScriptingException("Failed to load language driver for " + cls.getName(), ex);
            }
        }

    }

    /**
     * 获取默认
     * @return
     */
    public LanguageDriver getDefaultDriver() {
        return getDriver(getDefaultDriverClass());
    }

    public LanguageDriver getDriver(Class<? extends LanguageDriver> driverClass) {
        return LANGUAGE_DRIVER_MAP.get(driverClass);
    }


    /**
     * 返回默认驱动
     * @return
     */
    public Class<? extends LanguageDriver> getDefaultDriverClass() {
        return defaultDriverClass;
    }

    public void setDefaultDriverClass(Class<? extends LanguageDriver> defaultDriverClass) {
        this.defaultDriverClass = defaultDriverClass;
    }
}
