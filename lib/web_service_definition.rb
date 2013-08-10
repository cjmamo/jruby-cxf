# Copyright 2013 Claude Mamo
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

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

		def expose(name, signature, options = {})

			java_return_type = get_java_type(signature[:returns])

			java_param_types = []
			signature[:expects].each {|expect|
				java_param_types << get_java_type(expect.values.first)
			}

			java_signature = [java_return_type].concat(java_param_types)

			add_method_signature(name.to_s, java_signature)

			exposed_method = { name.to_sym => { label: options[:label], 
										        response_wrapper_name: options[:response_wrapper_name], 
										        out_parameter_name: options[:out_parameter_name], 
										        expects: signature[:expects], returns: signature[:returns] } }
			
			@web_service_definition ||= {}

			if @web_service_definition.has_key? 'exposed_methods'
				@web_service_definition['exposed_methods'] = @web_service_definition['exposed_methods'].merge(exposed_method)
			else	
				@web_service_definition['exposed_methods'] = exposed_method
			end	
		end	

	end	
end