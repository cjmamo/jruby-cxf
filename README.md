JRuby CXF    
=========

JRuby CXF is a JRuby gem that wraps [Apache CXF](http://cxf.apache.org) to provide a more friendly API for publishing Web Services.

Getting started
---------------

1. Install the gem: `jruby -S gem install jruby-cxf`
2. Include the gem into your application: `require 'jruby_cxf'`
3. Download the [Apache CXF distribution](http://cxf.apache.org/download.html)
4. Add Apache CXF's libraries to the Java classpath before running your application: `jruby -J-cp "/apache-cxf-[version]/lib/*" your_webservice.rb `

Usage
----

### WebServiceServlet

Any class to be published as a Web Service must include CXF::WebServiceServlet:

```ruby
class MyWebService
include CXF::WebServiceServlet
end
```
WebServiceServlet transforms your class into a Java servlet and provides these methods:

#### expose(name, signature, options = {})

* name: name of method to be exposed

* signature - a map of the param types and return types of the method
    * expected - a key-value pair where the key is the parameter name and value is parameter type




name
signature
	expects
	returns
options
	label

####service_name(service_name)
####service_namespace(service_namespace)
