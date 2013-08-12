JRuby CXF [![Gem Version](https://badge.fury.io/rb/jruby-cxf.png)](http://badge.fury.io/rb/jruby-cxf)    
=========

JRuby CXF is a JRuby gem that wraps the [Apache CXF](http://cxf.apache.org) framework to provide a friendlier API for publishing Web Services.

Requirements
------------

JRuby CXF was tested with JRuby 1.7.4 and Apache CXF 2.7.6.

Getting started
---------------

1. From your console install the gem: `jruby -S gem install jruby-cxf`

2. Include the gem in your application: 
```ruby
require 'jruby_cxf'
...
```
3. Download the [Apache CXF distribution](http://cxf.apache.org/download.html).

4. Add Apache CXF's libraries to the Java classpath before running your application: 
   `jruby -J-cp "/apache-cxf-[version]/lib/*" your_webservice.rb `

API Usage
---------

### WebServiceServlet

A class must include *CXF::WebServiceServlet* for it to become a Web Service:
```ruby
class MyWebService
    include CXF::WebServiceServlet
    
    ...
end
```
*WebServiceServlet* transforms your class into a Java servlet so any servlet container (e.g., Tomcat, Jetty, JBoss AS)
can load an instance of the class. 

The default servlet path is the root path but it can be changed by passing a path to the class's constructor:
```ruby
...
my_web_service = MyWebService.new('/my-webservice')

# using Jetty 8 to publish Web Service
server = org.eclipse.jetty.server.Server.new(8080)
contexts = org.eclipse.jetty.server.handler.ContextHandlerCollection.new
server.set_handler(contexts)
rootContext = org.eclipse.jetty.servlet.ServletContextHandler.new(contexts, "/")
rootContext.addServlet(org.eclipse.jetty.servlet.ServletHolder.new(my_web_service), "/*")
server.start
```
The module provides the following methods to the class:

#### expose(name, signature, options = {})

* name - Name of method to be published as a Web Service operation

* signature - Map of method's expected parameters and return type:

  * *expected* => Sequence of key-value pairs where each pair corresponds to a method parameter. The key represents the 
                  type element name that is shown in the WSDL. The value represents the parameter's expected type.
  * *returns* => Expected object type returned by the method.
        
* options - Map of options
  * *label* => Name given to the corresponding operation XML element in the WSDL. The default value is the name of the 
               Ruby method. 
  * *out_parameter_name* => Element name of the return value.
  * *response_wrapper_name* => Response wrapper element name.

##### Example
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

* service_name - Service name of the Web Service. The default value is the name of the Ruby class + *Service*.

##### Example
```ruby
class Calculator
  include CXF::WebServiceServlet

  service_name: :CalculatorService

  ...
end
```
####service_namespace(service_namespace)

* service_namepace - Service's target namespace. The default value is *http://rubjobj/*.

##### Example
```ruby
class Calculator
  include CXF::WebServiceServlet

  service_namespace: 'http://jruby-cxf.org'

  ...
end
```
####endpoint_name(endpoint_name)

* endpoint_name - Service's port name.

##### Example
```ruby
class Calculator
  include CXF::WebServiceServlet

  endpoint_name: 'ExamplePort'

  ...
end
```
---
### ComplexType

An exposed method can have a parameter or return complex type. The class implementing the complex type
must include *CXF::ComplexType*:

```ruby
class MyComplexType
   include CXF::ComplexType
   ...
end

class MyWebService
   include CXF::WebServiceServlet
   
   expose :foo, {:expects => [{:param => :MyComplexType}], :returns => :MyComplexType}
   ...
end   
```

*ComplexType* provides the following methods:

#### member(name, type, options = {})

A member behaves like an *attr_accessor* but with one important difference: attributes are typed.

* name - Attribute name.

* type - Attribute type.

* options - Map of options
  * *required* => The *minOccurs* for the corresponding type element. True sets the value to 1. False sets the value 
  		  to 0. Its default value is true.
  * *label* => Name given to the corresponding type element in the WSDL. The default value is the member name.

##### Example
```ruby
class Person
   include CXF::ComplexType
	
   member :name, :string
   member :age, :int, :required => false
end
```

#### namespace(namespace)

* namespace - Complex type's target namespace. The default value is *http://rubjobj/*.
 
##### Example
```ruby
class Person
   include CXF::ComplexType
	
   ...

   namespace 'http://jruby-cxf.org'	
end	
```
---
### Supported simple types 

* big_decimal
* boolean
* byte
* datetime
* double
* float
* long
* int
* short
* string
* time

Support 
-------

Please [report](http://github.com/claudemamo/jruby-cxf/issues) any bugs or desired improvements.
