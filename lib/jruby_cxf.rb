require 'jruby-cxf-1.0.jar'
require 'complex_type'
require 'web_service_servlet'

module CXF

    private

    def get_java_type(type)
		case type
			when :string
				java.lang.String
			when :int
				java.lang.Integer
			when :float
				java.lang.Float
			when :long
				java.lang.Long
			when :byte
				java.lang.Byte
			when :short
				java.lang.Short
			when :big_decimal
				java.math.BigDecimal
			when :time
				java.sql.Time
			when :double
				java.lang.Double
			when :boolean
				java.lang.Boolean
			when :datetime
				java.util.Date
			when :nil
				java.lang.Void
			else
				Kernel.const_get(type.to_s).become_java!(false)

		end
	end

end

