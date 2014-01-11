require 'jruby_cxf'
Dir.glob(File.dirname(File.absolute_path(__FILE__)) + '/examples/*', &method(:require))

describe CXF::WebServiceServlet do

  context WebServiceWithExposedMethodsHavingResponseWrapperNameSet do
    subject(:wsdl) { @web_service.wsdl }

    before(:all) do
      @web_service = WebServiceWithExposedMethodsHavingResponseWrapperNameSet.new
      @web_service.publish
    end

    after(:all) do
      @web_service.teardown
    end

    it "gives a WSDL with response wrapper elements named according to given response wrapper names" do
      wsdl['definitions']['types']['schema']['complexType'][1]['name'].should eq 'GiveAgeResponseWrapper'
      wsdl['definitions']['types']['schema']['complexType'][3]['name'].should eq 'SayHelloResponseWrapper'
    end

  end

  context WebServiceWithExposedMethodsHavingOutParameterNameSet do
    subject(:wsdl) { @web_service.wsdl }

    before(:all) do
      @web_service = WebServiceWithExposedMethodsHavingOutParameterNameSet.new
      @web_service.publish
    end

    after(:all) do
      @web_service.teardown
    end

    it "gives a WSDL with return value elements named according to given out parameter names" do
      wsdl['definitions']['types']['schema']['complexType'][1]['sequence']['element']['name'].should eq 'OutGiveAge'
      wsdl['definitions']['types']['schema']['complexType'][3]['sequence']['element']['name'].should eq 'OutSayHello'
    end

  end

  context WebServiceWithExposedMethodsHavingLabels do
    subject(:wsdl) { @web_service.wsdl }

    before(:all) do
      @web_service = WebServiceWithExposedMethodsHavingLabels.new
      @web_service.publish
    end

    after(:all) do
      @web_service.teardown
    end

    it "gives a WSDL that has its operations name according to labels given" do
      wsdl['definitions']['binding']['operation'][0]['name'].should eq 'GiveAge'
      wsdl['definitions']['binding']['operation'][1]['name'].should eq 'SayHello'
    end

  end

  context WebServiceWithExposedMethods do
    subject(:wsdl) { @web_service.wsdl }

    before(:all) do
      @web_service = WebServiceWithExposedMethods.new
      @web_service.publish
    end

    after(:all) do
      @web_service.teardown
    end

    it "gives a WSDL with declared methods exposed" do
      wsdl['definitions']['types']['schema']['complexType'][0]['sequence']['element']['type'].should eq 'xsd:int'
      wsdl['definitions']['types']['schema']['complexType'][1]['sequence']['element']['type'].should eq 'xsd:int'
      wsdl['definitions']['types']['schema']['complexType'][2]['sequence']['element']['type'].should eq 'xsd:string'
      wsdl['definitions']['types']['schema']['complexType'][3]['sequence']['element']['type'].should eq 'xsd:string'
      wsdl['definitions']['binding']['operation'][0]['name'].should eq 'give_age'
      wsdl['definitions']['binding']['operation'][1]['name'].should eq 'say_hello'
    end
  end

  context WebServiceWithExposedMethods do
    subject(:wsdl) { @web_service.wsdl }

    before(:all) do
      @web_service = WebServiceWithExposedMethods.new
      @web_service.publish
    end

    after(:all) do
      @web_service.teardown
    end

    it "gives a WSDL with declared methods exposed" do
      wsdl['definitions']['types']['schema']['complexType'][0]['sequence']['element']['type'].should eq 'xsd:int'
      wsdl['definitions']['types']['schema']['complexType'][1]['sequence']['element']['type'].should eq 'xsd:int'
      wsdl['definitions']['types']['schema']['complexType'][2]['sequence']['element']['type'].should eq 'xsd:string'
      wsdl['definitions']['types']['schema']['complexType'][3]['sequence']['element']['type'].should eq 'xsd:string'
      wsdl['definitions']['binding']['operation'][0]['name'].should eq 'give_age'
      wsdl['definitions']['binding']['operation'][1]['name'].should eq 'say_hello'
    end
  end

   context WebServiceWithRequiredProperty do
      subject(:wsdl) { @web_service.wsdl }

      before(:all) do
        @web_service = WebServiceWithRequiredProperty.new
        @web_service.publish
      end

      after(:all) do
        @web_service.teardown
      end

      it "gives a WSDL with non-required members having minOccurs set to 0" do
        wsdl['definitions']['types']['schema'][0]['complexType']['sequence']['element'][2]['minOccurs'].should eq '0'
      end

      it "gives a WSDL with required members having no minOccurs" do
        wsdl['definitions']['types']['schema'][0]['complexType']['sequence']['element'][0]['minOccurs'].should eq nil
        wsdl['definitions']['types']['schema'][0]['complexType']['sequence']['element'][1]['minOccurs'].should eq nil
      end
    end

  context WebServiceWithServiceNameSet do
    subject(:wsdl) { @web_service.wsdl }

    before(:all) do
      @web_service = WebServiceWithServiceNameSet.new
      @web_service.publish
    end

    after(:all) do
      @web_service.teardown
    end

    it "gives a WSDL with the configured service name" do
      wsdl['definitions']['service']['name'].should eq 'FooService'
    end
  end

  context WebServiceWithEndpointNameSet do
    subject(:wsdl) { @web_service.wsdl }

    before(:all) do
      @web_service = WebServiceWithEndpointNameSet.new
      @web_service.publish
    end

    after(:all) do
      @web_service.teardown
    end

    it "gives a WSDL with the port name matching the configured endpoint name" do
      wsdl['definitions']['service']['port']['name'].should eq 'FooPort'
    end
  end

  context WebServiceWithServiceNamespaceSet do
    subject(:wsdl) { @web_service.wsdl }

    before(:all) do
      @web_service = WebServiceWithServiceNamespaceSet.new
      @web_service.publish
    end

    after(:all) do
      @web_service.teardown
    end

    it "gives a WSDL with the service target namespace matching the configured service namespace" do
      wsdl['definitions']['targetNamespace'].should eq 'http://jruby-cxf.org'
    end
  end

end