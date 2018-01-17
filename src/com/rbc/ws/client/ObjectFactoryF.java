
package com.rbc.ws.client;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the documentservices.wms0.rbcfg.servicemessage package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactoryF {

    private final static QName _ServiceRequest_QNAME = new QName("http://RBCFG.WMS0.DocumentServices/ServiceMessage", "serviceRequest");
    private final static QName _DelAllResponse_QNAME = new QName("http://RBCFG.WMS0.DocumentServices/ServiceMessage", "delAllResponse");
    private final static QName _ServiceRequestResponse_QNAME = new QName("http://RBCFG.WMS0.DocumentServices/ServiceMessage", "serviceRequestResponse");
    private final static QName _DelAll_QNAME = new QName("http://RBCFG.WMS0.DocumentServices/ServiceMessage", "delAll");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: documentservices.wms0.rbcfg.servicemessage
     * 
     */
    public ObjectFactoryF() {
    }

    /**
     * Create an instance of {@link ServiceRequestResponse }
     * 
     */
    public ServiceRequestResponse createServiceRequestResponse() {
        return new ServiceRequestResponse();
    }

    /**
     * Create an instance of {@link ServiceRequest }
     * 
     */
    public ServiceRequest createServiceRequest() {
        return new ServiceRequest();
    }

    /**
     * Create an instance of {@link DelAllResponse }
     * 
     */
    public DelAllResponse createDelAllResponse() {
        return new DelAllResponse();
    }

    /**
     * Create an instance of {@link DelAll }
     * 
     */
    public DelAll createDelAll() {
        return new DelAll();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServiceRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://RBCFG.WMS0.DocumentServices/ServiceMessage", name = "serviceRequest")
    public JAXBElement<ServiceRequest> createServiceRequest(ServiceRequest value) {
        return new JAXBElement<ServiceRequest>(_ServiceRequest_QNAME, ServiceRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DelAllResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://RBCFG.WMS0.DocumentServices/ServiceMessage", name = "delAllResponse")
    public JAXBElement<DelAllResponse> createDelAllResponse(DelAllResponse value) {
        return new JAXBElement<DelAllResponse>(_DelAllResponse_QNAME, DelAllResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServiceRequestResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://RBCFG.WMS0.DocumentServices/ServiceMessage", name = "serviceRequestResponse")
    public JAXBElement<ServiceRequestResponse> createServiceRequestResponse(ServiceRequestResponse value) {
        return new JAXBElement<ServiceRequestResponse>(_ServiceRequestResponse_QNAME, ServiceRequestResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DelAll }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://RBCFG.WMS0.DocumentServices/ServiceMessage", name = "delAll")
    public JAXBElement<DelAll> createDelAll(DelAll value) {
        return new JAXBElement<DelAll>(_DelAll_QNAME, DelAll.class, null, value);
    }

}
