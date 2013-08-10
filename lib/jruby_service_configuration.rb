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
				QName.new(op.getName().getNamespaceURI(), @declared_methods[method.get_name][:response_wrapper_name].to_s)
			else
				super
			end		
		end

		def getOutParameterName(op, method, paramNumber)
			unless @declared_methods[method.get_name][:out_parameter_name].nil?
				QName.new(op.getName().getNamespaceURI(), @declared_methods[method.get_name][:out_parameter_name].to_s)
			else
				super
			end		
		end	

	    def getInParameterName(op, method, paramNumber)
	        QName.new(op.get_name.get_namespace_uri, @declared_methods[method.get_name][:in_parameter_names][paramNumber].to_s)
	    end

	    def getOperationName(intf, method)
	    	unless @declared_methods[method.get_name][:label].nil?
				QName.new(intf.get_name.get_namespace_uri, @declared_methods[method.get_name][:label].to_s)
			else
				super
			end					    			
		end	    	

		def initialize
			@declared_methods = {}
		end

	end

end
