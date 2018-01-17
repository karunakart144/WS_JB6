
package com.rbc.ws.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for delAll complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="delAll">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="deleteAll" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "delAll", propOrder = {
    "deleteAll"
})
public class DelAll {

    protected boolean deleteAll;

    /**
     * Gets the value of the deleteAll property.
     * 
     */
    public boolean isDeleteAll() {
        return deleteAll;
    }

    /**
     * Sets the value of the deleteAll property.
     * 
     */
    public void setDeleteAll(boolean value) {
        this.deleteAll = value;
    }

}
