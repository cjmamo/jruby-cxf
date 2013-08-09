require 'java'
require 'jruby/core_ext'
require 'web_service_definition'
require 'jruby_service_configuration'
require 'jruby_reflection_service_factory_bean'
require 'jruby_cxf_non_spring_servlet'

java_import javax.xml.namespace.QName

module CXF

	module WebServiceServlet  
 		include CXF::WebServiceDefinition

		def self.included(base)
			base.extend(self)
		end

		# required to avoid inheritance
		def method_missing(m, *args, &block)  
    		@proxy_servlet.send(m, *args, &block)
  		end 

		def initialize(address = '/')

			web_service_definition = get_web_service_definition(self)

			service_factory = create_service_factory({service_name: web_service_definition[:service_name], 
													  service_namespace: web_service_definition[:service_namespace],
													  endpoint_name: web_service_definition[:endpoint_name],
													  declared_methods: web_service_definition[:methods]})

			data_binder = create_data_binder(web_service_definition[:complex_types])

			server_factory = create_server_factory({service_factory: service_factory, 
							                 	    data_binder: data_binder, 
								   					address: address, 
								   					service_bean: self.class.become_java!.newInstance})

			@proxy_servlet = JRubyCXFNonSpringServlet.new
			@proxy_servlet.server_factory = server_factory
		end

		private

		def get_web_service_definition(web_service)

			declared_methods = {}
			found_complex_types = {}
			web_service_definition = web_service.class.web_service_definition
			
			web_service_definition['exposed_methods'] = {} if not web_service_definition.has_key? 'exposed_methods'

			web_service.methods.each{ |method|

				if web_service_definition['exposed_methods'].has_key? method		

					in_parameter_names = []
					web_service_definition['exposed_methods'][method].each{|config_key, config_value|

						if config_key == :expects
							config_value.each { |expect|
								found_complex_types = find_complex_types(expect.values.first, found_complex_types)
								in_parameter_names << expect.keys.first.to_s
							}
						end
				
					}
					
				    unless web_service_definition['exposed_methods'][method][:label].nil?
				    	method_label = web_service_definition['exposed_methods'][method][:label].to_s
				    end	

                	unless web_service_definition['exposed_methods'][method][:returns][:wrapper_name].nil?
				    	response_wrapper_name = web_service_definition['exposed_methods'][method][:returns][:wrapper_name].to_s
				    end	

                	unless web_service_definition['exposed_methods'][method][:returns][:parameter_name].nil?
						out_parameter_name = web_service_definition['exposed_methods'][method][:returns][:parameter_name].to_s
				    end	

					declared_methods[method.to_s] = { in_parameter_names: in_parameter_names, 
						 							  label: method_label,
						 							  response_wrapper_name: response_wrapper_name, 
						 							  out_parameter_name: out_parameter_name }
					
				end
			}


			return { methods: declared_methods, 
					 complex_types: found_complex_types, 
					 service_name: web_service_definition['service_name'],
					 service_namespace: web_service_definition['service_namespace'],
					 endpoint_name: web_service_definition['endpoint_name'] }
		end	

		def create_data_binder(complex_types)
	    	data_binder = org.jrubycxf.aegis.databinding.AegisDatabinding.new
			aegis_context = org.jrubycxf.aegis.AegisContext.new
			opts = org.jrubycxf.aegis.type.TypeCreationOptions.new
			
			opts.set_default_min_occurs(1);
			opts.set_default_nillable(false)
			opts.set_complex_types(complex_types.to_java)

			aegis_context.set_type_creation_options(opts)
			data_binder.set_aegis_context(aegis_context)

			return data_binder
	    end	

	    def create_service_factory(config)
			service_factory = org.apache.cxf.service.factory.ReflectionServiceFactoryBean.new;
			service_configuration = JRubyServiceConfiguration.new

			service_configuration.service_namespace = config[:service_namespace]
			service_configuration.service_name = config[:service_name]
			service_configuration.declared_methods = config[:declared_methods]

			service_factory.get_service_configurations.add(0, service_configuration)

			unless config[:endpoint_name].nil?
				service_factory.set_endpoint_name(QName.new(config[:endpoint_name])) 
			end			

			return service_factory
		end   

		def create_server_factory(config)
			server_factory = org.apache.cxf.frontend.ServerFactoryBean.new;
			server_factory.set_service_factory(config[:service_factory])
			server_factory.set_data_binding(config[:data_binder])
			server_factory.set_service_bean(config[:service_bean])
			server_factory.set_address(config[:address])
			
			return server_factory
		end 	

	    def find_complex_types(complex_type, found_complex_types)

			if is_complex_type? complex_type
				complex_type_class = Kernel.const_get(complex_type)
				complex_type_java_class = get_java_type(complex_type).to_s
				found_complex_types[complex_type_java_class] ||= {}

				if complex_type_class.complex_type_definition.has_key? 'namespace'
	    			found_complex_types[complex_type_java_class]['namespace'] = complex_type_class.complex_type_definition['namespace'].to_s
	    		end	

	    		complex_type_class.complex_type_definition['members'].each{ |member_name, definition|
	    			found_complex_types[complex_type_java_class] ||= {}
	    			found_complex_types[complex_type_java_class][member_name] ||= {}
					found_complex_types[complex_type_java_class][member_name]['required'] = definition[:required]

					# avoid infinite recursion
					if is_complex_type?(definition[:type]) and Kernel.const_get(definition[:type].to_s) != complex_type_class
						find_complex_types(definition[:type], found_complex_types)
	    			end	
				}

			end

	    	return found_complex_types
	    end	

	    def is_complex_type?(type)
			Kernel.const_get(type.to_s).included_modules.include? CXF::ComplexType
		rescue
			return false	
		end		
	end		
end	