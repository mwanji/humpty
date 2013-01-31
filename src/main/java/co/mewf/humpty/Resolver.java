package co.mewf.humpty;

import co.mewf.humpty.config.Context;

import java.io.Reader;

public interface Resolver {

  boolean accepts(String uri);
  Reader resolve(String uri, Context context);
  String expand(String uri);
}
