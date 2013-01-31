package co.mewf.humpty;

import co.mewf.humpty.config.Context;

import java.io.Reader;
import java.util.Map;

public interface PreProcessor extends Processor {

  boolean canPreProcess(String asset);
  Reader preProcess(Reader reader, Map<String, Object> options, Context context);
}
