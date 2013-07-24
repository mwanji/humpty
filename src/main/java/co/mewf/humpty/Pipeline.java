package co.mewf.humpty;

import co.mewf.humpty.caches.AssetCache;
import co.mewf.humpty.config.Bundle;
import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.Context;
import co.mewf.humpty.config.PreProcessorContext;
import co.mewf.humpty.resolvers.AssetFile;
import co.mewf.humpty.resolvers.Resolver;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

public class Pipeline {

  private final Configuration configuration;
  private final List<Resolver> resolvers;
  private final List<AssetProcessor> assetProcessors;
  private final List<BundleProcessor> bundleProcessors;
  private final List<CompilingProcessor> compilingProcessors;
  private final AssetCache cache;

  public Pipeline(Configuration configuration, AssetCache cache, List<? extends Resolver> resolvers, List<? extends CompilingProcessor> compilingProcessors, List<? extends AssetProcessor> assetProcessors, List<? extends BundleProcessor> bundleProcessors) {
    this.configuration = configuration;
    this.cache = cache;
    this.resolvers = Collections.unmodifiableList(resolvers);
    this.compilingProcessors = Collections.unmodifiableList(compilingProcessors);
    this.assetProcessors = Collections.unmodifiableList(assetProcessors);
    this.bundleProcessors = Collections.unmodifiableList(bundleProcessors);
  }

  public Reader process(String originalAssetName) {
    if (cache.contains(originalAssetName)) {
      return cache.get(originalAssetName);
    }

    String bundleName = originalAssetName;
    if (configuration.isTimestamped()) {
      bundleName = stripTimestamp(originalAssetName);
    }

    Bundle bundle = null;
    for (Bundle candidate : configuration.getBundles()) {
      if (candidate.accepts(bundleName)) {
        bundle = candidate;
        break;
      }
    }

    Context context = new Context(configuration.getMode(), bundle);
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

      List<AssetFile> assetFiles = resolver.resolve(filteredAsset, context);

      for (AssetFile assetFile : assetFiles) {
        try {
          String assetName = assetFile.getPath();
          if (configuration.isTimestamped()) {
            assetName = FilenameUtils.getPath(assetName) + FilenameUtils.getBaseName(assetName) + "-humpty" + originalAssetName.substring(originalAssetName.indexOf("-humpty") + "-humpty".length()) + "." + FilenameUtils.getExtension(assetName);
          }
          String processedAssetString;
          if (cache.contains(assetName)) {
            processedAssetString = IOUtils.toString(cache.get(assetName));
          } else {
            Reader asset = assetFile.getReader();
            PreProcessorContext preprocessorContext = context.getPreprocessorContext(assetName);

            CompilingProcessor.CompilationResult compilationResult = compile(assetName, asset, preprocessorContext);
            Reader processedAsset = processAsset(compilationResult.getAssetName(), compilationResult.getAsset(), preprocessorContext);
            processedAssetString = IOUtils.toString(processedAsset);
            cache.put(bundle, assetName, processedAssetString);
          }
          bundleString.append(processedAssetString);
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
      cache.put(bundle, originalAssetName, processedBundleString);
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
