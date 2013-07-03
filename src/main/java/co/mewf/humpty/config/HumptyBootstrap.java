package co.mewf.humpty.config;

import co.mewf.humpty.CompilingProcessor;
import co.mewf.humpty.Pipeline;
import co.mewf.humpty.PostProcessor;
import co.mewf.humpty.PreProcessor;
import co.mewf.humpty.Processor;
import co.mewf.humpty.Resolver;
import co.mewf.humpty.config.gson.GsonClassAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * <b>By default</b>
 * <p>Uses humpty.json at root of classpath to load the {@link Configuration} (eg. src/main/resources in a Maven project).
 *
 * Uses a {@link ServiceLoader} to get the {@link Resolver}s, {@link PreProcessor}s and {@link PostProcessor}s.
 *
 * Extend and override the appropriate methods to customise how these resources are located, how they are ordered, etc.</p>
 *
 * <b>Constructor configuration</b>
 * <p>{@link HumptyBootstrap#HumptyBootstrap(Object...)} can be used to specify which Configuration, Resolvers and Processors to use,
 * as well as their order.</p>
 *
 */
public class HumptyBootstrap {

  private final Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new GsonClassAdapter()).create();
  private final ServiceLoader<Processor> processors = ServiceLoader.load(Processor.class);
  private final Object[] resources;

  public HumptyBootstrap(Object... resources) {
    this.resources = resources;
  }

  public Pipeline createPipeline() {
    Configuration configuration = getConfiguration();
    List<? extends Resolver> resolvers = getResolvers();
    List<? extends PreProcessor> preProcessors = getPreProcessors();
    List<? extends PostProcessor> postProcessors = getPostProcessors();
    List<? extends CompilingProcessor> compilingProcessors = getCompilingProcessors();

    return new Pipeline(configuration, resolvers, compilingProcessors, preProcessors, postProcessors);
  }

  protected Configuration getConfiguration() {
    for (Object resource : resources) {
      if (resource instanceof Configuration) {
        return (Configuration) resource;
      }
    }

    return gson.fromJson(new InputStreamReader(getClass().getResourceAsStream("/humpty.json")), Configuration.class);
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

  protected List<? extends PreProcessor> getPreProcessors() {
    ArrayList<PreProcessor> preProcessors = new ArrayList<PreProcessor>();

    for (Object resource : resources) {
      if (resource instanceof PreProcessor) {
        preProcessors.add((PreProcessor) resource);
      }
    }

    if (!preProcessors.isEmpty()) {
      return preProcessors;
    }

    for (Processor processor : processors) {
      if (processor instanceof PreProcessor) {
        preProcessors.add((PreProcessor) processor);
      }
    }

    return preProcessors;
  }

  protected List<? extends PostProcessor> getPostProcessors() {
    ArrayList<PostProcessor> postProcessors = new ArrayList<PostProcessor>();

    for (Object resource : resources) {
      if (resource instanceof PostProcessor) {
        postProcessors.add((PostProcessor) resource);
      }
    }

    if (!postProcessors.isEmpty()) {
      return postProcessors;
    }

    for (Processor processor : processors) {
      if (processor instanceof PostProcessor) {
        postProcessors.add((PostProcessor) processor);
      }
    }

    return postProcessors;
  }
}
