package co.mewf.humpty;

import co.mewf.humpty.config.PreProcessorContext;

import java.io.Reader;
import java.util.Map;

public interface PreProcessor {

  boolean canProcess(String asset);
  Reader process(String asset, Reader reader, Map<String, Object> options, PreProcessorContext context);
}
