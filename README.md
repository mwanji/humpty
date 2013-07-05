# humpty

humpty puts your web assets back together. It is a small library that is easy to understand, embed, configure and extend.

## Installation

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

The `url-pattern` can be anything you want, but only humpty-managed assets should be passed to it.

If you are are using Servlet 2.5 or under, add a filter declaration:

````xml
<filter>
	<filter-name>HumptyFilter</filter-name>
	<filter-class>co.mewf.humpty.servlets.HumptyFilter</filter-class>
</filter>
````

## Usage

The best way to use humpty is with [WebJars](http://webjars.org) for 3rd-party libraries and to put application code in folders accessible via URL.

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

### Bundles and Assets

A Bundle is a named list of files that are accessed and processed together. The result is made available at the URL defined by the `name` property. The files and the order in which they are processed are set in the `assets` property.

Each asset has a prefix identifying its type:

* `<no prefix>` | `webjar:`: This is the default. The asset is in a WebJar. This can be just a file name if there is no ambiguity, or a longer path if there is, eg. `smoothness/theme.css`.
* `/`: The asset is available via URL. This must be the full path after the context path.

If an asset does not have an extension, it will use the one in the name of the bundle.

You can use `*` as a wildcard to get all the files in a folder: `/assets/*`, `/assets/*.tpl`. The same extension rules apply.

### Processors

Processors modify the assets they are given in some way: compile, concatenate, minify, etc. There are 3 kinds of processors, run in the following order:

* `CompilingProcessor` changes the type of the asset (eg. from asset.coffee to asset.js)
* `PreProcessor` runs on individual assets (eg. URL rewriting, linting)
* `PostProcessor` runs on a concatenated bundle (eg. minification)

humpty has no default processors, but they are easy to add: simply put them on the classpath and they are automatically used.

There are a number of processors available:

* [humpty-compression](http://mewf.co/humpty/compression): JS & CSS minification/obfuscation
* [humpty-bootstrap-less](http://mewf.co/humpty/bootstrap-less): Bootstrap customisation via LESS
* [humpty-emberjs](http://mewf.co/humpty/emberjs): Compile Ember.Handlebars templates

#### Custom Processors

New processors can be created by implementing any interface extending the `Processor` interface. For the processor to be picked up by a `ServiceLoader`, add a file called co.mewf.humpty.Processor to META-INF/services, containing one fully-qualified class name per line.

If the processor is configurable, implement the `Configurable` interface. `Configurable#configure(Map<String, Object>)` is called before the processor is used.

By default, in the configuration, processors are referred to by their fully-qualified class name. Annotate the processor with @Alias("myAlias") to give it a friendlier name.

Processors that are configurable and will be distributed publically may offer a friendly Java interface to do so, alongside the JSON API.

### Resolvers

Resolvers take an asset's name and turn it into one or more (in case of wildcards) named `Reader`s. There are 2 resolvers bundled with humpty:

* `WebJarResolver` is the default and looks up resources in a WebJar
* `ServletContextPathResolver` finds assets relative to the Servlet context path. Is used when an asset's name starts with `/`

#### Custom Resolvers

Implement the `Resolver` interface. For the resolver to be picked up by a `ServiceLoader`, add a file called co.mewf.humpty.Resolver to META-INF/services, containing one fully-qualified class name per line.

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
  "co.mewf.humpty.CoffeeScriptPreProcessor": { // fully-qualified class name
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

