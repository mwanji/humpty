package co.mewf.humpty.resolvers;

import co.mewf.humpty.Resolver;
import co.mewf.humpty.config.Context;

import java.io.InputStreamReader;
import java.io.Reader;

public class ClasspathResolver implements Resolver {

  @Override
  public boolean accepts(String uri) {
    return uri.startsWith("classpath:");
  }

  @Override
  public Reader resolve(String uri, Context context) {
    return new InputStreamReader(getClass().getResourceAsStream(uri.substring("classpath:".length())));
  }

  @Override
  public String expand(String uri) {
    return uri.substring("classpath:".length()) + "?resolver=classpath";
  }

}
