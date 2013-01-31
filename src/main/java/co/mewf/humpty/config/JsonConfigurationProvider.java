package co.mewf.humpty.config;

import com.google.gson.Gson;

import java.io.InputStreamReader;

public class JsonConfigurationProvider implements ConfigurationProvider {

  private Gson gson = new Gson();

  @Override
  public Configuration getConfiguration() {
    Configuration configuration = gson.fromJson(new InputStreamReader(getClass().getResourceAsStream("/humpty.json")), Configuration.class);
    return configuration;
  }

}
