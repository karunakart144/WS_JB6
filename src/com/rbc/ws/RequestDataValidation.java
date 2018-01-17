package com.rbc.ws;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.log4j.Logger;

import com.rbc.ws.bean.DocumentServicesType;
import com.rbc.ws.bean.DocumentType;
import com.rbc.ws.bean.MetaDataFieldType;
import com.rbc.ws.bean.PackageType;

public class RequestDataValidation {

	static Logger log = Logger.getLogger(RequestDataValidation.class);

	public static boolean mandatoryStaticFieldCheck(DocumentServicesType reqObj){
		String applicationCode = reqObj.getUserAuthRequest().getApplication().getApplicationCode();
		String languageCode = reqObj.getUserAuthRequest().getApplication().getLanguageCode();
		String userId = reqObj.getUserAuthRequest().getUserID();
		String userRole = reqObj.getUserAuthRequest().getUserRole();
		String transactionGUID = reqObj.getDocumentServiceRequest().getTransactionGUID();
		String transactionType = reqObj.getDocumentServiceRequest().getTransactionType();
		String transactionTime = reqObj.getDocumentServiceRequest().getTransactionTime();
		String transactionDate = reqObj.getDocumentServiceRequest().getTransactionDate();
		boolean includeDocumentImage = reqObj.getDocumentServiceRequest().isIncludeDocumentImage();
		
		log.debug("Mandatory Fields Information (other than metafields) ... "+
				"applicationCode = "+applicationCode+
				"...languageCode = "+languageCode+
				"...userId = "+userId+
				"...userRole = "+userRole+
				"...transactionGUID = "+transactionGUID+
				"...transactionType = "+transactionType+
				"...transactionTime = "+transactionTime+
				"...transactionDate = "+transactionDate+
				"...includeDocumentImage = "+includeDocumentImage);
		
		if((applicationCode != null && !"".equals(applicationCode))
				&& (languageCode != null && !"".equals(languageCode))
				&& (userId != null  && !"".equals(userId))
				&& (userRole != null && !"".equals(userRole))
				&& (transactionGUID != null && !"".equals(transactionGUID))
				&& (transactionType != null && !"".equals(transactionType))
				&& (transactionTime != null && !"".equals(transactionTime))
				&& (transactionDate != null && !"".equals(transactionDate))
				&& (includeDocumentImage == false || includeDocumentImage == true)	)
			return true;
		return false;
	}
	
	public static boolean mandatoryMetaFieldCheck(DocumentServicesType reqObj){
		
		 MultiMap metaFields = new MultiValueMap();
		 boolean validationResult = false;
		 StringBuilder errorMessage = new StringBuilder();
		for (PackageType p : reqObj.getDocumentServiceRequest().getPackages().getPackage()) {

			for (DocumentType d : p.getDocuments().getDocument()) {
				
				for (MetaDataFieldType m : d.getMetaDataFields().getMetaDataField()) {
					
					 metaFields.put(m.getName(), m.getValue());
					 
				}
			}
		}
		
		try {
		
			Properties mandatoryMetaFields = new Properties();
			mandatoryMetaFields.load(new FileInputStream("D:\\properties\\mandatoryMetaFields.properties"));
			
			Enumeration<?> mandatoryFields = mandatoryMetaFields.propertyNames();
			while (mandatoryFields.hasMoreElements()) {
				String mandatoryField = (String) mandatoryFields.nextElement();
				boolean isFieldPresent = metaFields.containsKey(mandatoryField);
				if(!isFieldPresent){
					errorMessage.append("Mandatory Field --> "+mandatoryField+" is not present in the request \n");
					log.error(errorMessage);
					throw new NullPointerException();
				}
				validationResult = true;
			}
			
			if(metaFields.containsKey("DocType")){
				List<String> docTypeValueList = (ArrayList)metaFields.get("DocType");
				Properties docType = new Properties();
				docType.load(new FileInputStream("D:\\properties\\docType.properties"));
				for(String docTypeValue : docTypeValueList){
					if(!docType.containsKey(docTypeValue)){				
						errorMessage.append("DocType Value "+docTypeValue+" is not a valid one \n");
						log.error(errorMessage);
						validationResult = false;
						break;
					} 
				}
			}
			
			if(metaFields.containsKey("DocSubType")){
				List<String> docSubTypeValueList = (ArrayList)metaFields.get("DocSubType");
				Properties docSubType = new Properties();
				docSubType.load(new FileInputStream("D:\\properties\\docSubType.properties"));
				for(String docSubTypeValue : docSubTypeValueList){
					if(!docSubType.containsKey(docSubTypeValue)){				
						errorMessage.append("DocSubType Value "+docSubTypeValue+" is not a valid one \n");
						log.error(errorMessage);
						validationResult = false;
						break;
					} 
				}
			}
			
			if(metaFields.containsKey("BusinessUnit")){
				List<String> buValueList = (ArrayList)metaFields.get("BusinessUnit");
				Properties businessUnit = new Properties();
				businessUnit.load(new FileInputStream("D:\\properties\\businessUnit.properties"));
				for(String buValue : buValueList){
					if(!businessUnit.containsKey(buValue)){				
						errorMessage.append("businessUnit Value "+buValue+" is not a valid one \n");
						log.error(errorMessage);
						validationResult = false;
						break;
					} 
				}
			}
			
			if(metaFields.containsKey("FunctionalGroup")){
				List<String> fgValueList = (ArrayList)metaFields.get("FunctionalGroup");
				Properties functionalGroup = new Properties();
				functionalGroup.load(new FileInputStream("D:\\properties\\functionalGroup.properties"));
				for(String fgValue : fgValueList){
					if(!functionalGroup.containsKey(fgValue)){				
						errorMessage.append("FunctionalGroup Value "+fgValue+" is not a valid one \n");
						log.error(errorMessage);
						validationResult = false;
						break;
					} 
				}
			}
			
			if(metaFields.containsKey("LineOfBusiness")){
				List<String> lobValueList = (ArrayList)metaFields.get("LineOfBusiness");
				Properties lineOfBusiness = new Properties();
				lineOfBusiness.load(new FileInputStream("D:\\properties\\lineOfBusiness.properties"));
				for(String lobValue : lobValueList){
					if(!lineOfBusiness.containsKey(lobValue)){				
						errorMessage.append("LineOfBusiness Value "+lobValue+" is not a valid one \n");
						log.error(errorMessage);
						validationResult = false;
						break;
					}
				}
			}
			
			if(metaFields.containsKey("SourceSystemCode")){
				List<String> sscValueList = (ArrayList)metaFields.get("SourceSystemCode");
				Properties sourceSystemCode = new Properties();
				sourceSystemCode.load(new FileInputStream("D:\\properties\\SourceSystemCode.properties"));
				for(String sscValue : sscValueList){
					if(!sourceSystemCode.containsKey(sscValue)){				
						errorMessage.append("SourceSystemCode Value "+sscValue+" is not a valid one \n");
						log.error(errorMessage);
						validationResult = false;
						break;
					} 
				}
			}
			
			if(metaFields.containsKey("Language")){
				List<String> languageList = (ArrayList)metaFields.get("Language");
				for(String language : languageList){
					if(!language.equalsIgnoreCase("EN") && !language.equalsIgnoreCase("FR")){
						errorMessage.append("language Value "+language+" is not a valid one \n");
						log.error(errorMessage);
						validationResult = false;
						break;
					}
				}
			}
			
			
			ArrayList multiFields = new ArrayList();
			ArrayList singleFields = new ArrayList();
			Set<String> keys = metaFields.keySet();
			for(String key : keys){
				
				 //System.out.println("key: " + key + " value: " + metaFields.get(key));
				 ArrayList  valueList = (ArrayList) metaFields.get(key);
				 //System.out.println(valueList.size());
				 if(valueList.size() > 1){
					 multiFields.add(key);
				 } else if(valueList.size() == 1){
					 singleFields.add(key);
				 }
			}
			
			if (!reqObj.getDocumentServiceRequest().getTransactionType().equals("11")) {
				Properties multiValueProps = new Properties();
				multiValueProps.load(new FileInputStream("D:\\properties\\multiValue.properties"));
				for(int i=0; i<multiFields.size();i++){
					if(!multiValueProps.containsKey(multiFields.get(i))){				
						errorMessage.append("For "+multiFields.get(i)+"  multi values are not allowed \n");
						log.error(errorMessage);
						validationResult = false;
					} 
				}
			}
			log.info(errorMessage);
		//String password = props.getProperty("jdbc.password");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return validationResult;
	}
}
