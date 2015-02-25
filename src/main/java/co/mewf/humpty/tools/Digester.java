package co.mewf.humpty.tools;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.inject.Inject;

import co.mewf.humpty.Pipeline;
import co.mewf.humpty.config.Bundle;
import co.mewf.humpty.config.Configuration;

public class Digester {
  
  private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
  
  public Map<String, Path> processBundles(Pipeline pipeline, List<Bundle> bundles, Path buildDir) {
    buildDir.toFile().mkdirs();
    try {
      Files.walk(buildDir).filter(path -> path != buildDir).forEach(path -> path.toFile().delete());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    
    Map<String, Path> digests = new HashMap<>();
    
    bundles.stream()
      .map(Bundle::getName)
      .map(bundleName -> {
        String asset = pipeline.process(bundleName).getAsset();
        Path bundleDigestPath = buildDir.resolve(getFullDigestName(bundleName, asset));
        Path bundlePathFile = bundleDigestPath.getFileName();
        digests.put(bundleName, bundlePathFile);

        try {
          Files.write(bundleDigestPath, asset.getBytes(UTF_8));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        
        return bundleDigestPath;
      })
      .forEach(bundleDigestPath -> {
        Path bundlePathGzip = buildDir.resolve(bundleDigestPath.getFileName() + ".gz");
        
        try (GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(bundlePathGzip.toFile()))) {
          Files.copy(bundleDigestPath, out);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
    
    return digests;
  }
  
  @Inject
  public void configure(Pipeline pipeline, Configuration configuration, Configuration.Options options) {
//    this.digestDir = Paths.get(options.get("digestDir", "src/main/resources"));
//    this.compiledDir = Paths.get(options.get("compiledDir", "src/main/webapp/assets"));
    
//    if (mode == Configuration.Mode.EXTERNAL) {
//      @SuppressWarnings("unchecked")
//      Map<String, String> externalDigests = new Toml().parse(getClass().getResourceAsStream("/humpty-digest.toml")).to(Map.class);
//      bundleDigests.putAll(externalDigests);
//    }
  }
  
  private String getFullDigestName(String name, String asset) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("MD5");
      String fingerprint = encodeHex(messageDigest.digest(asset.getBytes()));
      
      return name.substring(0, name.lastIndexOf('.')) + "-humpty" + fingerprint + name.substring(name.lastIndexOf('.'));
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  // Adapted from org.springframework.util.DigestUtils
  private static String encodeHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder(32);
    for (int i = 0; i < 32; i = i + 2) {
      byte b = bytes[i / 2];
      sb.append(HEX_CHARS[(b >>> 0x4) & 0xf])
        .append(HEX_CHARS[b & 0xf]);
    };

    return sb.toString();
  }
}
