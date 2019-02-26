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
package com.ziningmei.mybatis.reflection;


import com.ziningmei.mybatis.reflection.factory.DefaultObjectFactory;
import com.ziningmei.mybatis.reflection.factory.DefaultReflectorFactory;
import com.ziningmei.mybatis.reflection.factory.ObjectFactory;
import com.ziningmei.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;
import com.ziningmei.mybatis.reflection.wrapper.ObjectWrapperFactory;

/**
 * @author Clinton Begin
 * 系统源对象
 */
public final class SystemMetaObject {

  /**
   * 对象工厂
   */
  public static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
  /**
   * 对象包装工厂
   */
  public static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();

  /**
   * 源对象
   */
  public static final MetaObject NULL_META_OBJECT = MetaObject.forObject(NullObject.class, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());


  private SystemMetaObject() {
    // Prevent Instantiation of Static Class
  }


  private static class NullObject {
  }

  /**
   * 返回源对象
   * @param object
   * @return
   */
  public static MetaObject forObject(Object object) {
    return MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
  }

}
