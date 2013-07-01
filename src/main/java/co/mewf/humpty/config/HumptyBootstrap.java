package co.mewf.humpty.config;

import co.mewf.humpty.Pipeline;
import co.mewf.humpty.PostProcessor;
import co.mewf.humpty.PreProcessor;
import co.mewf.humpty.Resolver;

import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class HumptyBootstrap {

  private final Gson gson = new Gson();

  public Pipeline createPipeline() {
    Configuration configuration = getConfiguration();
    List<Resolver> resolvers = getResolvers();
    List<PreProcessor> preProcessors = getPreProcessors();
    List<PostProcessor> postProcessors = getPostProcessors();

    return new Pipeline(configuration, resolvers, preProcessors, postProcessors);
  }

  protected Configuration getConfiguration() {
    return gson.fromJson(new InputStreamReader(getClass().getResourceAsStream("/humpty.json")), Configuration.class);
  }

  protected List<Resolver> getResolvers() {
    ArrayList<Resolver> resolvers = new ArrayList<Resolver>();
    ServiceLoader<Resolver> serviceLoader = ServiceLoader.load(Resolver.class);
    for (Resolver resolver : serviceLoader) {
      resolvers.add(resolver);
    }

    return resolvers;
  }

  protected List<PreProcessor> getPreProcessors() {
    ArrayList<PreProcessor> preProcessors = new ArrayList<PreProcessor>();
    ServiceLoader<PreProcessor> serviceLoader = ServiceLoader.load(PreProcessor.class);
    for (PreProcessor preProcessor : serviceLoader) {
      preProcessors.add(preProcessor);
    }

    return preProcessors;
  }

  protected List<PostProcessor> getPostProcessors() {
    ArrayList<PostProcessor> postProcessors = new ArrayList<PostProcessor>();
    ServiceLoader<PostProcessor> serviceLoader = ServiceLoader.load(PostProcessor.class);
    for (PostProcessor postProcessor : serviceLoader) {
      postProcessors.add(postProcessor);
    }

    return postProcessors;
  }
}
