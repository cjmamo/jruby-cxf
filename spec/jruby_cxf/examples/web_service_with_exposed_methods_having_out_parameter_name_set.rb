require File.dirname(__FILE__) + '/test_web_service'

class WebServiceWithExposedMethodsHavingOutParameterNameSet < TestWebService

  expose :say_hello, {:expects => [{:name => :string}], :returns => :string}, :out_parameter_name => :OutSayHello
  expose :give_age, {:expects => [{:age => :int}], :returns => :int}, :out_parameter_name => :OutGiveAge

  def say_hello(name)
    return 'Hello ' + name
  end

  def give_age(age)
    return 'Your age is ' + age.to_s
  end
end