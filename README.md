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

* name - Name of method to be exposed

* signature - Map of method's expected parameters and return type:
  * *expected* => List of key-value pairs where each pair corresponds to a method parameter. The key represents the
                  parameter name as shown the in the WSDL and the value  the parameter's type
  * *returns* => Object type returned by the method

* options - Map of options
  * *label* => Name of the wsdl:operation matching this method. The default value is the name of Ruby method.
  * *out_parameter_name* =>  Mapping of the return value
  * *response_wrapper_name* => Response wrapper name

```ruby
class Calculator
  include CXF::WebServiceServlet

  expose :add, {:expects => [{:a => :int}, {:b => :int}], :returns => :int}, :label => :Add
  expose :divide, {:expects => [{:a => :int}, {:b => :int}], :returns => :float}, :label => :Divide

  def add(a, b)
    return a + b
  end

  def divide(a, b)
    return a.to_f / b.to_f
  end
end
```

####service_name(service_name)

* service_name - Service name of the Web Service: *wsdl:service*. The default value is the name of Ruby class + *Service*.

```ruby
class Calculator
  include CXF::WebServiceServlet

  service_name: :CalculatorService

  ...
end
```

####service_namespace(service_namespace)

* service_namepace - XML namespace of the WSDL and XML elements generated from the Web service. The default value is *http://rubjobj/*.

```ruby
class Calculator
  include CXF::WebServiceServlet

  service_namespace: 'http://jruby-cxf.org'

  ...
end
```

####endpoint_name(endpoint_name)

* endpoint_name - Port name of the Web Service: *wsdl:port*.

```ruby
class Calculator
  include CXF::WebServiceServlet

  endpoint_name: 'http://jruby-cxf.org'

  ...
end
```

### ComplexType

A class that is used as a complex type for a Web Serice must include CXF::ComplexType

```ruby
class MyComplexType
    include CXF::ComplexType
end
```

#### member(name, type, options = {})

* name - Name of property

* type - Type of the property

* options - Map of options
  * required


