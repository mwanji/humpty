package co.mewf.humpty.config;

import co.mewf.humpty.Pipeline;
import co.mewf.humpty.PostProcessor;
import co.mewf.humpty.PreProcessor;
import co.mewf.humpty.Resolver;

import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
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
    Iterator<Resolver> resolverIterator = ServiceLoader.load(Resolver.class).iterator();
    while (resolverIterator.hasNext()) {
      resolvers.add(resolverIterator.next());
    }
    return resolvers;
  }

  protected List<PreProcessor> getPreProcessors() {
    ArrayList<PreProcessor> preProcessors = new ArrayList<PreProcessor>();
    Iterator<PreProcessor> processorIterator = ServiceLoader.load(PreProcessor.class).iterator();
    while (processorIterator.hasNext()) {
      preProcessors.add(processorIterator.next());
    }
    return preProcessors;
  }

  protected List<PostProcessor> getPostProcessors() {
    ArrayList<PostProcessor> postProcessors = new ArrayList<PostProcessor>();
    Iterator<PostProcessor> postProcessorIterator = ServiceLoader.load(PostProcessor.class).iterator();
    while (postProcessorIterator.hasNext()) {
      postProcessors.add(postProcessorIterator.next());
    }
    return postProcessors;
  }
}
