package co.mewf.humpty;

import co.mewf.humpty.config.PreProcessorContext;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.jcoffeescript.JCoffeeScriptCompiler;
import org.jcoffeescript.Option;

public class CoffeeScriptPreProcessor implements PreProcessor {

  @Override
  public boolean canProcess(String asset) {
    return asset.endsWith(".coffee");
  }

  @Override
  public Reader process(String asset, Reader reader, Map<String, Object> options, PreProcessorContext context) {
    ArrayList<Option> compilerOptions = new ArrayList<Option>();
    if (Boolean.TRUE.equals(options.get(Option.BARE.name()))) {
      compilerOptions.add(Option.BARE);
    }
    try {
      String compiled = new JCoffeeScriptCompiler(compilerOptions).compile(IOUtils.toString(reader));
      return new StringReader(compiled);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
