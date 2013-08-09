require 'rake'

Gem::Specification.new do |s|
  s.name        = 'jruby-cxf'
  s.version     = '1.0.0'
  s.date        = '2010-04-28'
  s.summary     = "A JRuby wrapper for Apache CXF"
  s.description = "A JRuby wrapper for Apache CXF"
  s.author      = 'Claude Mamo'
  s.email       = 'claude.mamo@gmail.com'
  s.files       =  FileList['lib/*.rb', 'target/*.jar'].to_a
  s.homepage    = 'http://rubygems.org/gems/jruby-cxf'
  s.license     = 'Apache License, Version 2.0'
end