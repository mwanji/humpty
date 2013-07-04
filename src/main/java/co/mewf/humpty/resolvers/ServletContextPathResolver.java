package co.mewf.humpty.resolvers;

import co.mewf.humpty.Resolver;
import co.mewf.humpty.config.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

import javax.servlet.ServletContext;

/**
 * Looks up assets using the {@link ServletContext}. It accepts assets starting with <code>/</code>, e.g. /scripts/app.js
 */
public class ServletContextPathResolver implements Resolver {

  @Override
  public boolean accepts(String uri) {
    return uri.startsWith("/");
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
    return uri;
  }
}
