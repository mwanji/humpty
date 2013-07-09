# humpty

humpty puts your web assets back together. It is a small library that is easy to understand, embed, configure and extend.

## Installation

Requires Java 6 and Servlet 3.

Add the dependency to your POM:

````xml
<dependency>
  <groupId>co.mewf.humpty</groupId>
  <artifactId>humpty</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
````

Add a mapping to `web.xml`:

````xml
<filter-mapping>
	<filter-name>HumptyFilter</filter-name>
	<url-pattern>/humpty/*</url-pattern>
</filter-mapping>
````

The `url-pattern` can be anything you want, but only humpty-managed bundle names should be passed to it.

````xml
<filter>
	<filter-name>HumptyFilter</filter-name>
	<filter-class>co.mewf.humpty.servlets.HumptyFilter</filter-class>
</filter>
````

## Usage

The best way to use humpty is with [WebJars](http://webjars.org) for 3rd-party libraries and application code in folders accessible via URL.

### Example

Let's say you've added a dependency to the [jquery WebJar](https://github.com/webjars/jquery) and want to combine jquery.js with your own app.js, found at /assets/app.js.

By default, configuration is done via a JSON object in a file called `humpty.json` at the root of the classpath (eg. in src/main/resources):

````json
{
	"bundles": [
		{
			"name": "asset1.js",
			"assets": ["jquery", "/assets/app"]
		}
	]
}
````

Exactly what happens to these assets depends on what is on your classpath.

### Bundles and Assets

A Bundle is a named list of files that are accessed and processed together. The result is made available at the URL defined by the `name` property. The files and the order in which they are processed are set in the `assets` property.

Each asset may have a prefix identifying its type:

* `<no prefix>`: This is the default, meaning the asset is in a WebJar. This can be just a file name if there is no ambiguity, or a longer path if the are other files with the same name, eg. `smoothness/theme.css` in the case of JqueryUI.
* `/`: The asset is available via URL. This must be the full path after the context path.

If an asset does not have an extension, it will use the one in the name of the bundle.

You can use `*` as a wildcard to get all the files in a folder: `/assets/*`, `/assets/*.tpl`. The same extension rules apply.

### Processors

Processors generally modify the assets they are given in some way: compile, concatenate, minify, etc. There are 3 kinds of processors, run in the following order:

1. `CompilingProcessor` changes the type of the asset (eg. from asset.coffee to asset.js)
2. `AssetProcessor` runs on individual assets (eg. URL rewriting, linting)
3. `BundleProcessor` runs on a concatenated bundle (eg. minification)

humpty has no default processors, but they are easy to add: simply put them on the classpath and they are automatically used.

There are a number of processors available:

* [humpty-compression](http://mewf.co/humpty/compression): JS & CSS minification/obfuscation
* [humpty-bootstrap-less](http://mewf.co/humpty/bootstrap-less): Bootstrap and Font Awesome customisation via LESS
* [humpty-emberjs](http://mewf.co/humpty/emberjs): Compile Ember.Handlebars templates

Creating custom processors is discussed in the [Extension Points](#extension-points) section.

### Resolvers

Resolvers take an asset's name and turn it into one or more (in case of wildcards) named `Reader`s. There are 2 resolvers bundled with humpty:

* `WebJarResolver` is the default and looks up resources in a WebJar
* `ServletContextPathResolver` finds assets relative to the Servlet context path. Is used when an asset's name starts with `/`

Creating custom resovlers is discussed in the [Extension Points](#extension-points) section.

## JSON Configuration Reference

The configuration elements are:

### bundles

Required. Must contain at least one bundle.

````json
"bundles": [
	{
		"name": "app.js",
		"assets": ["jquery", "/assets/app.js"]
	},
	{
	  "name": "app.css",
	  "assets": ["bootstrap.less", "theme"]
  }
]
````

### options

Optional, processor-specific settings.

This is a map where keys identify the processor and values are a map of processor-specific settings.

Processors can be identified either by a fully-qualified class name or a friendly alias.

````json
"options": {
  "co.mewf.humpty.CoffeeScriptAssetProcessor": { // fully-qualified class name
	  "BARE": true
  },
  "bootstrap_less": { // alias
    "responsive": false
  }
}
````

### mode

Optional. Can be PRODUCTION or DEVELOPMENT. Defaults to PRODUCTION.

In PRODUCTION mode, all processing is applied. In DEVELOPMENT mode, processors can decide whether to run or not. For example, a CoffeeScript processor would run in both modes, but a JavaScript minifier might not run in DEVELOPMENT mode.

````json
"mode": "DEVELOPMENT"
````

## Java Configuration Reference

The JSON configuration object is easy to use initially, but is not programmable and can be cumbersome. At the cost of a little bit of configuraton, the Java API provides typesafety and easier processor configuration.

### HumptyBootstrap

Use `HumptyBootstrap.Builder` to:

* use a file located elsewhere than `/humpty.json`
* use a specific set of processors and resolvers, rather than using the ServiceLoader mechanism

To use a custom `HumptyBootstrap`, extend `HumptyFilter` and override `createPipeline()`.

## Extension Points

humpty makes two interfaces available, as well as a limited form of dependency injection.

### Injection

While constructor injection is not allowed because resources must be instantiatable by a ServiceLoader, field and method injection may be used by using the javax.inject.Inject annotation.

Dependencies that can be injected:

* `WebJarAssetLocator` to find assets in a WebJar
* `Configuration.Options` contains user-provided options

### Custom Processors

New processors can be created by implementing one of the interfaces extending the `Processor` interface. For the processor to be picked up by a `ServiceLoader`, add a file called co.mewf.humpty.Processor to META-INF/services, containing one fully-qualified class name per line.

If the processor is configurable, implement the `Configurable` interface. `Configurable#configure(Map<String, Object>)` is called before the processor is used.

By default, in the configuration, processors are referred to by their fully-qualified class name. Annotate the processor with `@Alias("myAlias")` to give them a friendlier name.

Processors that are configurable and will be distributed publicly may offer a friendly Java interface to do so, alongside the JSON API.

#### Custom Resolvers

Implement the `Resolver` interface. For the resolver to be picked up by a `ServiceLoader`, add a file called co.mewf.humpty.Resolver to META-INF/services, containing one fully-qualified class name per line.

## Licensing

humpty is copyright Moandji Ezana 2013.
humpty is licensed under the MIT License.
