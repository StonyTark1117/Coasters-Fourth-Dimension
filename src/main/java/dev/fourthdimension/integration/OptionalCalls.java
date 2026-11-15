package dev.fourthdimension.integration;

import java.lang.reflect.*;
import java.util.concurrent.ConcurrentHashMap;

/** Public, version-gated optional API access without making the addon a base runtime dependency. */
public final class OptionalCalls {
  private record Key(Class<?> type,String name,int count) {}
  private static final ConcurrentHashMap<Key,Method> METHODS=new ConcurrentHashMap<>();
  private OptionalCalls() {}
  public static Object call(Object target,String name,Object...args) {
    try {
      var method=METHODS.computeIfAbsent(new Key(target.getClass(),name,args.length),key->{
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
