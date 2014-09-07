package co.mewf.humpty.config;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import javax.inject.Inject;

import org.webjars.WebJarAssetLocator;

import co.mewf.humpty.Pipeline;
import co.mewf.humpty.spi.PipelineElement;
import co.mewf.humpty.spi.processors.AssetProcessor;
import co.mewf.humpty.spi.processors.BundleProcessor;
import co.mewf.humpty.spi.processors.Processor;
import co.mewf.humpty.spi.processors.SourceProcessor;
import co.mewf.humpty.spi.resolvers.Resolver;

/**
 * <b>By default</b>
 * <p>Uses humpty.json at root of classpath to load the {@link Configuration} (eg. src/main/resources in a Maven project).
 *
 * Uses a {@link ServiceLoader} to get the {@link Resolver}s, {@link AssetProcessor}s and {@link BundleProcessor}s.
 *
 * <b>Configuration</b>
 * <p>Use {@link HumptyBootstrap.Builder} to construct a custom bootstrapper, e.g. to specify the order in which processors must be used.</p>
 * <p>Falls back to {@link ServiceLoader} on a per-resource type basis if none is provided.</p>
 *
 */
public class HumptyBootstrap implements PipelineElement {

  private final List<PipelineElement> pipelineElements;
  private final Object[] resources;
  private final Configuration configuration;
  private final List<Resolver> resolvers;
  private final List<Object> extras;
  private final List<SourceProcessor> sourceProcessors;
  private final List<BundleProcessor> bundleProcessors;
  private final List<AssetProcessor> assetProcessors;
  private final Configuration.Options humptyOptions;
  private List<String> sourceProcessorsConfiguration;
  private List<String> assetProcessorsConfiguration;
  private List<String> bundleProcessorsConfiguration;
  
  public HumptyBootstrap(Object... resources) {
    this("/humpty.toml", resources);
  }

  public HumptyBootstrap(String humptyFile, Object... resources) {
    this.resources = resources;
    this.configuration = Configuration.load(humptyFile);
    this.humptyOptions = configuration.getOptionsFor(this);
    this.pipelineElements = loadPipelineElements();
    this.resolvers = getResolvers();
    setProcessorConfigurations();
    this.assetProcessors = getAssetProcessors();
    this.bundleProcessors = getBundleProcessors();
    this.sourceProcessors = getSourceProcessors();
    this.extras = getExtras();

    List<Object> all = new ArrayList<Object>();
    all.addAll(resolvers);
    all.addAll(sourceProcessors);
    all.addAll(assetProcessors);
    all.addAll(bundleProcessors);
    
    WebJarAssetLocator locator = new WebJarAssetLocator();

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
          } else if (parameterType == Configuration.Options.class && resource instanceof PipelineElement) {
            args[i] = configuration.getOptionsFor((PipelineElement) resource);
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
  
  public Pipeline createPipeline() {
    return new Pipeline(configuration, getMode(), resolvers, sourceProcessors, assetProcessors, bundleProcessors);
  }

  @Override
  public String getName() {
    return "humpty";
  }

  private List<Resolver> getResolvers() {
    return pipelineElements.stream().filter(e -> e instanceof Resolver).map(e -> (Resolver) e).collect(toList());
  }

  private List<SourceProcessor> getSourceProcessors() {
    List<SourceProcessor> sourceProcessors = pipelineElements.stream().filter(e -> e instanceof SourceProcessor).map(e -> (SourceProcessor) e).collect(toList());
    
    if (sourceProcessorsConfiguration == null) {
      return sourceProcessors;
    }

    return sourceProcessorsConfiguration.stream().map(name -> {
      return sourceProcessors.stream().filter(s -> s.getName().equals(name)).findFirst().get();
    }).collect(toList());
  }

  private List<AssetProcessor> getAssetProcessors() {
    List<AssetProcessor> assetProcessors = pipelineElements.stream().filter(e -> e instanceof AssetProcessor).map(e -> (AssetProcessor) e).collect(toList());
    
    if (assetProcessorsConfiguration == null) {
      return assetProcessors;
    }
    
    return assetProcessorsConfiguration.stream().map(name -> {
      return assetProcessors.stream().filter(a -> a.getName().equals(name)).findFirst().get();
    }).collect(toList());
  }

  private List<BundleProcessor> getBundleProcessors() {
    List<BundleProcessor> bundleProcessors = pipelineElements.stream().filter(e -> e instanceof BundleProcessor).map(e -> (BundleProcessor) e).collect(toList());
    
    if (bundleProcessorsConfiguration == null) {
      return bundleProcessors;
    }
    
    return bundleProcessorsConfiguration.stream().map(name -> {
      return bundleProcessors.stream().filter(b -> b.getName().equals(name)).findFirst().get();
    }).collect(toList());
  }

  private Configuration.Mode getMode() {
    Configuration.Mode mode = humptyOptions.containsKey("mode") ? Configuration.Mode.valueOf((String) humptyOptions.get("mode")) : Configuration.Mode.PRODUCTION;
    return mode;
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

  private List<PipelineElement> loadPipelineElements() {
    List<PipelineElement> elements = new ArrayList<>();
    ServiceLoader.load(PipelineElement.class).forEach(e -> elements.add(e));
    
    return elements;
  }

  private void setProcessorConfigurations() {
    @SuppressWarnings("unchecked")
    Map<String, List<String>> processorConfiguration = humptyOptions.containsKey("processors") ? (Map<String, List<String>>) humptyOptions.get("processors") : null;
    if (processorConfiguration != null) {
      sourceProcessorsConfiguration = processorConfiguration.get("sources");
      assetProcessorsConfiguration = processorConfiguration.get("assets");
      bundleProcessorsConfiguration = processorConfiguration.get("bundles");
    }
  }
}
