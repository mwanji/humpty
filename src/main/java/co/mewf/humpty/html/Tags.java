package co.mewf.humpty.html;

import co.mewf.humpty.Resolver;
import co.mewf.humpty.config.Bundle;
import co.mewf.humpty.config.Configuration;

import java.util.List;

public class Tags {

  private final Configuration configuration;
  private final List<? extends Resolver> resolvers;

  public Tags(Configuration configuration, List<? extends Resolver> resolvers) {
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

    for (String asset : bundle.getBundleFor(bundleName)) {
      for (Resolver resolver : resolvers) {
        if (resolver.accepts(asset)) {
          String expandedAsset = resolver.expand(asset, bundleName);
          toHtml(contextPath, expandedAsset, html);
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
    if (contextPath.endsWith("/") && expandedAsset.startsWith("/")) {
      html.deleteCharAt(html.length() - 1);
    }
    html.append(expandedAsset);
    if (assetBaseName.endsWith(".js")) {
      html.append("\"></script>");
    } else if (assetBaseName.endsWith(".css")) {
      html.append("\" />");
    }
    html.append("\n");
  }
}
