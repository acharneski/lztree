package com.simiacryptus.lztree;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Util {

  public static void log(String msg, Object... args) {
    System.out.println(String.format(msg, args));
  }

  @SuppressWarnings("restriction")
  public static sun.misc.Unsafe getUnsafe() throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {
    Constructor<sun.misc.Unsafe> unsafeConstructor = sun.misc.Unsafe.class.getDeclaredConstructor();
    unsafeConstructor.setAccessible(true);
    sun.misc.Unsafe unsafe = unsafeConstructor.newInstance();
    return unsafe;
  }
  
}
