package co.mewf.humpty;

import co.mewf.humpty.config.Bundle;
import co.mewf.humpty.config.Configurable;
import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.Context;
import co.mewf.humpty.config.PreProcessorContext;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

public class Pipeline {

  private final Configuration configuration;
  private final List<Resolver> resolvers;
  private final List<PreProcessor> preProcessors;
  private final List<PostProcessor> postProcessors;
  private final List<CompilingProcessor> compilingProcessors;

  public Pipeline(Configuration configuration, List<? extends Resolver> resolvers, List<? extends CompilingProcessor> compilingProcessors, List<? extends PreProcessor> preProcessors, List<? extends PostProcessor> postProcessors) {
    this.configuration = configuration;
    this.resolvers = Collections.unmodifiableList(resolvers);
    this.compilingProcessors = Collections.unmodifiableList(compilingProcessors);
    this.preProcessors = Collections.unmodifiableList(preProcessors);
    this.postProcessors = Collections.unmodifiableList(postProcessors);
    configure();
  }

  public Reader process(String bundleName, HttpServletRequest request, HttpServletResponse response) {
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
          Reader preProcessedAsset = preProcess(compilationResult.getAssetName(), compilationResult.getAsset(), preprocessorContext);
          bundleString.append(IOUtils.toString(preProcessedAsset));
          if (bundleString.charAt(bundleString.length() - 1) != '\n') {
            bundleString.append('\n');
          }

        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }

    return postProcess(new StringReader(bundleString.toString()), context);
  }

  private List<String> expandAssetName(String filteredAsset) {
    return new ArrayList<String>();
  }

  private void configure() {
    ArrayList<Processor> resources = new ArrayList<Processor>();
    resources.addAll(compilingProcessors);
    resources.addAll(preProcessors);
    resources.addAll(postProcessors);

    for (Processor resource : resources) {
      if (resource instanceof Configurable) {
        Configurable configurable = (Configurable) resource;
        configurable.configure(configuration.getOptionsFor(configurable));
      }
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

  private Reader preProcess(String assetName, Reader asset, PreProcessorContext context) {
    Reader currentAsset = asset;
    for (PreProcessor preProcessor : preProcessors) {
      if (preProcessor.accepts(assetName)) {
        currentAsset = preProcessor.preProcess(assetName, currentAsset, context);
      }
    }

    return currentAsset;
  }

  private Reader postProcess(Reader asset, Context context) {
    Reader currentAsset = asset;
    for (PostProcessor postProcessor : postProcessors) {
      if (postProcessor.accepts(context.getBundleName())) {
        currentAsset = postProcessor.postProcess(context.getBundleName(), currentAsset, context);
      }
    }

    return currentAsset;
  }
}
