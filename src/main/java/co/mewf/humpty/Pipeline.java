package co.mewf.humpty;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import co.mewf.humpty.config.Bundle;
import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.Configuration.Mode;
import co.mewf.humpty.config.Context;
import co.mewf.humpty.config.PreProcessorContext;
import co.mewf.humpty.spi.listeners.PipelineListener;
import co.mewf.humpty.spi.processors.AssetProcessor;
import co.mewf.humpty.spi.processors.BundleProcessor;
import co.mewf.humpty.spi.processors.SourceProcessor;
import co.mewf.humpty.spi.resolvers.AssetFile;
import co.mewf.humpty.spi.resolvers.Resolver;

public class Pipeline {

  private final List<Bundle> bundles;
  private final List<Resolver> resolvers;
  private final List<AssetProcessor> assetProcessors;
  private final List<BundleProcessor> bundleProcessors;
  private final List<SourceProcessor> compilingProcessors;
  private final Mode mode;
  private final List<PipelineListener> pipelineListeners;

  public Pipeline(List<Bundle> bundles, Configuration.Mode mode, List<? extends Resolver> resolvers, List<? extends SourceProcessor> compilingProcessors, List<? extends AssetProcessor> assetProcessors, List<? extends BundleProcessor> bundleProcessors, List<PipelineListener> pipelineListeners) {
    this.bundles = bundles;
    this.mode = mode;
    this.resolvers = Collections.unmodifiableList(resolvers);
    this.compilingProcessors = Collections.unmodifiableList(compilingProcessors);
    this.assetProcessors = Collections.unmodifiableList(assetProcessors);
    this.bundleProcessors = Collections.unmodifiableList(bundleProcessors);
    this.pipelineListeners = Collections.unmodifiableList(pipelineListeners);
  }

  public String process(String originalAssetName) {
    String bundleName = originalAssetName;
    String assetInBundleName = null;
    
    if (originalAssetName.indexOf('/') > -1) {
      String[] split = originalAssetName.split("/", 2);
      bundleName = split[0];
      assetInBundleName = split[1];
    }
    

    Bundle bundle = null;
    for (Bundle candidate : bundles) {
      if (candidate.accepts(bundleName)) {
        bundle = candidate;
        break;
      }
    }

    Context context = new Context(mode, bundle);
    if (assetInBundleName != null) {
      context = context.getChild(assetInBundleName);
    }
    List<String> filteredAssets = bundle.getBundleFor(assetInBundleName == null ? bundleName : assetInBundleName);
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
        String assetName = assetFile.getPath();

        String asset = assetFile.getContents();
        PreProcessorContext preprocessorContext = context.getPreprocessorContext(assetName);

        SourceProcessor.CompilationResult compilationResult = compile(assetName, asset, preprocessorContext);
        String processedAsset = processAsset(compilationResult.getAssetName(), compilationResult.getAsset(), preprocessorContext);
        bundleString.append(processedAsset);
        if (bundleString.charAt(bundleString.length() - 1) != '\n') {
          bundleString.append('\n');
        }
      }
    }


    if (assetInBundleName != null) {
      return bundleString.toString();
    }
    
    try {
      return processBundle(bundleString.toString(), context);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  public <T extends PipelineListener> T getPipelineListener(Class<T> pipelineListenerClass) {
    return pipelineListenerClass.cast(pipelineListeners.stream().filter(l -> l.getClass() == pipelineListenerClass).findFirst().get());
  }

  private SourceProcessor.CompilationResult compile(String assetName, String asset, PreProcessorContext context) {
    SourceProcessor.CompilationResult compilationResult = new SourceProcessor.CompilationResult(assetName, asset);
    for (SourceProcessor processor : compilingProcessors) {
      if (processor.accepts(compilationResult.getAssetName())) {
        compilationResult = processor.compile(compilationResult.getAssetName(), compilationResult.getAsset(), context);
      }
    }
    
    return compilationResult;
  }

  private String processAsset(String assetName, String initialAsset, PreProcessorContext context) {
    String processedAsset = assetProcessors.stream()
        .filter(a -> a.accepts(assetName))
        .reduce(initialAsset, (asset, processor) -> processor.processAsset(assetName, asset, context), (s1, s2) -> s2);
    
    pipelineListeners.forEach(listener -> listener.onAssetProcessed(processedAsset, assetName, context.getAssetUrl(), context.getBundle()));

    return processedAsset;
  }

  private String processBundle(String asset, Context context) throws IOException {
    String currentBundle = asset;
    for (BundleProcessor postProcessor : bundleProcessors) {
      if (postProcessor.accepts(context.getBundleName())) {
        currentBundle = postProcessor.processBundle(context.getBundleName(), currentBundle, context);
      }
    }

    String processedBundle = currentBundle;
    pipelineListeners.forEach(listener -> listener.onBundleProcessed(processedBundle, context.getBundleName()));
    
    return processedBundle;
  }
}
