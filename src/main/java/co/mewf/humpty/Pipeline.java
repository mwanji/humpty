package co.mewf.humpty;

import co.mewf.humpty.config.Bundle;
import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.Context;
import co.mewf.humpty.config.PreProcessorContext;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

public class Pipeline {

  private final Configuration configuration;
  private final List<Resolver> resolvers;
  private final List<PreProcessor> preProcessors;
  private final List<PostProcessor> postProcessors;

  public Pipeline(Configuration configuration, List<? extends Resolver> resolvers, List<? extends PreProcessor> preProcessors, List<? extends PostProcessor> postProcessors) {
    this.configuration = configuration;
    this.resolvers = Collections.unmodifiableList(resolvers);
    this.preProcessors = Collections.unmodifiableList(preProcessors);
    this.postProcessors = Collections.unmodifiableList(postProcessors);
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

      Reader asset = resolver.resolve(filteredAsset, context);
      try {
        Reader preProcessedAsset = preProcess(filteredAsset.substring(filteredAsset.indexOf(':') + 1), asset, context.getPreprocessorContext(resolver.expand(filteredAsset)));
        bundleString.append(IOUtils.toString(preProcessedAsset));
        if (bundleString.charAt(bundleString.length() - 1) != '\n') {
          bundleString.append('\n');
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    return postProcess(new StringReader(bundleString.toString()), context);
  }

  private Reader preProcess(String assetName, Reader asset, PreProcessorContext context) {
    Reader currentAsset = asset;
    for (PreProcessor preProcessor : preProcessors) {
      if (preProcessor.canProcess(assetName)) {
        currentAsset = preProcessor.process(assetName, currentAsset, configuration.getOptionsFor(preProcessor.getClass()), context);
      }
    }

    return currentAsset;
  }

  private Reader postProcess(Reader asset, Context context) {
    Reader currentAsset = asset;
    for (PostProcessor postProcessor : postProcessors) {
      if (postProcessor.canProcess(context.getBundleName())) {
        currentAsset = postProcessor.process(context.getBundleName(), currentAsset, configuration.getOptionsFor(postProcessor.getClass()), context);
      }
    }

    return currentAsset;
  }
}
