package co.mewf.humpty.config;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.webjars.WebJarAssetLocator;

import co.mewf.humpty.Pipeline;
import co.mewf.humpty.spi.PipelineElement;
import co.mewf.humpty.spi.bundles.BundleResolver;
import co.mewf.humpty.spi.listeners.PipelineListener;
import co.mewf.humpty.spi.processors.AssetProcessor;
import co.mewf.humpty.spi.processors.BundleProcessor;
import co.mewf.humpty.spi.processors.SourceProcessor;
import co.mewf.humpty.spi.resolvers.Resolver;

/**
 * <b>By default</b>
 * <p>Uses humpty.json at root of classpath to load the {@link Configuration} (eg. src/main/resources in a Maven project).
 *
 * Uses a {@link ServiceLoader} to get the {@link Resolver}s, {@link AssetProcessor}s and {@link BundleProcessor}s.
 *
 * <b>Configuration</b>
 * <p>Falls back to {@link ServiceLoader} on a per-resource type basis if none is provided.</p>
 *
 */
public class HumptyBootstrap implements PipelineElement {

  private final List<PipelineElement> pipelineElements;
  private final Object[] resources;
  private final List<BundleResolver> bundleResolvers;
  private final List<Resolver> resolvers;
  private final List<SourceProcessor> sourceProcessors;
  private final List<BundleProcessor> bundleProcessors;
  private final List<AssetProcessor> assetProcessors;
  private final Configuration.Options humptyOptions;
  private final List<PipelineListener> pipelineListeners;
  private Pipeline pipeline;
  private final Configuration configuration;
  private final WebJarAssetLocator locator;
  
  public HumptyBootstrap(Object... resources) {
    this("/humpty.toml", resources);
  }
  public HumptyBootstrap(String humptyFile, Object... resources) {
    this(Configuration.load(humptyFile), resources);
  }
  
  public HumptyBootstrap(Configuration configuration, Object... resources) {
    this.configuration = configuration;
    this.resources = resources;
    this.humptyOptions = configuration.getOptionsFor(this);
    this.pipelineElements = loadPipelineElements();
    this.bundleResolvers = getElements(BundleResolver.class, getConfiguration("bundleResolvers"));
    this.resolvers = getElements(Resolver.class, Optional.empty());
    this.sourceProcessors = getElements(SourceProcessor.class, getConfiguration("sources"));
    this.assetProcessors = getElements(AssetProcessor.class, getConfiguration("assets"));
    this.bundleProcessors = getElements(BundleProcessor.class, getConfiguration("bundles"));
    this.pipelineListeners = getElements(PipelineListener.class, getConfiguration("listeners"));
    this.locator = Arrays.stream(resources)
      .filter(resource -> resource instanceof WebJarAssetLocator)
      .map(resource -> (WebJarAssetLocator) resource)
      .findFirst()
      .orElseGet(this::getDefaultLocator);
    this.pipeline = new Pipeline(bundleResolvers, resolvers, sourceProcessors, assetProcessors, bundleProcessors, pipelineListeners);
    
    bundleResolvers.forEach(this::inject);
    resolvers.forEach(this::inject);
    sourceProcessors.forEach(this::inject);
    assetProcessors.forEach(this::inject);
    bundleProcessors.forEach(this::inject);
    pipelineListeners.forEach(this::inject);
  }
  
  @Override
  public String getName() {
    return "pipeline";
  }

  public Pipeline createPipeline() {
    return pipeline;
  }
  
  private List<PipelineElement> loadPipelineElements() {
    List<PipelineElement> elements = new ArrayList<>();
    ServiceLoader.load(PipelineElement.class).forEach(elements::add);

    return elements;
  }
  
  private Optional<List<String>> getConfiguration(String key) {
    Map<String, List<String>> elements = humptyOptions.get("elements", Collections.<String, List<String>>emptyMap());
    
    return Optional.ofNullable(elements.get(key));
  }
  
  private <T extends PipelineElement> List<T> getElements(Class<T> elementClass, Optional<List<String>> configuration) {
    Stream<T> stream = pipelineElements.stream()
      .filter(e -> elementClass.isAssignableFrom(e.getClass()))
      .map(e -> elementClass.cast(e));

    if (configuration.isPresent()) {
      List<String> conf = configuration.get();
      stream = stream
        .filter(e -> conf.contains(e.getName()))
        .sorted((e1, e2) -> conf.indexOf(e1.getName()) < conf.indexOf(e2.getName()) ? -1 : 1);
    }

    return stream.collect(toList());
  }
  
  private void inject(PipelineElement element) {
    Stream.of(element.getClass().getMethods())
    .filter(m -> m.isAnnotationPresent(Inject.class))
    .forEach(method -> {
      Class<?>[] parameterTypes = method.getParameterTypes();
      Object[] args = new Object[parameterTypes.length];
      for (int i = 0; i < parameterTypes.length; i++) {
        Class<?> parameterType = parameterTypes[i];
        if (parameterType == Pipeline.class) {
          args[i] = pipeline;
        } else if (parameterType == WebJarAssetLocator.class) {
          args[i] = locator;
        } else if (parameterType == Configuration.class) {
          args[i] = configuration;
        } else if (parameterType == Configuration.Options.class) {
          args[i] = configuration.getOptionsFor(element);
        } else if (parameterType == Configuration.GlobalOptions.class) {
          args[i] = configuration.getGlobalOptions();
        } else {
          args[i] = getExtra(parameterType).orElseThrow(() -> new IllegalArgumentException("Cannot inject the type " + parameterType.getName() + " into " + element.getClass().getName()));
        }
      }

      try {
        method.invoke(element, args);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  private Optional<Object> getExtra(Class<?> extra) {
    for (Object candidate : resources) {
      if (extra.isAssignableFrom(candidate.getClass())) {
        return Optional.of(candidate);
      }
    }

    return Optional.empty();
  }
  
  private WebJarAssetLocator getDefaultLocator() {
    try {
      String assetsDir = configuration.getGlobalOptions().getAssetsDir().toString();
      Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(assetsDir);
      if (!assetsDir.startsWith("/")) {
        assetsDir = "/" + assetsDir;
      }
      if (!assetsDir.endsWith("/")) {
        assetsDir += "/";
      }
      String fullAssetsDir = assetsDir;
      
      Set<String> assetPaths = new HashSet<>();
      
      while (urls.hasMoreElements()) {
        URL url = urls.nextElement();
        Files.walk(Paths.get(url.getFile()))
          .filter(path -> path.toFile().isFile())
          .map(Path::toString)
          .map(path -> path.substring(path.indexOf(fullAssetsDir) + 1))
          .forEach(assetPaths::add);
      }
      
      assetPaths.addAll(new WebJarAssetLocator().getFullPathIndex().values());
      
      return new WebJarAssetLocator(assetPaths);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
