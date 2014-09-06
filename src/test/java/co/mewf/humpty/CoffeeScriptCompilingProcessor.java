package co.mewf.humpty;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.webjars.WebJarAssetLocator;

import co.mewf.humpty.config.PreProcessorContext;
import co.mewf.humpty.processors.CompilingProcessor;

public class CoffeeScriptCompilingProcessor implements CompilingProcessor {

  private static final String COFFEE_SCRIPT_JS = new WebJarAssetLocator().getFullPath("coffee-script.min.js");
  
  @Override
  public String getAlias() {
    return "coffee";
  }

  @Override
  public boolean accepts(String asset) {
    return asset.endsWith(".coffee");
  }

  @Override
  public CompilationResult compile(String assetName, Reader asset, PreProcessorContext context) {
    ClassLoader classLoader = getClass().getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(COFFEE_SCRIPT_JS);
    try {
      Reader csReader = new InputStreamReader(inputStream, "UTF-8");
      Context rhinoContext = Context.enter();
      Scriptable globalScope = rhinoContext.initStandardObjects();
      rhinoContext.evaluateReader(globalScope, csReader, "coffee-script.js", 0, null);

      Scriptable compileScope = rhinoContext.newObject(globalScope);
      compileScope.setParentScope(globalScope);
      compileScope.put("coffeeScriptSource", compileScope, IOUtils.toString(asset));

      StringReader compiledReader = new StringReader((String) rhinoContext.evaluateString(compileScope,
          "CoffeeScript.compile(coffeeScriptSource, { bare: true });", "JCoffeeScriptCompiler", 0, null));

      return new CompilationResult(assetName.replace(".coffee", ".js"), compiledReader);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      IOUtils.closeQuietly(inputStream);
      Context.exit();
    }
  }
}
