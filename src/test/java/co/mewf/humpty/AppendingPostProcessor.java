package co.mewf.humpty;

import co.mewf.humpty.config.Context;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import org.apache.commons.io.IOUtils;

public class AppendingPostProcessor implements PostProcessor {

  @Override
  public boolean canProcess(String asset) {
    return true;
  }

  @Override
  public Reader process(String asset, Reader reader, Map<String, Object> options, Context context) {
    try {
      return new StringReader(IOUtils.toString(reader) + "Appended!");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
