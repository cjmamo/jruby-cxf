require 'jruby_cxf'
require 'httparty'

class TestWebService
  include CXF::WebServiceServlet

  def wsdl
    HTTParty.get("http://localhost:8080/#{@path}?wsdl").parsed_response       
  end

  def publish
    @server = org.eclipse.jetty.server.Server.new(8080)
    contexts = org.eclipse.jetty.server.handler.ContextHandlerCollection.new
    @server.set_handler(contexts)
    rootContext = org.eclipse.jetty.servlet.ServletContextHandler.new(contexts, "/")
    rootContext.addServlet(org.eclipse.jetty.servlet.ServletHolder.new(self), "/*")

    @server.start       
  end 

  def teardown
    @server.stop
  end 

  def initialize
    @path = 'web-service'
    super('/' + @path)
  end
end