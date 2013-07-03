package co.mewf.humpty;

import co.mewf.humpty.config.Context;

import java.io.Reader;
import java.util.Map;

public interface PostProcessor extends Processor {

  boolean accepts(String asset);
  Reader postProcess(String asset, Reader reader, Map<String, Object> options, Context context);
}
