package co.mewf.humpty.tools;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.FilenameUtils;

import co.mewf.humpty.Pipeline;
import co.mewf.humpty.config.Bundle;
import co.mewf.humpty.config.Configuration;

import com.moandjiezana.toml.Toml;

public class AssetDigester {
  
  public Toml processBundles(Pipeline pipeline, List<Bundle> bundles, Path buildDir, Path humptyDigestToml) {
    if (humptyDigestToml.toFile().exists()) {
      humptyDigestToml.toFile().delete();
    }
    
    buildDir.toFile().mkdirs();
    try {
      Files.walk(buildDir).filter(path -> path != buildDir).forEach(path -> path.toFile().delete());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    
    bundles.forEach(b -> {
      String bundleName = b.getName();
      String asset = pipeline.process(bundleName, Configuration.Mode.PRODUCTION).getAsset();
      
      String digest;
      try {
        MessageDigest messageDigest = MessageDigest.getInstance("md5");
        digest = DatatypeConverter.printHexBinary(messageDigest.digest(asset.getBytes()));
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
      }
      
      Path bundleDigestPath = buildDir.resolve(getFullDigestName(bundleName, digest));
      Path bundlePathGzip = buildDir.resolve(bundleDigestPath.getFileName() + ".gz");
      try {
        Files.write(bundleDigestPath, asset.getBytes(UTF_8));
        try (GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(bundlePathGzip.toFile()))) {
          Files.copy(bundleDigestPath, out);
        }

        Path bundlePathFile = bundleDigestPath.getFileName();
        Files.write(humptyDigestToml, (bundleName + " = \"" + bundlePathFile + "\"\n").getBytes(UTF_8), CREATE, WRITE, APPEND);
        
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
    
    return new Toml().parse(humptyDigestToml.toFile());
  }
  
  @Inject
  public void configure(Configuration.Mode mode, Pipeline pipeline, Configuration configuration, Configuration.Options options) {
//    this.digestDir = Paths.get(options.get("digestDir", "src/main/resources"));
//    this.compiledDir = Paths.get(options.get("compiledDir", "src/main/webapp/assets"));
    
//    if (mode == Configuration.Mode.EXTERNAL) {
//      @SuppressWarnings("unchecked")
//      Map<String, String> externalDigests = new Toml().parse(getClass().getResourceAsStream("/humpty-digest.toml")).to(Map.class);
//      bundleDigests.putAll(externalDigests);
//    }
  }
  
  private String getFullDigestName(String name, String digest) {
    return name.substring(0, name.lastIndexOf('.')) + "-humpty" + digest + "." + FilenameUtils.getExtension(name);
  }
}
