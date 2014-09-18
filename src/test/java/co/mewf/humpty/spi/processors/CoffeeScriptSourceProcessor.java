package co.mewf.humpty.spi.processors;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.webjars.WebJarAssetLocator;

import co.mewf.humpty.config.PreProcessorContext;

public class CoffeeScriptSourceProcessor implements SourceProcessor {

  private static final String COFFEE_SCRIPT_JS = new WebJarAssetLocator().getFullPath("coffee-script.min.js");
  
  @Override
  public String getName() {
    return "coffee";
  }

  @Override
  public boolean accepts(String asset) {
    return asset.endsWith(".coffee");
  }

  @Override
  public CompilationResult compile(SourceProcessor.CompilationResult compilationResult, PreProcessorContext context) {
    ScriptEngine nashorn = new ScriptEngineManager().getEngineByName("nashorn");

    ClassLoader classLoader = getClass().getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(COFFEE_SCRIPT_JS);
    try (InputStreamReader coffeeScriptReader = new InputStreamReader(inputStream, "UTF-8")) {
      nashorn.eval(coffeeScriptReader);
      Object coffeeScript = nashorn.eval("CoffeeScript");
      HashMap<String, Object> options = new HashMap<String, Object>();
      options.put("bare", true);
      String compiled = (String) ((Invocable) nashorn).invokeMethod(coffeeScript, "compile", compilationResult.getAsset(), options);

      return new CompilationResult(compilationResult.getAssetName().replace(".coffee", ".js"), compiled);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
