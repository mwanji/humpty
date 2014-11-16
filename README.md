# humpty

humpty puts your web assets back together. It is a small library that is easy to understand, configure and extend.

humpty works best with [WebJars](http://webjars.org) for 3rd-party libraries and application code in folders accessible via URL. Configuring its behaviour is as simple as dropping a JAR on the classpath.

Requires Java 8 and Servlet 3.

## Getting Started

In this example, we will use Jquery, underscore and Bootstrap and bundle them together into a single, minified file, along with application code.

Add humpty to your dependencies:

````xml
<dependency>
  <groupId>co.mewf.humpty</groupId>
  <artifactId>humpty</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
````

To use humpty as a Servlet Filter, add humpty-servlet to your dependencies:

````xml
<dependency>
  <groupId>co.mewf.humpty</groupId>
  <artifactId>humpty-servlet</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
````

We want to minify our assets, so add humpty-compression:

````xml
<dependency>
  <groupId>co.mewf.humpty</groupId>
  <artifactId>humpty-compression</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
````

(As humpty-compression depends on humpty, it is not strictly necessary to declare humpty as a dependency.)

Add the following dependencies to make the web libraries available:

````xml
<dependency>
  <groupId>org.webjars</groupId>
  <artifactId>bootstrap</artifactId> <!-- includes Jquery transitively -->
  <version>2.3.2</version>
</dependency>
<dependency>
  <groupId>org.webjars</groupId>
  <artifactId>underscore</artifactId>
  <version>1.4.4</version>
</dependency>
````

humpty uses [TOML](https://github.com/toml-lang/toml/tree/v0.2.0) as its configuration language. Create a file called `humpty.toml` in `src/main/resources`:

````toml
[[bundles]]
  name = "example.js" # the file extension is required
  assets = ["jquery", "underscore", "bootstrap", "app"] # will be concatenated into a single JS file called example.js
[[bundles]]
  name = "example.css"
  assets = ["bootstrap", "bootstrap-responsive", "app"] # will be concatenated into a single4 CSS file called example.css
````

Now we can include our concatenated and minified files in index.html:

````html
<!DOCTYPE html>
<html>
  <head>
    <script src="humpty/example.js"></script>
    <link href="humpty/example.css" type="text/css" rel="stylesheet" />
  </head>
  <body>
    Hello, humpty!
  </body>
</html>
````

## Pipeline Elements

humpty is a modular system that builds a pipeline composed of pipeline elements, into which bundles and assets are fed. 

### Bundles and Assets

A bundle is a named list of files that are accessed and processed together. The result of processing a bundle is made available at the URL defined by the `name` property. The `name` must contain a file extension (.js or .css). The bundle's contents and the order in which they are processed are set in the `assets` array.

By default, each asset is considered to be in a [WebJar](http://webjars.org). Add WebJars to your classpath and refer to them by name in the `assets` array. The name can be a file name if there is no ambiguity (eg. `jquery`), or a longer path if the are other files with the same name, eg. `smoothness/theme.css` in the case of JqueryUI.

If an asset does not have an extension, the one in the name of the bundle will be used.

You can use `*` as a wildcard to get all the files in a folder: `/assets/*`, `/assets/*.tpl`. The same extension rules apply.

````toml
[[bundles]]
  name = "example.js"
  assets = ["underscore.js",
            "otherLib.coffee"
            "jquery", # File extension is optional if it is the same as the one in the bundle's name
            "myApp" # Application code should be packaged like a WebJar, ie. located under META-INF/resources/webjars, but does not need to be in a separate JAR
           ]
````

### Processors

Processors generally modify the assets they are given in some way: compile, concatenate, minify, etc. There are 3 kinds of processors, run in the following order:

1. `SourceProcessor` changes the type of the asset (eg. from asset.coffee to asset.js)
2. `AssetProcessor` runs on individual assets (eg. URL rewriting, linting)
3. `BundleProcessor` runs on a concatenated bundle (eg. minification)

humpty has no default processors, but they are easy to add: put the ones you want on the classpath and they are automatically added to the pipeline.

There are a number of processors available:

* [humpty-compression](http://mewf.co/humpty/compression): JS & CSS minification/obfuscation
* [humpty-coffeescript](http://mewf.co/humpty/coffeescript): CoffeeScript compilation
* [humpty-bootstrap-less](http://mewf.co/humpty/bootstrap-less): Bootstrap and Font Awesome customisation via LESS
* [humpty-emberjs](http://mewf.co/humpty/emberjs): Compile Ember.Handlebars templates

Creating custom processors is discussed in the [Extension Points](#extension-points) section.

### Resolvers

Resolvers take an asset's name and turn it into one or more (in case of wildcards) files whose contents can be read. Creating custom resolvers is discussed in the [Extension Points](#extension-points) section.

**`WebJarResolver`**

Looks up resources in a [WebJar](http://webjars.org) and is bundled with humpty. For example, if `org.webjars:jquery:2.1.1` has been added to the dependencies, the resolver will find `jquery.js`. Bundled with humpty.

Configuration:

* preferMin: boolean. If true, `WebJarResolver` looks for a minified version of the requested asset by adding `.min` to the asset's base name (ie. jquery.js becomes jquery.min.js). If no such version exists or preferMin is set to false, the requested version is used. If preferMin is not set, it falls back to true in production mode and to false otherwise.
* rootDir: string, defaults to "src/main/resources". This is the base location of assets using the webjar directory format, but that are not in a JAR, such as an application's custom assets.

## Modes

humpty has 3 different modes, which may change how pipeline elements behave.

* "PRODUCTION": default. Typically enables optimisations such as minification.
* "EXTERNAL": Indicates that assets have already been processed and no runtime processing is needed.
* "DEVELOPMENT": Enables a fast development cycle. For example, may shut off caching or minification, and allow assets to be linked individually in HTML.

In your configuration file, add:

````toml
[options.humpty]
  mode = "DEVELOPMENT"
````

## Configuration Reference

By default, configuration is done via a TOML object in a file called `humpty.toml` at the root of the classpath. The configuration's properties are:

### bundles

Required. An array of tables. Must contain at least one bundle. Each bundle has a name (required) and an array of assets (required).

````toml
[[bundles]]
	name = "app.js"
	assets = ["jquery", "/assets/app.js"]

[[bundles]]
  name = "app.css",
  assets = ["bootstrap.less", "theme"]
````

### options

Optional. A table of processor-specific settings. The name to use is in each processor's documentation.

````toml
[options.bootstrap_less]
  responsive = false
````

### options.pipeline

Options that determine how the asset pipeline itself is created. By default, all processors loaded via ServiceLoaders are run.

````toml
[options.pipeline]
  mode = "DEVELOPMENT" # Defaults to "PRODUCTION". Processors are made aware of the mode and may modify their behaviour or even not run at all
  
[options.pipeline.elements] # Used to customise the processors that will be applied and their ordering 
  sources = ["coffee", "emberJs"]
  assets = [] # No AssetProcessors will run
  bundles = ["compressCSS"]
````

## Extension Points

humpty makes several interfaces available, as well as a limited form of dependency injection.

### Custom Pipeline Elements

New behaviour can be added to a pipeline by implementing one of `PipelineElement`'s sub-interfaces. For them to be added to the pipeline when a user adds the JAR to their classpath, add a file called co.mewf.humpty.spi.PipelineElement to META-INF/services, containing one fully-qualified class name per line. For more information, see the JavaDoc for `java.util.ServiceLoader`.

The sub-interfaces are:

* `BundleResolver`: finds the bundle that matches a given name
* `Resolver`: provides the contents of a given asset
* `SourceProcessor`
* `AssetProcessor`
* `BundleProcessor`
* `PipelineListener`: is notified of pipeline events, but does not directly participate in the pipeline


### Injection

While constructor injection is not allowed because resources must be instantiatable by a ServiceLoader, a limited form of method injection is available. One method may be annotated with the `javax.inject.Inject` annotation. Dependencies that can be injected:

* `Configuration` is the Java representation of the entire configuration file
* `Configuration.Options` contains the options set for the current processor
* `Configuration.Mode` whether the pipeline is running in production or development mode
* `Pipeline` the asset pipeline itself
* `WebJarAssetLocator` to find assets in a WebJar
* any object that was added programatically to `HumptyBootstrap`

If a dependency cannot be satisfied, an exception is thrown.

## Licensing

humpty is copyright Moandji Ezana 2013 - 2014.
humpty is licensed under the MIT License.
