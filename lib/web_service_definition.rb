module CXF
	module WebServiceDefinition
		include CXF

		attr_accessor :web_service_definition

		def self.included(base)
			base.extend(self)
		end

		def web_service_definition
			@web_service_definition || {}			
		end

		def service_namespace(service_namespace)
			@web_service_definition ||= {}
			@web_service_definition['service_namespace'] = service_namespace
		end					

		def endpoint_name(endpoint_name)
			@web_service_definition ||= {}
			@web_service_definition['endpoint_name'] = endpoint_name
		end				

		def service_name(service_name)
			@web_service_definition ||= {}
			@web_service_definition['service_name'] = service_name
		end		

		def expose(name, signature)

			java_return_type = get_java_type(signature[:returns][:type])

			java_param_types = []
			signature[:expects].each {|expect|
				java_param_types << get_java_type(expect.values.first)
			}

			java_signature = [java_return_type].concat(java_param_types)

			add_method_signature(name.to_s, java_signature)

			exposed_method = { name => { label: signature[:label], expects: signature[:expects], returns: signature[:returns] } }
			@web_service_definition ||= {}
			if @web_service_definition.has_key? 'exposed_methods'
				@web_service_definition['exposed_methods'] = @web_service_definition['exposed_methods'].merge(exposed_method)
			else	
				@web_service_definition['exposed_methods'] = exposed_method
			end	
		end	

	end	
end