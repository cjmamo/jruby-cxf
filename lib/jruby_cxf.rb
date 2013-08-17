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

require 'jruby-cxf-1.1.jar'
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

