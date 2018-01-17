package com.rbc.ws;

import java.sql.SQLException;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

@WebService(targetNamespace = "http://RBCFG.WMS0.DocumentServices/ServiceMessage")
@SOAPBinding(style = Style.DOCUMENT)
public interface DocumentServer{
		
	@WebMethod String serviceRequest(@WebParam(name = "ServiceRequest") String request );	
	@WebMethod String delAll(@WebParam(name = "deleteAll") boolean delAll );	

}