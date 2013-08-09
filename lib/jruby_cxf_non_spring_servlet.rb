module CXF

	module WebServiceServlet

		class JRubyCXFNonSpringServlet < org.apache.cxf.transport.servlet.CXFNonSpringServlet
			attr_accessor :server_factory

			def loadBus(servletConfig)
				super(servletConfig)

				@server_factory.create
			end			
		end	
	end	

end
