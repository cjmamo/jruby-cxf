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
			@complex_type_definition ||= {}
			
			add_accessors(name, "@#{name}", type)

			member = {name => {type: type, required: options[:required].nil? ? true : options[:required]}}

			if @complex_type_definition.has_key? 'members'
				@complex_type_definition['members'] = @complex_type_definition['members'].merge(member)
			else
				@complex_type_definition['members'] = member
			end	

		end	

		private 

		def add_accessors(property_name, ivar_name, type)
			property_name_capitalized = String.new property_name
			property_name_capitalized[0] = property_name[0].capitalize

			setter_name = ("set" + property_name_capitalized).to_sym
			getter_name = ("get" + property_name_capitalized).to_sym

			# for Aegis
			send :define_method, setter_name do |value|
				setter(ivar_name, value)
			end

			# for Ruby
			send :define_method, underscore(property_name) + '=' do |value|
				setter(ivar_name, value)
			end
			
			# for Aegis
			send :define_method, getter_name do
				getter ivar_name
			end

			# for Ruby
			send :define_method, underscore(property_name) do
				getter ivar_name
			end

			java_param_type = get_java_type(type)

			add_method_signature(setter_name, [java.lang.Void, java_param_type])
			add_method_signature(getter_name, [java_param_type])

		end	

    	def underscore(camel_cased_word)
		    word = camel_cased_word.to_s.dup
		    word.gsub!(/::/, '/')
		    word.gsub!(/([A-Z]+)([A-Z][a-z])/,'\1_\2')
		    word.gsub!(/([a-z\d])([A-Z])/,'\1_\2')
		    word.tr!("-", "_")
		    word.downcase!
		    word
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
