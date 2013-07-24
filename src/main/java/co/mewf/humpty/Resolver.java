package co.mewf.humpty;

import co.mewf.humpty.config.Context;
import co.mewf.humpty.resolvers.AssetFile;

import java.util.List;
import java.util.ServiceLoader;

/**
 * Uses a URI to locate an asset.
 *
 * Resolvers should be declared in META-INF/services/co.mewf.humpty.Resolver so they can be picked up by a {@link ServiceLoader}
 */
public interface Resolver {

  boolean accepts(String uri);
  List<AssetFile> resolve(String uri, Context context);
}
