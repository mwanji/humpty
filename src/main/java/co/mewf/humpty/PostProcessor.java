package co.mewf.humpty;

import co.mewf.humpty.config.Context;

import java.io.Reader;
import java.util.Map;

public interface PostProcessor {

  boolean canProcess(String asset);
  Reader process(String asset, Reader reader, Map<String, Object> options, Context context);
}
