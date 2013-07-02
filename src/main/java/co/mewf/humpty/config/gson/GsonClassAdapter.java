package co.mewf.humpty.config.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class GsonClassAdapter implements JsonDeserializer<Class<?>> {

  @Override
  public Class<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    try {
      return Class.forName(json.getAsString());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
