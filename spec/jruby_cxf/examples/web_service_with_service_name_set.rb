require File.dirname(__FILE__) + '/test_web_service'

class WebServiceWithServiceNameSet < TestWebService
    service_name :FooService
end
