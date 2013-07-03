package co.mewf.humpty;

import co.mewf.humpty.config.PreProcessorContext;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.jcoffeescript.JCoffeeScriptCompiler;
import org.jcoffeescript.Option;

public class CoffeeScriptCompilingProcessor implements CompilingProcessor {

  @Override
  public boolean accepts(String asset) {
    return asset.endsWith(".coffee");
  }

  @Override
  public CompilationResult compile(String asset, Reader reader, Map<String, Object> options, PreProcessorContext context) {
    ArrayList<Option> compilerOptions = new ArrayList<Option>();
    compilerOptions.add(Option.BARE);
    try {
      String compiled = new JCoffeeScriptCompiler(compilerOptions).compile(IOUtils.toString(reader));
      return new CompilationResult(asset.replace(".coffee", ".js"), new StringReader(compiled));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
