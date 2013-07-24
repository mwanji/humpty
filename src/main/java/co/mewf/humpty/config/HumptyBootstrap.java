package co.mewf.humpty.config;

import co.mewf.humpty.AssetProcessor;
import co.mewf.humpty.BundleProcessor;
import co.mewf.humpty.CompilingProcessor;
import co.mewf.humpty.Pipeline;
import co.mewf.humpty.Processor;
import co.mewf.humpty.Resolver;
import co.mewf.humpty.caches.AssetCache;
import co.mewf.humpty.caches.SimpleAssetCache;
import co.mewf.humpty.caches.WatchingAssetCache;
import co.mewf.humpty.html.Includes;

import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import javax.inject.Inject;

import org.webjars.WebJarAssetLocator;

/**
 * <b>By default</b>
 * <p>Uses humpty.json at root of classpath to load the {@link Configuration} (eg. src/main/resources in a Maven project).
 *
 * Uses a {@link ServiceLoader} to get the {@link Resolver}s, {@link AssetProcessor}s and {@link BundleProcessor}s.
 *
 * <p>Extend and override the appropriate methods to customise how these resources are located, how they are ordered, etc.</p>
 *
 * <b>Configuration</b>
 * <p>Use {@link HumptyBootstrap.Builder} to construct a custom bootstrapper, e.g. to specify the order in which processors must be used.</p>
 * <p>Falls back to {@link ServiceLoader} on a per-resource type basis if none is provided.</p>
 *
 */
public class HumptyBootstrap {

  public static class Builder {
    private Config config = new Config();

    /**
     * @param humptyFile The path to the humpty configuration file on the classpath. Must start with a slash, e.g. /app/config/my-humpty.json
     */
    public Builder humptyFile(String humptyFile) {
      config.humptyFile = humptyFile;
      return this;
    }

    /**
     * @param resources Can contain a {@link Configuration}, {@link Resolver}s, {@link AssetProcessor}s and {@link BundleProcessor}s.
     * At runtime, the resources are used in declaration order.
     */
    public HumptyBootstrap build(Object... resources) {
      return new HumptyBootstrap(config, resources);
    }
  }

  private static class Config {
    String humptyFile = "/humpty.json";
  }

  private final Gson gson = new Gson();
  private final ServiceLoader<Processor> processors = ServiceLoader.load(Processor.class);
  private final Object[] resources;
  private final Config config;
  private Configuration configuration;
  private AssetCache assetCache;
  private List<? extends Resolver> resolvers;
  private List<Object> extras;
  private List<? extends CompilingProcessor> compilingProcessors;
  private List<? extends BundleProcessor> bundleProcessors;
  private List<? extends AssetProcessor> assetProcessors;

  HumptyBootstrap(Object... resources) {
    this(new Config(), resources);
  }

  public Pipeline createPipeline() {
    return new Pipeline(configuration, assetCache, resolvers, compilingProcessors, assetProcessors, bundleProcessors);
  }

  public Includes createTags() {
    return new Includes(configuration, resolvers);
  }

  protected Configuration getConfiguration() {
    for (Object resource : resources) {
      if (resource instanceof Configuration) {
        return (Configuration) resource;
      }
    }

    return gson.fromJson(new InputStreamReader(getClass().getResourceAsStream(config.humptyFile)), Configuration.class);
  }

  protected AssetCache getAssetCache(Configuration configuration) {
    return configuration.getMode() == Configuration.Mode.PRODUCTION ? new SimpleAssetCache() : new WatchingAssetCache();
  }

  protected List<? extends Resolver> getResolvers() {
    ArrayList<Resolver> resolvers = new ArrayList<Resolver>();

    for (Object resource : resources) {
      if (resource instanceof Resolver) {
        resolvers.add((Resolver) resource);
      }
    }

    if (!resolvers.isEmpty()) {
      return resolvers;
    }

    ServiceLoader<Resolver> serviceLoader = ServiceLoader.load(Resolver.class);
    for (Resolver resolver : serviceLoader) {
      resolvers.add(resolver);
    }

    return resolvers;
  }

  protected List<? extends CompilingProcessor> getCompilingProcessors() {
    List<CompilingProcessor> compilingProcessors = new ArrayList<CompilingProcessor>();

    for (Object resource : resources) {
      if (resource instanceof CompilingProcessor) {
        compilingProcessors.add((CompilingProcessor) resource);
      }
    }

    if (!compilingProcessors.isEmpty()) {
      return compilingProcessors;
    }

    for (Processor processor : processors) {
      if (processor instanceof CompilingProcessor) {
        compilingProcessors.add((CompilingProcessor) processor);
      }
    }

    return compilingProcessors;

  }

  protected List<? extends AssetProcessor> getAssetProcessors() {
    ArrayList<AssetProcessor> preProcessors = new ArrayList<AssetProcessor>();

    for (Object resource : resources) {
      if (resource instanceof AssetProcessor) {
        preProcessors.add((AssetProcessor) resource);
      }
    }

    if (!preProcessors.isEmpty()) {
      return preProcessors;
    }

    for (Processor processor : processors) {
      if (processor instanceof AssetProcessor) {
        preProcessors.add((AssetProcessor) processor);
      }
    }

    return preProcessors;
  }

  protected List<? extends BundleProcessor> getBundleProcessors() {
    ArrayList<BundleProcessor> postProcessors = new ArrayList<BundleProcessor>();

    for (Object resource : resources) {
      if (resource instanceof BundleProcessor) {
        postProcessors.add((BundleProcessor) resource);
      }
    }

    if (!postProcessors.isEmpty()) {
      return postProcessors;
    }

    for (Processor processor : processors) {
      if (processor instanceof BundleProcessor) {
        postProcessors.add((BundleProcessor) processor);
      }
    }

    return postProcessors;
  }

  HumptyBootstrap(Config config, Object... resources) {
    this.config = config;
    this.resources = resources;

    this.configuration = getConfiguration();
    this.assetCache = getAssetCache(configuration);
    this.resolvers = getResolvers();
    this.assetProcessors = getAssetProcessors();
    this.bundleProcessors = getBundleProcessors();
    this.compilingProcessors = getCompilingProcessors();
    this.extras = getExtras();

    WebJarAssetLocator locator = new WebJarAssetLocator();

    List<Object> all = new ArrayList<Object>();
    all.add(assetCache);
    all.addAll(resolvers);
    all.addAll(compilingProcessors);
    all.addAll(assetProcessors);
    all.addAll(bundleProcessors);

    for (Object resource : all) {
      for (Method method : resource.getClass().getMethods()) {
        if (!method.isAnnotationPresent(Inject.class)) {
          continue;
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
          Class<?> parameterType = parameterTypes[i];
          if (parameterType == WebJarAssetLocator.class) {
            args[i] = locator;
          } else if (parameterType == Configuration.Options.class) {
            args[i] = configuration.getOptionsFor(resource.getClass());
          } else if (getExtra(extras, parameterType) != null) {
            args[i] = getExtra(extras, parameterType);
          } else {
            throw new IllegalArgumentException("Cannot inject the type " + parameterType.getName() + " into " + resource.getClass().getName());
          }
        }

        try {
          method.invoke(resource, args);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        break;
      }
    }
  }

  private List<Object> getExtras() {
    ArrayList<Object> extras = new ArrayList<Object>();

    for (Object resource : resources) {
      if (!(resource instanceof Processor || resource instanceof Configuration || resource instanceof Resolver)) {
        extras.add(resource);
      }
    }

    return extras;
  }

  private Object getExtra(List<Object> extras, Class<?> extra) {
    for (Object candidate : extras) {
      if (extra.isAssignableFrom(candidate.getClass())) {
        return candidate;
      }
    }

    return null;
  }
}
