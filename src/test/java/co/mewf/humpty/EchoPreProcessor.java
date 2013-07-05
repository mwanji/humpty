package co.mewf.humpty;

import co.mewf.humpty.config.PreProcessorContext;

import java.io.Reader;

public class EchoPreProcessor implements PreProcessor {

  @Override
  public boolean accepts(String asset) {
    return true;
  }

  @Override
  public Reader preProcess(String asset, Reader reader, PreProcessorContext context) {
    return reader;
  }
}
