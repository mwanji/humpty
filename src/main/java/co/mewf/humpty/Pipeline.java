package co.mewf.humpty;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

import co.mewf.humpty.config.Bundle;
import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.Configuration.Mode;
import co.mewf.humpty.config.Context;
import co.mewf.humpty.config.PreProcessorContext;
import co.mewf.humpty.spi.listeners.PipelineListener;
import co.mewf.humpty.spi.processors.AssetProcessor;
import co.mewf.humpty.spi.processors.BundleProcessor;
import co.mewf.humpty.spi.processors.SourceProcessor;
import co.mewf.humpty.spi.processors.SourceProcessor.CompilationResult;
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
    final String bundleName;
    final String assetInBundleName;
    
    if (originalAssetName.indexOf('/') > -1) {
      String[] split = originalAssetName.split("/", 2);
      bundleName = split[0];
      assetInBundleName = split[1];
    } else {
      bundleName = originalAssetName;
      assetInBundleName = null;
    }
    
    Bundle bundle = bundles.stream().filter(b -> b.accepts(bundleName)).findFirst().orElseThrow(illegal("There is no bundle named " + bundleName));

    if (assetInBundleName != null) {
      bundle = bundle.getBundleFor(assetInBundleName);
    }

    Context context = new Context(mode, bundle);
    
    StringBuilder bundleString = new StringBuilder();
    for (String filteredAsset : bundle) {
      Resolver resolver = resolvers.stream().filter(r -> r.accepts(filteredAsset)).findFirst().orElseThrow(illegal("There is no resolver for asset: " + filteredAsset));
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

    String rawBundle = bundleString.toString();
    
    try {
      return processBundle(rawBundle, context);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  public <T extends PipelineListener> T getPipelineListener(Class<T> pipelineListenerClass) {
    return pipelineListenerClass.cast(pipelineListeners.stream().filter(l -> l.getClass() == pipelineListenerClass).findFirst().orElseThrow(illegal("There is no listener configured for " + pipelineListenerClass.getName())));
  }

  private CompilationResult compile(String assetName, String asset, PreProcessorContext context) {
    return compilingProcessors.stream().reduce(new CompilationResult(assetName, asset), SourceProcessor.maybe(assetName, context), ignored());
  }

  private String processAsset(String assetName, String initialAsset, PreProcessorContext context) {
    String processedAsset = assetProcessors.stream().filter(p -> p.accepts(assetName))
        .reduce(initialAsset, (asset, processor) -> processor.processAsset(assetName, asset, context), ignored());
    
    pipelineListeners.forEach(listener -> listener.onAssetProcessed(processedAsset, assetName, context.getAssetUrl(), context.getBundle()));

    return processedAsset;
  }
  
  private String processBundle(String asset, Context context) throws IOException {
    String processedBundle = bundleProcessors.stream().filter(p -> p.accepts(context.getBundleName()))
        .reduce(asset, (a, p) -> p.processBundle(context.getBundleName(), a, context), ignored());

    pipelineListeners.forEach(listener -> listener.onBundleProcessed(processedBundle, context.getBundleName()));
    
    return processedBundle;
  }
  
  private <T> BinaryOperator<T> ignored() {
    return (a, b) -> b;
  }
  
  private Supplier<? extends RuntimeException> illegal(String message) {
    return () -> new IllegalArgumentException(message);
  }
}
