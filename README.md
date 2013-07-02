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

The best way to use humpty is with [WebJars](http://webjars.org).

### Example

Let's say you've added a dependency to the [jquery WebJar](https://github.com/webjars/jquery) and want to combine jquery.js with your own app.js, found at /assets/app.js.

#### JSON

By default, configuration is done via a JSON object in a file called `humpty.json` at the root of the classpath:

````json
{
	"bundles": [
		{
			"name": "asset1.js",
			"assets": ["webjar:jquery.js", "path:/assets/app.js"]
		}
	]
}
````

#### Java

The Java Configuration API is discussed in more detail later, but here's what it looks like:

````java
import static java.util.Arrays.asList;

public MyConfigurationProvider implements ConfigurationProvider {

  public Configuration getConfiguration() {
    return new Configuration(asList(new Bundle("asset1.js", asList("webjar:jquery.js", "path:/assets/app.js"))));
  }
}
````

### Bundles

A Bundle is a named list of files that are accessed and processed together. The result is made available at the URL defined by the `name` property. The files and the order in which they are processed are set in the `assets` property.

Each asset has a prefix identifying its type:

* `webjar`: The asset is in a WebJar. This can be just a file name if there is no ambiguity, or a longer path if there is, eg. `smoothness/theme.css`.
* `path`: The asset is available via URL. This must be the full path after the context path.

### Processors

Processors modify the assets they are given in some way: compile, concatenate, minify, etc. Pre-processors (eg. a compiler) run before the assets have been concatenated, post-processors (eg. a minifier) run after.

humpty has no default processors, but they are easy to add: simply put them on the classpath and they are automatically used.

There are a number of first-party processors:

* [humpty-compression](http://mewf.co/humpty/compression): JS & CSS minification/obfuscation
* humpty-bootstrap-less: Bootstrap customisation via Less

#### Custom Processors

New processors can be created by implementing either the `PreProcessor` or `PostProcessor` interface. For the processors to be automatically picked up by a `ServiceLoader`, add a file called co.mewf.humpty.(Pre|Post)Processor to META-INF/services, containing one fully-qualified class name per line.

Processors that are configurable and will be distributed publically should offer a friendly Java interface to do so.

### Configuration

The configuration elements are:

#### bundles

Required. Must contain at least one bundle.

````json
{
	"bundles": [
		{
			"name": "app.js",
			"assets": ["webjar:jquery.js", "path:/assets/app.js"]
		},
		{
		  "name": "app.css",
		  "assets": ["webjar:bootstrap.less", "webjar:theme.css"]
	  }
	]
}
````

#### options

Optional, processor-specific settings.

Processors are configured through a `Map<String, Map<String, Object>>` where the key is the processor's fully-qualified class name and the value is a processor-specific `Map<String, Object>`. Add an `options` key to the, JSON object described above.

Example:

````json
{
  "bundles": [],
  "options": {
	  "co.mewf.humpty.CoffeeScriptPreProcessor": {
		  "BARE": true
	  }
  }
}
````

#### mode

Optional. Can be PRODUCTION or DEVELOPMENT. Defaults to PRODUCTION.

In PRODUCTION mode, all processing is applied. In DEVELOPMENT mode, processors can decide whether to run or not. For example, a CoffeeScript processor would run in both modes, but a JavaScript minifier would not run in DEVELOPMENT mode.

````json
{
  "bundles": [],
  "options": {},
  "mode": "DEVELOPMENT"
}
````

#### Java Configuration API

The JSON configuration object is easy to use initially, but is not programmable and can be cumbersome. At the cost of a little bit of configuraton, the Java API provides typesafety and easier processor configuration.

1. Implement a `ConfigurationProvider` and build up your `Configuration`.
1. Register the `ConfigurationProvider` as a service by putting its fully-qualifed name in `META-INF/services/co.mewf.humpty.config.ConfigurationProvider`. Example:

````
com.example.myapp.MyConfigurationProvider
````
