require File.dirname(__FILE__) + '/test_web_service'

class WebServiceWithServiceNamespaceSet < TestWebService
	service_namespace 'http://jruby-cxf.org'
end