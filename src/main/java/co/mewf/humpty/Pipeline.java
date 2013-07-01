package co.mewf.humpty;

import co.mewf.humpty.config.Bundle;
import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.Context;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
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

  public Pipeline(Configuration configuration, List<Resolver> resolvers, List<PreProcessor> preProcessors, List<PostProcessor> postProcessors) {
    this.configuration = configuration;
    this.resolvers = Collections.unmodifiableList(resolvers);
    this.preProcessors = Collections.unmodifiableList(preProcessors);
    this.postProcessors = Collections.unmodifiableList(postProcessors);
  }

  public Reader process(String asset, HttpServletRequest request, HttpServletResponse response) {
    Context context = new Context(configuration.getMode(), request, response);
    Bundle matchingBundle = null;
    for (Bundle bundle : configuration.getBundles()) {
      if (bundle.accepts(asset)) {
        matchingBundle = bundle;
        break;
      }
    }

    List<String> filteredAssets = matchingBundle.getBundleFor(asset);
    StringWriter bundleString = new StringWriter();
    for (String filteredAsset : filteredAssets) {
      Resolver matchingResolver = null;
      for (Resolver resolver : resolvers) {
        if (resolver.accepts(filteredAsset)) {
          matchingResolver = resolver;
          break;
        }
      }

      Reader resolvedAsset = matchingResolver.resolve(filteredAsset, context);
      try {
        Reader preProcessedAsset = preProcess(filteredAsset.substring(asset.indexOf(':') + 1), resolvedAsset, context);
        IOUtils.copy(preProcessedAsset, bundleString);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    return postProcess(new StringReader(bundleString.toString()), context);
  }

  private Reader preProcess(String assetName, Reader asset, Context context) {
    Reader currentAsset = asset;
    for (PreProcessor preProcessor : preProcessors) {
      if (preProcessor.canPreProcess(assetName)) {
        currentAsset = preProcessor.preProcess(currentAsset, configuration.getOptionsFor(preProcessor.getClass().getName()), context);
      }
    }
    return currentAsset;
  }

  private Reader postProcess(Reader asset, Context context) {
    return asset;
  }
}
