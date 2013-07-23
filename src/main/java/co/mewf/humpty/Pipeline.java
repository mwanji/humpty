package co.mewf.humpty;

import co.mewf.humpty.config.Bundle;
import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.Context;
import co.mewf.humpty.config.PreProcessorContext;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

public class Pipeline {

  private final Configuration configuration;
  private final List<Resolver> resolvers;
  private final List<AssetProcessor> assetProcessors;
  private final List<BundleProcessor> bundleProcessors;
  private final List<CompilingProcessor> compilingProcessors;
  private final ConcurrentMap<String, String> cache = new ConcurrentHashMap<String, String>();

  public Pipeline(Configuration configuration, List<? extends Resolver> resolvers, List<? extends CompilingProcessor> compilingProcessors, List<? extends AssetProcessor> assetProcessors, List<? extends BundleProcessor> bundleProcessors) {
    this.configuration = configuration;
    this.resolvers = Collections.unmodifiableList(resolvers);
    this.compilingProcessors = Collections.unmodifiableList(compilingProcessors);
    this.assetProcessors = Collections.unmodifiableList(assetProcessors);
    this.bundleProcessors = Collections.unmodifiableList(bundleProcessors);
  }

  public Reader process(String bundleName, HttpServletRequest request, HttpServletResponse response) {
    if (cache.containsKey(bundleName)) {
      return new StringReader(cache.get(bundleName));
    }

    Context context = new Context(configuration.getMode(), bundleName, request, response);
    Bundle bundle = null;
    for (Bundle candidate : configuration.getBundles()) {
      if (candidate.accepts(bundleName)) {
        bundle = candidate;
        break;
      }
    }

    List<String> filteredAssets = bundle.getBundleFor(bundleName);
    StringBuilder bundleString = new StringBuilder();
    for (String filteredAsset : filteredAssets) {
      Resolver resolver = null;
      for (Resolver candidate : resolvers) {
        if (candidate.accepts(filteredAsset)) {
          resolver = candidate;
          break;
        }
      }

      LinkedHashMap<String, ? extends Reader> assets = resolver.resolve(filteredAsset, context);

      for (Map.Entry<String, ? extends Reader> entry : assets.entrySet()) {
        try {
          String assetName = entry.getKey();
          Reader asset = entry.getValue();
          PreProcessorContext preprocessorContext = context.getPreprocessorContext(assetName);

          CompilingProcessor.CompilationResult compilationResult = compile(assetName, asset, preprocessorContext);
          Reader preProcessedAsset = processAsset(compilationResult.getAssetName(), compilationResult.getAsset(), preprocessorContext);
          bundleString.append(IOUtils.toString(preProcessedAsset));
          if (bundleString.charAt(bundleString.length() - 1) != '\n') {
            bundleString.append('\n');
          }

        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }

    Reader processedBundle = processBundle(new StringReader(bundleString.toString()), context);

    try {
      String processedBundleString = IOUtils.toString(processedBundle);
      cache.putIfAbsent(bundleName, processedBundleString);
      return new StringReader(processedBundleString);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private CompilingProcessor.CompilationResult compile(String assetName, Reader asset, PreProcessorContext context) {
    CompilingProcessor.CompilationResult compilationResult = new CompilingProcessor.CompilationResult(assetName, asset);
    for (CompilingProcessor processor : compilingProcessors) {
      if (processor.accepts(compilationResult.getAssetName())) {
        compilationResult = processor.compile(compilationResult.getAssetName(), compilationResult.getAsset(), context);
      }
    }
    return compilationResult;
  }

  private Reader processAsset(String assetName, Reader asset, PreProcessorContext context) {
    Reader currentAsset = asset;
    for (AssetProcessor preProcessor : assetProcessors) {
      if (preProcessor.accepts(assetName)) {
        currentAsset = preProcessor.processAsset(assetName, currentAsset, context);
      }
    }

    return currentAsset;
  }

  private Reader processBundle(Reader asset, Context context) {
    Reader currentAsset = asset;
    for (BundleProcessor postProcessor : bundleProcessors) {
      if (postProcessor.accepts(context.getBundleName())) {
        currentAsset = postProcessor.processBundle(context.getBundleName(), currentAsset, context);
      }
    }

    return currentAsset;
  }
}
