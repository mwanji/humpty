package co.mewf.humpty;

import co.mewf.humpty.config.Context;

import java.io.Reader;
import java.util.Map;

public interface PostProcessor {

  boolean canPostProcess(String asset);
  Reader postProcess(Reader reader, Map<String, Object> options, Context context);
}
