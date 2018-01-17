//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.11.02 at 02:23:18 PM IST 
//


package com.rbc.ws.bean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DocumentServicesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DocumentServicesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;sequence>
 *           &lt;element ref="{http://RBCFG.WMS0.DocumentServices/ServiceMessage}UserAuthRequest" minOccurs="0"/>
 *           &lt;element ref="{http://RBCFG.WMS0.DocumentServices/ServiceMessage}DocumentServiceRequest" minOccurs="0"/>
 *         &lt;/sequence>
 *         &lt;sequence>
 *           &lt;element ref="{http://RBCFG.WMS0.DocumentServices/ServiceMessage}UserAuthResponse" minOccurs="0"/>
 *           &lt;element ref="{http://RBCFG.WMS0.DocumentServices/ServiceMessage}DocumentServiceResponse" minOccurs="0"/>
 *         &lt;/sequence>
 *       &lt;/choice>
 *       &lt;attribute name="Version" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DocumentServicesType", propOrder = {
    "userAuthRequest",
    "documentServiceRequest",
    "userAuthResponse",
    "documentServiceResponse"
})
public class DocumentServicesType {
	
	public DocumentServicesType(){
	}

    @XmlElement(name = "UserAuthRequest")
    protected UserAuthRequestType userAuthRequest;
    @XmlElement(name = "DocumentServiceRequest")
    protected DocumentServiceRequestType documentServiceRequest;
    @XmlElement(name = "UserAuthResponse")
    protected UserAuthResponseType userAuthResponse;
    @XmlElement(name = "DocumentServiceResponse")
    protected DocumentServiceResponseType documentServiceResponse;
    @XmlAttribute(name = "Version")
    protected String version;

    /**
     * Gets the value of the userAuthRequest property.
     * 
     * @return
     *     possible object is
     *     {@link UserAuthRequestType }
     *     
     */
    public UserAuthRequestType getUserAuthRequest() {
        return userAuthRequest;
    }

    /**
     * Sets the value of the userAuthRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserAuthRequestType }
     *     
     */
    public void setUserAuthRequest(UserAuthRequestType value) {
        this.userAuthRequest = value;
    }

    /**
     * Gets the value of the documentServiceRequest property.
     * 
     * @return
     *     possible object is
     *     {@link DocumentServiceRequestType }
     *     
     */
    public DocumentServiceRequestType getDocumentServiceRequest() {
        return documentServiceRequest;
    }

    /**
     * Sets the value of the documentServiceRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentServiceRequestType }
     *     
     */
    public void setDocumentServiceRequest(DocumentServiceRequestType value) {
        this.documentServiceRequest = value;
    }

    /**
     * Gets the value of the userAuthResponse property.
     * 
     * @return
     *     possible object is
     *     {@link UserAuthResponseType }
     *     
     */
    public UserAuthResponseType getUserAuthResponse() {
        return userAuthResponse;
    }

    /**
     * Sets the value of the userAuthResponse property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserAuthResponseType }
     *     
     */
    public void setUserAuthResponse(UserAuthResponseType value) {
        this.userAuthResponse = value;
    }

    /**
     * Gets the value of the documentServiceResponse property.
     * 
     * @return
     *     possible object is
     *     {@link DocumentServiceResponseType }
     *     
     */
    public DocumentServiceResponseType getDocumentServiceResponse() {
        return documentServiceResponse;
    }

    /**
     * Sets the value of the documentServiceResponse property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentServiceResponseType }
     *     
     */
    public void setDocumentServiceResponse(DocumentServiceResponseType value) {
        this.documentServiceResponse = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

}