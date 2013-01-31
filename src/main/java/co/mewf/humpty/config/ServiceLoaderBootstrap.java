package co.mewf.humpty.config;

import co.mewf.humpty.Pipeline;
import co.mewf.humpty.PostProcessor;
import co.mewf.humpty.PreProcessor;
import co.mewf.humpty.Resolver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ServiceLoader;

public class ServiceLoaderBootstrap implements Bootstrap {
  @Override
  public Pipeline createPipeline() {
    Configuration configuration = getConfiguration();
    ArrayList<Resolver> resolvers = getResolvers();
    ArrayList<PreProcessor> preProcessors = getPreProcessors();
    ArrayList<PostProcessor> postProcessors = getPostProcessors();

    return new Pipeline(configuration, resolvers, preProcessors, postProcessors);
  }

  public Configuration getConfiguration() {
    ConfigurationProvider configurationProvider = null;
    Iterator<ConfigurationProvider> iterator = ServiceLoader.load(ConfigurationProvider.class).iterator();
    if (iterator.hasNext()) {
      configurationProvider = iterator.next();
    } else {
      configurationProvider = new JsonConfigurationProvider();
    }

    return configurationProvider.getConfiguration();
  }

  public ArrayList<Resolver> getResolvers() {
    ArrayList<Resolver> resolvers = new ArrayList<Resolver>();
    Iterator<Resolver> resolverIterator = ServiceLoader.load(Resolver.class).iterator();
    while (resolverIterator.hasNext()) {
      resolvers.add(resolverIterator.next());
    }
    return resolvers;
  }

  public ArrayList<PreProcessor> getPreProcessors() {
    ArrayList<PreProcessor> preProcessors = new ArrayList<PreProcessor>();
    Iterator<PreProcessor> processorIterator = ServiceLoader.load(PreProcessor.class).iterator();
    while (processorIterator.hasNext()) {
      preProcessors.add(processorIterator.next());
    }
    return preProcessors;
  }

  public ArrayList<PostProcessor> getPostProcessors() {
    ArrayList<PostProcessor> postProcessors = new ArrayList<PostProcessor>();
    Iterator<PostProcessor> postProcessorIterator = ServiceLoader.load(PostProcessor.class).iterator();
    while (postProcessorIterator.hasNext()) {
      postProcessors.add(postProcessorIterator.next());
    }
    return postProcessors;
  }
}
