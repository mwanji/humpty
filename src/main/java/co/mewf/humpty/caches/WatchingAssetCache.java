package co.mewf.humpty.caches;

import co.mewf.humpty.config.Bundle;
import co.mewf.humpty.resolvers.AssetFile;

import java.io.File;
import java.io.Reader;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.servlet.ServletContext;

/**
 * A cache that invalidates assets when they are modified, as well as the bundle containing the assets.
 */
public class WatchingAssetCache implements AssetCache {

  private final AssetWatcher assetWatcher;
  private final ConcurrentHashMap<String, AssetFile> cache = new ConcurrentHashMap<String, AssetFile>();
  private ServletContext servletContext;

  public WatchingAssetCache() {
    assetWatcher = new AssetWatcher(5000, new AssetChangeListener() {
      @Override
      public void fileChanged(File file) {
        AssetFile removed = null;
        for (Iterator<AssetFile> iterator = cache.values().iterator(); iterator.hasNext();) {
          AssetFile assetFile = iterator.next();
          if (file.getAbsolutePath().equals(assetFile.getPath())) {
            iterator.remove();
            removed = assetFile;
          }
        }
        if (removed != null) {
          cache.remove(removed.getBundle().getName());
        }
      }
    });
  }

  @Override
  public void put(Bundle bundle, String assetName, String asset) {
    cache.put(assetName, new AssetFile(bundle, assetName, asset));
    for (String bundledAsset : bundle.getBundleFor(bundle.getName())) {
      File file = new File(servletContext.getRealPath(bundledAsset));
      assetWatcher.watch(file);
    }
  }

  @Override
  public Reader get(String assetName) {
    return cache.get(assetName).getReader();
  }

  @Override
  public boolean contains(String assetName) {
    return cache.containsKey(assetName);
  }

  @Inject
  public void configure(ServletContext servletContext) {
    this.servletContext = servletContext;
  }
}
