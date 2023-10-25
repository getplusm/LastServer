package t.me.p1azmer.plugin.last_server.dependencies;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.net.URLClassLoader;

public class IsolatedClassLoader extends URLClassLoader {

  public IsolatedClassLoader(@NotNull URL[] urls) {
    super(urls, ClassLoader.getSystemClassLoader().getParent());
  }

  static {
    ClassLoader.registerAsParallelCapable();
  }
}