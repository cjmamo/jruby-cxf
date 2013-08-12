require File.dirname(__FILE__) + '/test_web_service'

class WebServiceWithExposedMethodsHavingLabels < TestWebService

  expose :say_hello, {:expects => [{:name => :string}], :returns => :string}, :label => :SayHello
  expose :give_age, {:expects => [{:age => :int}], :returns => :int}, :label => :GiveAge

  def say_hello(name)
    return 'Hello ' + name
  end

  def give_age(age)
    return 'Your age is ' + age.to_s
  end
end