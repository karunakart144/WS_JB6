package com.rbc.ws.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.bind.JAXB;

import org.apache.log4j.Logger;

import com.rbc.ws.bean.ApplicationType;
import com.rbc.ws.bean.ContentType;
import com.rbc.ws.bean.DocumentServiceRequestType;
import com.rbc.ws.bean.DocumentServicesType;
import com.rbc.ws.bean.DocumentType;
import com.rbc.ws.bean.DocumentsType;
import com.rbc.ws.bean.MetaDataFieldType;
import com.rbc.ws.bean.MetaDataFields;
import com.rbc.ws.bean.ObjectFactory;
import com.rbc.ws.bean.ObjectMimeType;
import com.rbc.ws.bean.PackageType;
import com.rbc.ws.bean.PackagesType;
import com.rbc.ws.bean.UserAuthRequestType;


public class DocumentServerClient {
       final static ObjectFactory of = new ObjectFactory();
       static Logger log = Logger.getLogger(DocumentServerClient.class);

       public String serviceCall(Map<String, String> staticValues, Map<String, String> metaFieldsMap) {
              log.info("In client program for accessing the service ");
              DocumentService docService = new DocumentService();
              DocumentServer docServer = docService.getDocumentServerImplPort();
              try {

                     DocumentServicesType req = of.createDocumentServicesType();
                     UserAuthRequestType userAuth = of.createUserAuthRequestType();
                     
                     String value = getMapValue(staticValues, "UserID");
                     userAuth.setUserID(value==null?"SYSTEM":value);
                     
                     value = getMapValue(staticValues, "UserRole");
                     userAuth.setUserRole(value==null?"NA":value);
                     
                     ApplicationType app = of.createApplicationType();
                     
                     app.setApplicationCode("SXL0");
                     
                     value = getMapValue(staticValues, "LanguageCode");
                     app.setLanguageCode(value==null?"EN":value);
                     userAuth.setApplication(app);

                     DocumentServiceRequestType docRequestType = of.createDocumentServiceRequestType();

                     value = getMapValue(staticValues, "TransactionGUID");
                     if(value == null){
                           log.error("TransactionGUID is mandatory. space assumed.");
                           value = "";
                     }
                     docRequestType.setTransactionGUID(value);

                     value = getMapValue(staticValues, "TransactionType");
                     log.info(" TransactionType value is " + value);
                     docRequestType.setTransactionType(value);
                     
                     value = getTransctionDesc(value);
                     if(value == null){
                           log.error("Invalid TransactionType value " + value);
                           throw new RuntimeException("Invalid TransactionType value " + value);
                     }
                     docRequestType.setTransactionDescription(value);
                     
                     
                     docRequestType.setTransactionDate(getMapValue(staticValues, "TransactionDate"));
                     docRequestType.setTransactionTime(getMapValue(staticValues, "TransactionTime"));

                     docRequestType.setIncludeDocumentImage(getMapValue(staticValues, "IncludeDocumentImage").equalsIgnoreCase("false")? false : true);

                     MetaDataFields mdfields = of.createMetaDataFields();

                     PackagesType reqPkgs = of.createPackagesType();
                     PackageType reqPkg = of.createPackageType();
                     DocumentsType reqDocs = of.createDocumentsType();
                     DocumentType reqDoc = of.createDocumentType();
                     ContentType imageContent = of.createContentType();
                     
              
                     if (getMapValue(staticValues, "TransactionType").equals("8")) {// Storage
                           value = getMapValue(staticValues, "objectContent");
                           if(value == null){
                                  log.error("objectContent not provided for  TransactionType = 8 i.e. Storage");
                                  throw new RuntimeException("objectContent not provided for  TransactionType = 8 i.e. Storage");
                           }
                           
                           log.info("Attempting to read file " + value);

                           File file = new File(value);
                           // Reading a Image file from file system
                           FileInputStream imageInFile = new FileInputStream(file);
                           byte imageData[] = new byte[(int) file.length()];
                           imageInFile.read(imageData);
                           imageContent.setObjectContent(imageData);
                           imageInFile.close();

                           value = getMapValue(staticValues, "objectMimeType");
                           if(value != null){
                                  if(value.equalsIgnoreCase("image/tiff"))
                                         imageContent.setObjectMimeType(ObjectMimeType.IMAGE_TIFF);
                                  else if(value.equalsIgnoreCase("application/pdf"))
                                         imageContent.setObjectMimeType(ObjectMimeType.APPLICATION_PDF);
                                  else {
                                         log.error("Invalid objectMimeType = " + value);
                                         throw new RuntimeException("Invalid objectMimeType = " + value);
                                  }
                           } else {
                                  log.error("Invalid objectMimeType = " + value);
                                  throw new RuntimeException("Invalid objectMimeType = " + value);
                           }
                           
                           reqDoc.setContent(imageContent);
                     }

                    Properties multiValueProps = new Properties();
         			multiValueProps.load(new FileInputStream("D:\\properties\\multiValue.properties"));
         			if (getMapValue(staticValues, "TransactionType").equals("11")) {// Update index
         				for(Entry<String, String> e: metaFieldsMap.entrySet()){
         					 StringTokenizer st = new StringTokenizer(e.getValue(), "+");
               			 	while (st.hasMoreElements()) {
               			 		MetaDataFieldType mdfield = of.createMetaDataFieldType();
               			 		mdfield.setName(e.getKey());
               			 		mdfield.setValue((String)st.nextElement());
               			 		mdfields.getMetaDataField().add(mdfield); 
               				}
         				}
         			}else{
	                     for(Entry<String, String> e: metaFieldsMap.entrySet()){
	                           
	                           if(multiValueProps.containsKey(e.getKey())){
	                        	   log.info("multi value key --> "+e.getKey()+"....value is "+e.getValue());
	                        	   StringTokenizer st = new StringTokenizer(e.getValue(), "+");
	                  			 	while (st.hasMoreElements()) {
	                  			 		MetaDataFieldType mdfield = of.createMetaDataFieldType();
	                  			 		mdfield.setName(e.getKey());
	                  			 		mdfield.setValue((String)st.nextElement());
	                  			 		mdfields.getMetaDataField().add(mdfield); 
	                  				}
	                           } else{
	                        	   log.info("not a multi value key --> "+e.getKey()+"....value is "+e.getValue());
	                        	   MetaDataFieldType mdfield = of.createMetaDataFieldType();
	                        	   mdfield.setName(e.getKey());
	                        	   mdfield.setValue(e.getValue());
	                        	   mdfields.getMetaDataField().add(mdfield);
	                           }
	                     }
         			}

                     reqDoc.setMetaDataFields(mdfields);
                     reqDocs.getDocument().add(reqDoc);
                     reqPkg.setDocuments(reqDocs);
                     reqPkgs.getPackage().add(reqPkg);
                     docRequestType.setPackages(reqPkgs);
                     //docRequestType.setMetaDataFields(mdfields);
                     req.setUserAuthRequest(userAuth);
                     req.setDocumentServiceRequest(docRequestType);
                     StringWriter strWriter = new StringWriter();
                     JAXB.marshal(req, strWriter);
                     //log.debug(".....serviceRequest is ........" + strWriter.toString());
                     log.info(".....serviceRequest is ........" + strWriter.toString());
                     String serviceResponse = docServer.serviceRequest(strWriter.toString());
                     //log.debug(".....serviceResponse is ........" + serviceResponse);
                     log.info(".....serviceResponse is ........" + serviceResponse);
                     return serviceResponse;
              } catch (Exception e) {
                     log.error("Encountered Exception" + e);
                     throw new RuntimeException(e);
              }
       }

       public String getMapValue(Map<String, String> m, String key) {
              return m.get(key);
       }
       
       public String getTransctionDesc(String type){
             
              
              if(type.equals("1"))
                     return "Search";

              if(type.equals("7"))
                     return "Get Content";

              if(type.equals("8"))
                  return "Storage";
              
              if(type.equals("9"))
                  return "Add Index";
              
              if(type.equals("10"))
                  return "Remove Index";
              
              if(type.equals("11"))
                  return "Update Index";

              return "";
       }
}