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

import org.apache.commons.io.FilenameUtils;
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

  public Reader process(String originalAssetName) {
    if (cache.containsKey(originalAssetName)) {
      return new StringReader(cache.get(originalAssetName));
    }

    String bundleName = originalAssetName;
    if (configuration.isTimestamped()) {
      bundleName = stripTimestamp(originalAssetName);
    }

    Context context = new Context(configuration.getMode(), bundleName);
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
      cache.putIfAbsent(originalAssetName, processedBundleString);
      return new StringReader(processedBundleString);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String stripTimestamp(String name) {
    String baseName = FilenameUtils.getBaseName(name);
    String extension = FilenameUtils.getExtension(name);

    int dashLastIndex = baseName.lastIndexOf('-');
    if (dashLastIndex == -1) {
      return name;
    }

    String timestamp = baseName.substring(dashLastIndex + 1);

    if (!timestamp.startsWith("humpty")) {
      return name;
    }

    return baseName.substring(0, dashLastIndex) + "." + extension;
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
