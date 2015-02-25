# humpty

humpty puts your web assets back together.

humpty is a library that strives to be small, understandable and easy to use. It works out of the box with [WebJars](http://webjars.org) for 3rd-party libraries.

humpty builds a pipeline to process assets. Customising that pipeline is often as easy as adding dependencies to your project.

Requires Java 8. humpty-servlet requires Servlet 3.

[![Dependency Status](https://www.versioneye.com/user/projects/54e6361cd1ec5734f40009b0/badge.svg?style=flat)](https://www.versioneye.com/user/projects/54e6361cd1ec5734f40009b0)

## Getting Started

In this example, we will bundle Jquery, underscore, Bootstrap, an application JS file and an application LESS file together into one JS file and one CSS file.

### Add Dependencies

Add humpty to your dependencies:

```xml
<dependency>
  <groupId>co.mewf.humpty</groupId>
  <artifactId>humpty</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

To have humpty process requests to `http://${DOMAIN}/${CONTEXT_PATH}/humpty`, add [humpty-servlet](https://github.com/mwanji/humpty-servlet) to your dependencies:

```xml
<dependency>
  <groupId>co.mewf.humpty</groupId>
  <artifactId>humpty-servlet</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

To compile LESS files, add [humpty-less](https://github.com/mwanji/humpty-less):

```xml
<dependency>
  <groupId>co.mewf.humpty</groupId>
  <artifactId>humpty-less</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Add the following WebJars to make the JS and CSS libraries available:

```xml
<dependency>
  <groupId>org.webjars</groupId>
  <artifactId>bootstrap</artifactId> <!-- includes Jquery transitively -->
  <version>3.3.1</version>
</dependency>
<dependency>
  <groupId>org.webjars</groupId>
  <artifactId>underscore</artifactId>
  <version>1.4.4</version>
</dependency>
```

### Define Bundles

Create `app.js` and `app.less` in `src/main/resources/assets`.

humpty uses [TOML](https://github.com/toml-lang/toml/tree/v0.3.1) as its configuration language. Create a file called `humpty.toml` in `src/main/resources`:

```toml
"example.js" = ["jquery", "underscore", "bootstrap", "app"]
"example.css" = ["bootstrap", "app.less"]
```

This defines two bundles:

* __example.js__ which is a concatenation of jquery.js, underscore.js, bootstrap.js and app.js
* __example.css__ which is a concatenation of bootstrap.css and the compiled version of app.less.

Note that where the asset's file extension matches the bundle's, it can be omitted. Beware that files containing things such as ".min" must include the extension, eg. "jquery.min.js".

Out of the box, humpty handles assets located in WebJars (`jquery.js`, `bootstrap.css`, etc.) and in the`assets` folder (`app.js` and `app.less`).

Now we can include our concatenated and fully processed files in index.html:

```html
<!DOCTYPE html>
<html>
  <head>
    <link href="${CONTEXT_PATH}/humpty/example.css" type="text/css" rel="stylesheet" />
  </head>
  <body>
    Hello, humpty!

    <script src="${CONTEXT_PATH}/humpty/example.js"></script>
  </body>
</html>
```

While developing, you may want to include files separately, for easier debugging.

For each file, add the asset's name behind the bundle's name:

```html
<!DOCTYPE html>
<html>
  <head>
    <link href="${CONTEXT_PATH}/humpty/example.css/bootstrap.css" type="text/css" rel="stylesheet" />
    <link href="${CONTEXT_PATH}/humpty/example.css/app.less" type="text/css" rel="stylesheet" />
  </head>
  <body>
    Hello, humpty!

    <script src="${CONTEXT_PATH}/humpty/example.js/jquery.js"></script>
    <script src="${CONTEXT_PATH}/humpty/example.js/underscore.js"></script>
    <script src="${CONTEXT_PATH}/humpty/example.js/bootstrap.js"></script>
    <script src="${CONTEXT_PATH}/humpty/example.js/app.js"></script>
  </body>
</html>
```

and so on.

Thankfully, this can be automated. humpty-servlet adds an instance of `Includes` to the servlet context.

* Get it by calling `servletContext.getAttribute(Includes.class.getName())`
* Add the result of `Includes#generate("example.css")` and `Includes#generate("example.js")` to your HTML template

### Processing Bundles vs. Assets

The pipeline works differently when an entire bundle is requested, as opposed to a single asset. Exactly how differently depends on what processors are running in your pipeline. Here are a few examples:

* a compiler might produce a source map for a single asset, but not for a bundle
* a minifier might run only for bundles
* a linter might run only for single assets

### Prepare for Production

Add the Maven plugin:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>co.mewf.humpty</groupId>
      <artifactId>humpty-maven-plugin</artifactId>
      <version>1.0.0-SNAPSHOT</version>
    </plugin>
  </plugins>
</build>
```

Run the following from the project's root directory: `mvn humpty:digest`.

Digesting creates a fingerprinted file for each bundle, such as `example-humpty586985585.js`, which is written to the build directory (`src/main/resources/META-INF/resources` by default). The fingerprint will change when the bundle's content changes, so the file can be served with far-future HTTP caching headers.

This also creates a `humpty-digest.toml` file that indicates that the application is in production mode. To return to development mode, delete the file. In practice, this file might only exist on the source control branch from which you deploy.

In production mode, `Includes#generate` will link to the fingerprinted version, rather than the individual assets.

## Configuration

### Global Options

Option|Default|Description
------|-------|-----------
assetsDir|"assets"|The folder containing the application's assets, relative to the root of the classpath.
buildDir|"src/main/resources/META-INF/resources"|The root folder where assets are put after they've been through the pipeline. The default allows the assets to be served directly in environments such as Servlet 3.
digestFile|"src/main/resources/humpty-digest.toml"|The path to the file that tracks digested assets.

```toml
[options]
  assetsDir = "assets"
  buildDir = "src/main/resources/META-INF/resources"
  digestFile = "src/main/resources/humpty-digest.toml"
```

For example, the file `${assetsDir}/path/to/app.js` would be copied to `${buildDir}/path/to/app.js`.

### Element-specific options

Each pipeline element has a name that can be used to set its options.

```toml
[options.element_name]
  option1 = value1
  option2 = [value2, value3, value4]

  [options.element_name.option3]
    option3_1 = value5
```

## Pipeline Elements

humpty is a modular system that builds a pipeline composed of pipeline elements, into which bundles and assets are fed. There are several types of pipeline elements:

* __Resolvers__ find files
* __Processors__ do things to those files
* __Listeners__ are notified of certain events, such as when assets are processed

### Bundles and Assets

A bundle is a named list of files that are accessed and processed together. The result of processing a bundle is made available at the URL defined by the `name` property. The `name` must contain a file extension (e.g. .js or .css). The bundle's contents and the order in which they are processed are set in the `assets` array.

By default, each asset is considered to be in a [WebJar](http://webjars.org). Add WebJars to your classpath and refer to them by name in the `assets` array. The name can be a file name if there is no ambiguity (eg. `jquery`), or a longer path if the are other files with the same name, eg. `smoothness/theme.css` in the case of JqueryUI.

If an asset does not have an extension, the one in the name of the bundle will be used.

There are two ways of writing bundles:

```toml
# shorthand
"example.js" = ["underscore.js", "otherLib.coffee", "jquery", "myApp"]

# longhand 
["example.js"]
  assets = ["underscore.js",
            "otherLib.coffee",
            "jquery",
            "myApp"
           ]
```

__WARNING__: shorthand bundles MUST appear before any other table, or they will not work!

### Resolvers

Resolvers take an asset's name and turn it into files whose contents can be read. Creating custom resolvers is discussed in the [Extension Points](#extension-points) section.

#### FileResolver

Bundled with humpty. Looks for files in your application's folders. The root folder is set by the global `assetsDir` option.

In your `humpty.toml`, prefix asset names with a `/` to indicate that it is part of your application.

Example:

```toml
"mybundle.js" = ["/myApp.js"]
```

#### WebJarResolver

Bundled with humpty. Looks up resources in a [WebJar](http://webjars.org). For example, if `org.webjars:jquery:2.1.1` has been added to the dependencies, the resolver will find `jquery.js`.

Configuration:

option|type|default|description
------|----|-------|-----------
preferMin|boolean|(auto)|If true, `WebJarResolver` looks for a minified version of the requested asset by adding `.min` to the asset's base name (ie. jquery.js becomes jquery.min.js). If no such version exists or preferMin is set to false, the requested version is used. If preferMin is not set, it falls back to true in production mode and to false otherwise.

```toml
[options.webjars]
  preferMin = false
```

Note:

WebJars are typically in JAR files, but they can also be "faked" by reproducing the appropriate folder structure: `META-INF/resources/webjars/${WEBJAR_NAME}/${WEBJAR_VERSION}/`. This can be useful when a third-party library does not have a WebJar.

### Processors

Processors generally modify the assets they are given in some way: compile, concatenate, minify, etc. There are 3 kinds of processors, run in the following order:

1. `SourceProcessor` changes the type of the asset (eg. from asset.coffee to asset.js)
2. `AssetProcessor` runs on individual assets (eg. URL rewriting, linting)
3. `BundleProcessor` runs on a concatenated bundle (eg. minification)

humpty has no default processors, but they are easy to add: put the ones you want on the classpath and they are automatically added to the pipeline.

There are a number of processors available:

* [humpty-less](https://github.com/mwanji/humpty-less): LESS compilation
* [humpty-css](https://github.com/mwanji/humpty-css): CSS tools

Creating custom processors is discussed in the [Extension Points](#extension-points) section.

## Configuration Reference

By default, configuration is done via a TOML object in a file called `humpty.toml` at the root of the classpath. The configuration's properties are:

### bundle

An array of tables which must contain at least one bundle. Each bundle has a name (required) and an array of assets (required).

```toml
# shorthand  
libs.js = ["jquery", "underscore"]

[[bundle]]
	name = "app.js"
	assets = ["jquery", "/app.js"]

[[bundle]]
  name = "app.css",
  assets = ["bootstrap.less", "/theme"]
```

### options

Optional. A table of global and pipeline element-specific settings. The pipeline element name to use is in each element's documentation.

````toml
[options]
  mode = "DEVELOPMENT"

[options.pipeline_element_name]
  key = value
````

### options.pipeline

Options that determine how the asset pipeline itself is created. By default, all processors loaded via ServiceLoaders are run.

````toml
[options.pipeline]
  mode = "DEVELOPMENT" # Defaults to "PRODUCTION". Processors are made aware of the mode and may modify their behaviour or even not run at all

[options.pipeline.elements] # Used to customise the processors that will be applied and their ordering
  sources = ["coffee", "emberJs"] # Only these SourceProcessors will run
  assets = [] # No AssetProcessors will run
  # As bundles is commented out, the default BundleProcessors will run
  #bundles = ["compression"]
````

## Extension Points

humpty makes several interfaces available for new pipeline elements to be created, as well as a limited form of dependency injection.

### Custom Pipeline Elements

New behaviour can be added to a pipeline by implementing one of `PipelineElement`'s sub-interfaces. For them to be added to the pipeline when a user adds the JAR to their classpath, create a file called `co.mewf.humpty.spi.PipelineElement` in `META-INF/services`, containing one fully-qualified class name per line. For more information, see the JavaDoc for [java.util.ServiceLoader](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html).

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
* `Configuration.GlobalOptions` the global options
* `Configuration.Options` contains the options set for the current processor
* `Pipeline` the asset pipeline itself
* `WebJarAssetLocator` to find assets in a WebJar, avoids pipeline elements having to create their own instance
* any object that was added programatically to `HumptyBootstrap`

If a dependency cannot be satisfied, an exception is thrown.

## Licensing

humpty is copyright Moandji Ezana 2013 - 2014.
humpty is licensed under the MIT License.
