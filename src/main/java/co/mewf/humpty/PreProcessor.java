package co.mewf.humpty;

import co.mewf.humpty.config.Context;

import java.io.Reader;
import java.util.Map;

public interface PreProcessor {

  boolean canProcess(String asset);
  Reader process(Reader reader, Map<String, Object> options, Context context);
}
