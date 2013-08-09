module CXF

	class JRubyServiceConfiguration < org.apache.cxf.service.factory.DefaultServiceConfiguration

		attr_accessor :service_namespace, :service_name, :declared_methods

		def getServiceNamespace
			@service_namespace || super
    	end

    	def getServiceName
    		@service_name || super
    	end

	    def isOperation(method)
	        if @declared_methods.has_key? method.get_name
	            return true
	        end

	        return false
	    end

		def getResponseWrapperName(op, method)
			unless @declared_methods[method.get_name][:response_wrapper_name].nil?
				QName.new(op.getName().getNamespaceURI(), op.getName().getLocalPart() + @declared_methods[method.get_name][:response_wrapper_name])
			else
				super
			end		
		end

		def getOutParameterName(op, method, paramNumber)
			unless @declared_methods[method.get_name][:out_parameter_name].nil?
				QName.new(op.getName().getNamespaceURI(), op.getName().getLocalPart() + @declared_methods[method.get_name][:out_parameter_name])
			else
				super
			end		
		end	

	    def getInParameterName(op, method, paramNumber)
	        QName.new(op.get_name.get_namespace_uri, @declared_methods[method.get_name][:in_parameter_names][paramNumber])
	    end

	    def getOperationName(intf, method)
	    	unless @declared_methods[method.get_name][:label].nil?
				QName.new(intf.get_name.get_namespace_uri, @declared_methods[method.get_name][:label])
			else
				super
			end					    			
		end	    	

		def initialize
			@declared_methods = {}
		end


	end	

end
