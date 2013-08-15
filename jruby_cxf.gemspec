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

require 'rake'
require 'fileutils'

Dir.glob('target/*.jar') {|f| FileUtils.cp File.expand_path(f), 'lib'}

Gem::Specification.new do |s|
  s.name        = 'jruby-cxf'
  s.version     = '1.0.2'
  s.date        =  Time.now.strftime("%Y-%m-%d")
  s.summary     = "A wrapper for Apache CXF"
  s.description = "JRuby CXF is a JRuby gem that wraps the Apache CXF framework to provide a friendlier API for publishing SOAP Web Services."
  s.author      = 'Claude Mamo'
  s.email       = 'claude.mamo@gmail.com'
  s.files       =  FileList['lib/*.rb', 'lib/*.jar'].to_a
  s.homepage    = 'http://github.com/claudemamo/jruby-cxf'
  s.license     = 'Apache License, Version 2.0'
  s.test_files  =  FileList['spec/**/*'].to_a
end