require File.dirname(__FILE__) + '/test_web_service'

class WebServiceWithEndpointNameSet < TestWebService
	service_name :FooService
	endpoint_name :FooPort
end