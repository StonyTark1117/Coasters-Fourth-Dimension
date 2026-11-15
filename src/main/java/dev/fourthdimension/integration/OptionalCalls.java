package dev.fourthdimension.integration;

import java.lang.reflect.*;
import java.util.concurrent.ConcurrentHashMap;

/** Public, version-gated optional API access without making the addon a base runtime dependency. */
public final class OptionalCalls {
  private record Key(Class<?> type,String name,int count) {}
  private static final ConcurrentHashMap<Key,Method> METHODS=new ConcurrentHashMap<>();
  private OptionalCalls() {}
  public static Object staticCall(String type,String name,Object...args) {
    try{return invoke(Class.forName(type),null,name,args);}
    catch(ClassNotFoundException missing){throw new IllegalArgumentException("Optional API unavailable: "+type,missing);}
  }
  public static Object call(Object target,String name,Object...args) {
    return invoke(target.getClass(),target,name,args);
  }
  private static Object invoke(Class<?> type,Object target,String name,Object...args) {
    try {
      var method=METHODS.computeIfAbsent(new Key(type,name,args.length),key->{
        var matches=java.util.Arrays.stream(key.type().getMethods()).filter(m->m.getName().equals(key.name())&&m.getParameterCount()==key.count()).toList();
        if(matches.size()!=1)throw new IllegalArgumentException("Optional API method unavailable: "+name);
        return matches.getFirst();
      });
      return method.invoke(target,args);
    } catch(ReflectiveOperationException|SecurityException failure) {
      throw new IllegalArgumentException("Optional API unavailable: "+name,failure);
    }
  }
}
