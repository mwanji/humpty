package co.mewf.humpty;

import co.mewf.humpty.config.PreProcessorContext;

import java.io.Reader;
import java.util.Map;

public interface PreProcessor extends Processor {

  boolean accepts(String asset);
  Reader preProcess(String asset, Reader reader, Map<String, Object> options, PreProcessorContext context);
}
