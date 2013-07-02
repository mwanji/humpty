package co.mewf.humpty.resolvers;

import co.mewf.humpty.Resolver;
import co.mewf.humpty.config.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

public class ServletContextPathResolver implements Resolver {

  @Override
  public boolean accepts(String uri) {
    return uri.startsWith("path:");
  }

  @Override
  public Reader resolve(String uri, Context context) {
    try {
      return new FileReader(new File(context.getRequest().getServletContext().getRealPath(expand(uri))));
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String expand(String uri) {
    return uri.substring("path:".length());
  }
}
