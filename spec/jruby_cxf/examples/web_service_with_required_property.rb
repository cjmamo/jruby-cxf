require File.dirname(__FILE__) + '/test_web_service'

class Person
  include CXF::ComplexType

  member :name, :string
  member :age, :int
  member :pet, :boolean, :required => false

end

class WebServiceWithRequiredProperty < TestWebService

	expose :say_hello, {:expects => [{:person => :Person}], :returns => :string}

	def say_hello(name)
		return 'Hello ' + name
	end

end
