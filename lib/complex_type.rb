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
	module ComplexType
		include CXF

		attr_accessor :complex_type_definition

		def self.included(base)
			base.extend(self)
		end

		def namespace(namespace)
			@complex_type_definition ||= {}
			@complex_type_definition['namespace'] = namespace
		end	

		def member(name, type, options = {})
			name = name.to_s
			java_name = options[:label].nil? ? name : options[:label]
			@complex_type_definition ||= {}
			
			add_accessors(name, java_name, "@#{name}", type)

			member = {name => {type: type, 
				               required: options[:required].nil? ? true : options[:required], 
				               label: java_name}}

			if @complex_type_definition.has_key? 'members'
				@complex_type_definition['members'] = @complex_type_definition['members'].merge(member)
			else
				@complex_type_definition['members'] = member
			end	

		end	

		private 

		def add_accessors(ruby_property_name, java_property_name, ivar_name, type)
			java_property_name_capitalized = String.new java_property_name
			java_property_name_capitalized[0] = java_property_name[0].capitalize

			setter_name = ("set" + java_property_name_capitalized).to_sym
			getter_name = ("get" + java_property_name_capitalized).to_sym

			# for Aegis
			send :define_method, setter_name do |value|
				setter(ivar_name, value)
			end

			# for Ruby
			send :define_method, ruby_property_name + '=' do |value|
				setter(ivar_name, value)
			end
			
			# for Aegis
			send :define_method, getter_name do
				getter ivar_name
			end

			# for Ruby
			send :define_method, ruby_property_name do
				getter ivar_name
			end

			java_param_type = get_java_type(type)

			add_method_signature(setter_name, [java.lang.Void, java_param_type])
			add_method_signature(getter_name, [java_param_type])

		end	

		def setter(ivar_name, value)
			instance_variable_set(ivar_name, value)
			return nil
		end
		
		def getter(ivar_name)
			instance_variable_get(ivar_name)
		end	
	end
end
