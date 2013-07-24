package co.mewf.humpty.html;

import co.mewf.humpty.Resolver;
import co.mewf.humpty.config.Bundle;
import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.Context;
import co.mewf.humpty.resolvers.AssetFile;

import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTimeUtils;

public class Includes {

  private final Configuration configuration;
  private final List<? extends Resolver> resolvers;

  public Includes(Configuration configuration, List<? extends Resolver> resolvers) {
    this.configuration = configuration;
    this.resolvers = resolvers;
  }

  public String generate(String bundleName, String contextPath) {
    StringBuilder html = new StringBuilder();

    if (Configuration.Mode.PRODUCTION == configuration.getMode()) {
      toHtml(contextPath, "/" + bundleName, html);

      return html.toString();
    }

    Bundle bundle = null;
    for (Bundle candidate : configuration.getBundles()) {
      if (candidate.accepts(bundleName)) {
        bundle = candidate;
        break;
      }
    }

		Context context = new Context(configuration.getMode(), bundle);
    for (String asset : bundle.getBundleFor(bundleName)) {
      for (Resolver resolver : resolvers) {
        if (resolver.accepts(asset)) {
          List<AssetFile> assetFiles = resolver.resolve(asset, context);
          for (AssetFile assetFile : assetFiles) {
            toHtml(contextPath, assetFile.getPath(), html);
          }
          break;
        }
      }
    }

    return html.toString();
  }

  private void toHtml(String contextPath, String expandedAsset, StringBuilder html) {
    String assetBaseName = expandedAsset;
    if (assetBaseName.endsWith(".js")) {
      html.append("<script src=\"");
    } else if (assetBaseName.endsWith(".css")) {
      html.append("<link rel=\"stylesheet\" href=\"");
    }
    html.append(contextPath);
    if (html.charAt(html.length() - 1) != '/') {
      html.append('/');
    }
    html.append(FilenameUtils.getPath(expandedAsset));
    html.append(FilenameUtils.getBaseName(expandedAsset));
    if (configuration.isTimestamped()) {
      html.append("-humpty" + DateTimeUtils.currentTimeMillis());
    }
    html.append('.').append(FilenameUtils.getExtension(expandedAsset));
    if (assetBaseName.endsWith(".js")) {
      html.append("\"></script>");
    } else if (assetBaseName.endsWith(".css")) {
      html.append("\" />");
    }
    html.append("\n");
  }
}
