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
package com.ziningmei.mybatis.reflection.property;

import com.ziningmei.mybatis.reflection.ReflectionException;

import java.util.Locale;

/**
 * @author Clinton Begin
 * 属性命名器，工具类
 */
public final class PropertyNamer {

  private PropertyNamer() {
    // Prevent Instantiation of Static Class
  }

  /**
   * 根据方法名，获得对应的属性名
   * @param name
   * @return
   */
  public static String methodToProperty(String name) {
    //is方法
    if (name.startsWith("is")) {
      name = name.substring(2);
      //get set 方法
    } else if (name.startsWith("get") || name.startsWith("set")) {
      name = name.substring(3);
    } else {
      // 抛出 ReflectionException 异常，因为只能处理 is、set、get 方法
      throw new ReflectionException("Error parsing property name '" + name + "'.  Didn't start with 'is', 'get' or 'set'.");
    }

    //首字母小写
    if (name.length() == 1 || (name.length() > 1 && !Character.isUpperCase(name.charAt(1)))) {
      name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
    }

    return name;
  }

  /**
   * 判断是否是 get set is 方法
    * @param name
   * @return
   */
  public static boolean isProperty(String name) {
    return name.startsWith("get") || name.startsWith("set") || name.startsWith("is");
  }

  /**
   * 判断是不是get方法
   * @param name
   * @return
   */
  public static boolean isGetter(String name) {
    return name.startsWith("get") || name.startsWith("is");
  }

  /**
   * 判断是不是set方法
   * @param name
   * @return
   */
  public static boolean isSetter(String name) {
    return name.startsWith("set");
  }

}
