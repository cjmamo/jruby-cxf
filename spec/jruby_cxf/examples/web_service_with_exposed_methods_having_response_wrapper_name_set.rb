require File.dirname(__FILE__) + '/test_web_service'

class WebServiceWithExposedMethodsHavingResponseWrapperNameSet < TestWebService

  expose :say_hello, {:expects => [{:name => :string}], :returns => :string}, :response_wrapper_name => :SayHelloResponseWrapper
  expose :give_age, {:expects => [{:age => :int}], :returns => :int}, :response_wrapper_name => :GiveAgeResponseWrapper

  def say_hello(name)
    return 'Hello ' + name
  end

  def give_age(age)
    return 'Your age is ' + age.to_s
  end
end