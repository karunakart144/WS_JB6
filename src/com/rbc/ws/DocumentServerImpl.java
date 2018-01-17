package com.rbc.ws;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.jws.WebService;
import javax.xml.bind.JAXB;
import javax.xml.ws.soap.MTOM;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.log4j.Logger;

import com.rbc.ws.bean.ContentType;
import com.rbc.ws.bean.DocumentServiceResponseType;
import com.rbc.ws.bean.DocumentServicesType;
import com.rbc.ws.bean.DocumentType;
import com.rbc.ws.bean.DocumentsType;
import com.rbc.ws.bean.MetaDataFieldType;
import com.rbc.ws.bean.MetaDataFields;
import com.rbc.ws.bean.ObjectFactory;
import com.rbc.ws.bean.ObjectMimeType;
import com.rbc.ws.bean.PackageType;
import com.rbc.ws.bean.TransactionResultType;
import com.rbc.ws.bean.UserAuthResponseType;

@MTOM
@WebService(endpointInterface = "com.rbc.ws.DocumentServer", serviceName = "DocumentService", targetNamespace = "http://RBCFG.WMS0.DocumentServices/ServiceMessage")
public class DocumentServerImpl implements DocumentServer {

	static Logger log = Logger.getLogger(DocumentServerImpl.class);
	// storage related queries
	final String path = "D:\\STORE\\";
	final String insertIndex = "INSERT INTO DOCIDX (TRAN_DATE, TRAN_TIME, DOCGUID, MIME_TYPE) VALUES (?,?,?,?);SELECT @@IDENTITY";
	final String insertMetadata = "INSERT INTO DOCMETADATA (DOC_ID, META_NAME, META_VALUE, VALUE_SET) VALUES (?,?,?,?)";

	// getContent related queries
	final String selectMimetype = "SELECT MIME_TYPE FROM DOCIDX WHERE DOCGUID = ?";
	
	//add multiple indexes to existing document
	final String selectDocid = "SELECT ID FROM DOCIDX WHERE DOCGUID = ?";
	final String selectValueSet = "SELECT MAX(VALUE_SET) FROM DOCMETADATA WHERE DOC_ID = ?";
	
	//remove multiple indexes to existing document
	final String selectCountValueSet = "SELECT COUNT(DISTINCT VALUE_SET) FROM DOCMETADATA WHERE DOC_ID = ?";

	final ObjectFactory of = new ObjectFactory();
	

	@Override
	public String serviceRequest(String request) {
		DocumentServicesType respObj = null;
		StringWriter sw = new StringWriter();
		try {
			DocumentServicesType reqObj = JAXB.unmarshal(new StringReader(request), DocumentServicesType.class);
			log.debug("Service Request Processing ....... request xml is  ...." + request);
			
			if(RequestDataValidation.mandatoryStaticFieldCheck(reqObj)){
					if(RequestDataValidation.mandatoryMetaFieldCheck(reqObj)){
				
						if (reqObj.getDocumentServiceRequest().getTransactionType().equals("1")) {// Search document
							respObj = searchMetadata(reqObj);
							log.info("TransactionType from request is ..." + reqObj.getDocumentServiceRequest().getTransactionType());
						} else if (reqObj.getDocumentServiceRequest().getTransactionType().equals("7")) {// Get Content
							respObj = getDocContentById(reqObj);
							log.info("TransactionType from request is ..." + reqObj.getDocumentServiceRequest().getTransactionType());
						} else if (reqObj.getDocumentServiceRequest().getTransactionType().equals("8")) {// Store the document
							log.info("TransactionType from request is ..." + reqObj.getDocumentServiceRequest().getTransactionType());
							respObj = storeDocument(reqObj);
						} else if (reqObj.getDocumentServiceRequest().getTransactionType().equals("9")) {//add multi value index
							respObj = addMultivalueIndex(reqObj);
							log.info("TransactionType from request is ..." + reqObj.getDocumentServiceRequest().getTransactionType());
						} else if (reqObj.getDocumentServiceRequest().getTransactionType().equals("10")) {//remove multi value index
							respObj = removeMultivalueIndex(reqObj);
							log.info("TransactionType from request is ..." + reqObj.getDocumentServiceRequest().getTransactionType());
						} else if (reqObj.getDocumentServiceRequest().getTransactionType().equals("11")) {//update document index
							respObj = updateDocIndex(reqObj);
							log.info("TransactionType from request is ..." + reqObj.getDocumentServiceRequest().getTransactionType());
						}
	
						JAXB.marshal(respObj, sw);
						sw.flush();
					} else {
						log.error("Mandatory meta data fields validation has failed");
					}
			}else{
					log.error("Mandatory static data fields validation has failed");
			}

		} catch (SQLException sql) {
			sql.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sw.toString();
	}

	/**
	 * USE WSDEMO GO
	 * 
	 * CREATE TABLE dbo.DOCIDX ( ID INT IDENTITY(1,1) PRIMARY KEY, TRAN_DATE
	 * NCHAR(10) DEFAULT N'2000-01-01', TRAN_TIME NCHAR(8) DEFAULT N'00-00-00',
	 * CMP_ID NTEXT NOT NULL, MIME_TYPE NTEXT NOT NULL );
	 * 
	 * CREATE TABLE dbo.DOCMETADATA ( ID INT IDENTITY(1,1) PRIMARY KEY, DOC_ID
	 * INT NOT NULL FOREIGN KEY REFERENCES dbo.DOCIDX(ID), META_NAME NTEXT,
	 * META_VALUE NTEXT );
	 * 
	 * @throws Exception
	 */

	private DocumentServicesType storeDocument(DocumentServicesType req) throws Exception {

		log.info("Service Request Processing ...storing the document");
		Connection con = null;
		PreparedStatement psInsertIdx = null, psInsertMeta = null;
		PreparedStatement psUpdate = null;

		String tranDate = req.getDocumentServiceRequest().getTransactionDate();
		String tranTime = req.getDocumentServiceRequest().getTransactionTime();
		DocumentsType respDocs = of.createDocumentsType();
		Properties multiValueProps = new Properties();
		multiValueProps.load(new FileInputStream("D:\\properties\\multiValue.properties"));
		MultiMap metaDataMap = new MultiValueMap();
		try {
			con = getConn();
			psInsertIdx = con.prepareStatement(insertIndex, Statement.RETURN_GENERATED_KEYS);
			psInsertMeta = con.prepareStatement(insertMetadata, Statement.RETURN_GENERATED_KEYS);

			for (PackageType p : req.getDocumentServiceRequest().getPackages().getPackage()) {

				for (DocumentType d : p.getDocuments().getDocument()) {
					String DocGUID = UUID.randomUUID().toString();
					String objectMimeType = d.getContent().getObjectMimeType().value();

					psInsertIdx.setString(1, tranDate);
					psInsertIdx.setString(2, tranTime);
					psInsertIdx.setString(3, DocGUID);
					psInsertIdx.setString(4, objectMimeType);
					psInsertIdx.executeUpdate();

					int docId = -1;
					ResultSet rs = psInsertIdx.getGeneratedKeys();
					while (rs.next()) {
						docId = rs.getInt(1);
					}
					rs.close();

					for (MetaDataFieldType m : d.getMetaDataFields().getMetaDataField()) {
						 
						metaDataMap.put(m.getName(), m.getValue());
					}
					
					ArrayList multiFields = new ArrayList();
					ArrayList singleFields = new ArrayList();
					ArrayList  valueList = new ArrayList();
					ArrayList  subvalueList = new ArrayList();
					
					Set<String> keys = metaDataMap.keySet();
					
					for(String key : keys){
						if(multiValueProps.containsKey(key)){	
							 valueList = (ArrayList) metaDataMap.get(key);
							 log.info("Retreiving the size of multiple values given to the first Multi Value Property...key is "+key+"..."+valueList.size());
							 break;
						}
					}
					
					List<Map> allValueSetList = new ArrayList<Map>();
					log.info("Creating individual sets of values by storing in a collection ");
					for(int i=0; i<valueList.size(); i++){
						Map valueMap = new HashMap();
						for(String key : keys){
							subvalueList = (ArrayList)metaDataMap.get(key);
							if(multiValueProps.containsKey(key)){	
								valueMap.put(key, subvalueList.get(i));
							} else{
								valueMap.put(key, subvalueList.get(0));
							}
						}
						allValueSetList.add(valueMap);
					}
					
					
					int count = 0;
					log.info("Storing the metadata values in the form of sets");
					for(Map singleValueSet :allValueSetList){
						Set<String> keyset = singleValueSet.keySet();
						count++;
						for(String key : keyset){
							psInsertMeta.setInt(1, docId);
							psInsertMeta.setString(2, key);
							psInsertMeta.setString(3, (String)singleValueSet.get(key));
							psInsertMeta.setInt(4, count);
							psInsertMeta.executeUpdate();
							log.debug(count+" value set ..."+"key is "+key+"...value is "+singleValueSet.get(key));
						}
					}
					

					if (d.getContent().getObjectContent() != null) {
						FileOutputStream fos = new FileOutputStream(path + DocGUID);
						fos.write(d.getContent().getObjectContent());
						log.info("Service Request Processing ...File has been created in the store folder");
						fos.close();
					}

					// Add to response
					MetaDataFieldType mdType = of.createMetaDataFieldType();
					mdType.setName("DocGUID");
					mdType.setValue(DocGUID);
					MetaDataFields mdTypes = of.createMetaDataFields();
					mdTypes.getMetaDataField().add(mdType);

					DocumentType respDoc = of.createDocumentType();
					respDoc.setMetaDataFields(mdTypes);
					respDocs.getDocument().add(respDoc);
				}
			}
			con.commit();
		} catch (Exception e) {
			if (con != null) {
				con.rollback();
				con.close();
			}
			throw e;
			// throw new RuntimeException(e);
		} finally {
			if (psInsertIdx != null) {
				psInsertIdx.close();
			}
			if (psUpdate != null) {
				psUpdate.close();
			}
			if (con != null) {
				con.close();
			}
		}

		DocumentServicesType resp = of.createDocumentServicesType();
		UserAuthResponseType userAuth = of.createUserAuthResponseType();
		resp.setUserAuthResponse(userAuth);

		TransactionResultType tr = of.createTransactionResultType();
		userAuth.setTransactionResult(tr);
		tr.setResultCode(0);
		tr.setResultDescription("Success");

		DocumentServiceResponseType docResponseType = of.createDocumentServiceResponseType();
		resp.setDocumentServiceResponse(docResponseType);
		docResponseType.setDocuments(respDocs);
		docResponseType.setIncludeDocumentImage(false);
		docResponseType.setTransactionGUID(req.getDocumentServiceRequest().getTransactionGUID());

		TransactionResultType trType = of.createTransactionResultType();
		docResponseType.setTransactionResult(trType);
		trType.setResultCode(0);
		trType.setResultDescription("Success");
		log.info("Service Request Processing ...Storage has been done successfully");
		return resp;
	}
	
	private DocumentServicesType getDocContentById(DocumentServicesType req) throws Exception {
		log.info("Service Request Processing ...retrieving the document content based on DOC_GUID");
		Connection con = null;
		PreparedStatement psSelectIdx = null;
		String docGUID = null, mime_type = null;
		DocumentsType respDocs = of.createDocumentsType();
		DocumentType respDoc = of.createDocumentType();
		MetaDataFields mdfts = of.createMetaDataFields();
		respDoc.setMetaDataFields(mdfts);
		
		PackageType p = req.getDocumentServiceRequest().getPackages().getPackage().get(0);
		if(p != null){
			DocumentType d = p.getDocuments().getDocument().get(0);
			for (MetaDataFieldType m : d.getMetaDataFields().getMetaDataField()) {
				if (m.getName().equalsIgnoreCase("DocGUID")) {
					docGUID = m.getValue();

					// Add DocGUID to response
					MetaDataFieldType mdft = of.createMetaDataFieldType();
					mdft.setName(m.getName());
					mdft.setValue(m.getValue());
					respDoc.getMetaDataFields().getMetaDataField().add(mdft);
					//respDoc.setMetaDataFields(d.getMetaDataFields());
				}
			}
		}

		boolean success = false;
		byte[] imageInByte = null;
		DocumentServicesType resp = of.createDocumentServicesType();
		try {

			if (docGUID != null) {
				File file = new File(path + docGUID);
				con = getConn();
				psSelectIdx = con.prepareStatement(selectMimetype);
				psSelectIdx.setString(1, docGUID);
				ResultSet selectRs = psSelectIdx.executeQuery();

				if (selectRs.next()) {
					mime_type = selectRs.getString(1);
					log.info("retreived mime_type value from table is " + mime_type);
				} else {
					log.error("retreived mime_type value from table is  null");
				}

				selectRs.close();
				psSelectIdx.close();

				if (file.isFile()) {
					FileInputStream in = new FileInputStream(file);
					FileChannel channel = in.getChannel();
					ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
					channel.read(buffer);
					in.close();
					imageInByte = buffer.array();
					success = true;
				}
			}

			UserAuthResponseType userAuth = of.createUserAuthResponseType();
			resp.setUserAuthResponse(userAuth);

			TransactionResultType tr = of.createTransactionResultType();
			userAuth.setTransactionResult(tr);

			DocumentServiceResponseType docResponseType = of.createDocumentServiceResponseType();
			resp.setDocumentServiceResponse(docResponseType);

			TransactionResultType trType = of.createTransactionResultType();
			docResponseType.setTransactionResult(trType);

			docResponseType.setIncludeDocumentImage(false);
			docResponseType.setTransactionGUID(req.getDocumentServiceRequest().getTransactionGUID());

			if (success) {
				tr.setResultCode(0);
				tr.setResultDescription("Success");

				ContentType contentType = of.createContentType();
				respDoc.setContent(contentType);

				contentType.setObjectContent(imageInByte);
				if (mime_type.equalsIgnoreCase("image/tiff")) {
					contentType.setObjectMimeType(ObjectMimeType.valueOf("IMAGE_TIFF"));
				} else if (mime_type.equalsIgnoreCase("application/pdf")) {
					contentType.setObjectMimeType(ObjectMimeType.valueOf("APPLICATION_PDF"));
				}

				respDocs.getDocument().add(respDoc);
				docResponseType.setDocuments(respDocs);

				trType.setResultCode(0);
				trType.setResultDescription("Success");
				log.info("content retrieval has been completed successfully");

			} else {
				tr.setResultCode(1);
				tr.setResultDescription("Failed to find image");

				trType.setResultCode(1);
				trType.setResultDescription("Failed");

			}
		} catch (Exception e) {
			if (con != null) {
				con.rollback();
				con.close();
			}
			throw e;
			// throw new RuntimeException(e);
		} finally {
			if (psSelectIdx != null) {
				psSelectIdx.close();
			}
			if (con != null) {
				con.close();
			}
		}

		return resp;
	}

	private DocumentServicesType searchMetadata(DocumentServicesType req) throws Exception {
		log.info("Service Request Processing  ...retrieving the document details based on the search fields");
		Connection con = null;
		PreparedStatement psSearchIdx = null, psSearchMeta = null;

		String DOC_GUID, id;
		int rowCount = 0;
		boolean success = false;
		Map<String, String> idMap = new HashMap<String, String>();
		DocumentsType respDocs = of.createDocumentsType();
		StringBuilder docGUID_SearchQuery = new StringBuilder("SELECT DOCGUID, ID FROM DOCIDX WHERE ID IN  ( SELECT DISTINCT data.DOC_ID FROM DOCMETADATA data ");
		StringBuilder joinQuery = new StringBuilder();
		StringBuilder whereQuery = new StringBuilder();
		whereQuery.append(" WHERE ");

		try {

			for (PackageType p : req.getDocumentServiceRequest().getPackages().getPackage()) {

				for (DocumentType d : p.getDocuments().getDocument()) {

					for (MetaDataFieldType m : d.getMetaDataFields().getMetaDataField()) {
						rowCount++;
						joinQuery.append(" JOIN DOCMETADATA data" + rowCount + " ON data.DOC_ID = data" + rowCount + ".DOC_ID AND data.VALUE_SET = data"+rowCount+".VALUE_SET ");
						if (rowCount == 1)
							whereQuery.append("data" + rowCount + ".META_NAME = '" + m.getName() + "' AND data" + rowCount + ".META_VALUE = '" + m.getValue()
									+ "'");
						else if (rowCount > 1)
							whereQuery.append(" AND data" + rowCount + ".META_NAME = '" + m.getName() + "' AND data" + rowCount + ".META_VALUE = '"
									+ m.getValue() + "'");
					}
				}
			}
			docGUID_SearchQuery.append(joinQuery);
			docGUID_SearchQuery.append(whereQuery);
			docGUID_SearchQuery.append(") ");
			log.debug("docGUID_SearchQuery   " + docGUID_SearchQuery);
			con = getConn();
			psSearchIdx = con.prepareStatement(docGUID_SearchQuery.toString());

			ResultSet selectRs = psSearchIdx.executeQuery();

			while (selectRs.next()) {

				success = true;
				DOC_GUID = selectRs.getString(1);
				id = selectRs.getString(2);
				log.debug("......in search method .....selectRs....DOCGUID is " + DOC_GUID + "..id is " + id);
				idMap.put(id, DOC_GUID);
			}
			selectRs.close();
			psSearchIdx.close();

			psSearchMeta = con.prepareStatement("SELECT DISTINCT META_NAME, META_VALUE FROM DOCMETADATA WHERE DOC_ID = ?");

			Iterator itr = idMap.keySet().iterator();
			if (success) {
				while (itr.hasNext()) {
					String key = itr.next().toString();

					psSearchMeta.setString(1, key);
					MetaDataFields mdfs = of.createMetaDataFields();
					MetaDataFieldType mdfType = of.createMetaDataFieldType();
					DocumentType respDoc = of.createDocumentType();

					ResultSet metaFields = psSearchMeta.executeQuery();
					mdfType.setName("DocGUID");
					mdfType.setValue(idMap.get(key));
					mdfs.getMetaDataField().add(mdfType);
					respDoc.setMetaDataFields(mdfs);
					while (metaFields.next()) {
						MetaDataFieldType mdfType1 = of.createMetaDataFieldType();
						mdfType1.setName(metaFields.getString(1));
						mdfType1.setValue(metaFields.getString(2));

						// Add to response
						mdfs.getMetaDataField().add(mdfType1);
						respDoc.setMetaDataFields(mdfs);
					}
					respDocs.getDocument().add(respDoc);
				}
			}

			con.commit();
		} catch (Exception e) {
			if (con != null) {
				con.rollback();
				con.close();
			}
			throw e;
			// throw new RuntimeException(e);
		} finally {
			if (psSearchIdx != null) {
				psSearchIdx.close();
			}
			if (psSearchMeta != null) {
				psSearchMeta.close();
			}

			if (con != null) {
				con.close();
			}
		}

		DocumentServicesType resp = of.createDocumentServicesType();
		UserAuthResponseType userAuth = of.createUserAuthResponseType();
		resp.setUserAuthResponse(userAuth);

		TransactionResultType tr = of.createTransactionResultType();
		userAuth.setTransactionResult(tr);

		DocumentServiceResponseType docResponseType = of.createDocumentServiceResponseType();
		resp.setDocumentServiceResponse(docResponseType);

		TransactionResultType trType = of.createTransactionResultType();
		docResponseType.setTransactionResult(trType);

		docResponseType.setIncludeDocumentImage(false);
		docResponseType.setTransactionGUID(req.getDocumentServiceRequest().getTransactionGUID());

		docResponseType.setDocuments(respDocs);

		if (success) {
			tr.setResultCode(0);
			tr.setResultDescription("Success");

			trType.setResultCode(0);
			trType.setResultDescription("Success");

		} else {
			tr.setResultCode(1);
			tr.setResultDescription("Failure");

			trType.setResultCode(1);
			trType.setResultDescription("Failure");

		}
		log.info("Service Request Processing  ...retrieving the document details based on the search fields has been completed");
		return resp;
	}
	
	private DocumentServicesType addMultivalueIndex(DocumentServicesType reqObj) throws Exception {
		log.info("Service Request Processing ...addMultivalueIndex for the document");
		Connection con = null;
		PreparedStatement  psSelectDocid = null ,psSelectValueSet = null ,psInsertMeta = null;
		String docGUID = null;
		int docId = 0, maxValue = 0;
		boolean success = false;
		Map<String, String> metaDataFields = new HashMap<String, String>();
		
		DocumentsType respDocs = of.createDocumentsType();
		DocumentType respDoc = of.createDocumentType();
		MetaDataFields mdfts = of.createMetaDataFields();
		respDoc.setMetaDataFields(mdfts);
		try {
			con = getConn();
			psSelectDocid = con.prepareStatement(selectDocid);
			psInsertMeta = con.prepareStatement(insertMetadata, Statement.RETURN_GENERATED_KEYS);

			for (PackageType p : reqObj.getDocumentServiceRequest().getPackages().getPackage()) {

				for (DocumentType d : p.getDocuments().getDocument()) {
					
						for (MetaDataFieldType m : d.getMetaDataFields().getMetaDataField()) {
							
							
							if (m.getName().equalsIgnoreCase("DocGUID")){
									if(m.getValue() != null && !m.getValue().isEmpty() ) {
										docGUID = m.getValue();
										log.info("docGUID value from the request  is " + docGUID);
										
										// Add DocGUID to response
										MetaDataFieldType mdft = of.createMetaDataFieldType();
										mdft.setName(m.getName());
										mdft.setValue(m.getValue());
										respDoc.getMetaDataFields().getMetaDataField().add(mdft);
										//respDoc.setMetaDataFields(d.getMetaDataFields());
									} else{
										log.error("DocGUID value is empty in the request  ");
										throw new NullPointerException("DocGUID value is empty in the request ");
									}
							}else{
								metaDataFields.put(m.getName(), m.getValue());
							}
					}
				}
			}
			psSelectDocid.setString(1, docGUID);
			ResultSet docIdRs = psSelectDocid.executeQuery();

			if (docIdRs.next()) {
				docId = docIdRs.getInt(1);
				success = (docId > 0 ? true : false);
				log.info("doc_id value for the given docGUID value is  " + docId + "...success is " + success);
			} else {
				log.error("doc_id value retrieved from the table for the given docGUID value is null ");
			}
			docIdRs.close();
			psSelectDocid.close();
			
			if (success) {
				psSelectValueSet = con.prepareStatement(selectValueSet);
				psSelectValueSet.setInt(1, docId);
				ResultSet valueSetRs = psSelectValueSet.executeQuery();
				if (valueSetRs.next()) {
					maxValue = valueSetRs.getInt(1)+1;
				}
				valueSetRs.close();
				psSelectValueSet.close();
				
				for(Map.Entry<String, String> entry: metaDataFields.entrySet()){
					//if(!entry.getKey().equalsIgnoreCase("DocGUID")){
						//inserting the meta data fields
						psInsertMeta.setInt(1, docId);
						psInsertMeta.setString(2, entry.getKey());
						psInsertMeta.setString(3, entry.getValue());
						psInsertMeta.setInt(4, maxValue);
						psInsertMeta.execute();
					//}
				}	
				psInsertMeta.close();				
			}
			con.commit();
		} catch (Exception e) {
			if (con != null) {
				con.rollback();
				con.close();
			}
			throw e;
			// throw new RuntimeException(e);
		} finally { 
			if (psSelectDocid != null) {
				psSelectDocid.close();
			}
			if (psSelectValueSet != null) {
				psSelectValueSet.close();
			}
			if (psInsertMeta != null) {
				psInsertMeta.close();
			}
			if (con != null) {
				con.close();
			}
		}

		DocumentServicesType resp = of.createDocumentServicesType();
		UserAuthResponseType userAuth = of.createUserAuthResponseType();
		resp.setUserAuthResponse(userAuth);

		TransactionResultType tr = of.createTransactionResultType();
		userAuth.setTransactionResult(tr);

		DocumentServiceResponseType docResponseType = of.createDocumentServiceResponseType();
		resp.setDocumentServiceResponse(docResponseType);

		TransactionResultType trType = of.createTransactionResultType();
		docResponseType.setTransactionResult(trType);

		docResponseType.setIncludeDocumentImage(false);
		docResponseType.setTransactionGUID(reqObj.getDocumentServiceRequest().getTransactionGUID());

		respDocs.getDocument().add(respDoc);
		docResponseType.setDocuments(respDocs);

		if (success) {
			tr.setResultCode(0);
			tr.setResultDescription("Success");

			trType.setResultCode(0);
			trType.setResultDescription("Success");
			log.info("Service Request Processing ...adding the meta data fields has been completed");

		} else {
			tr.setResultCode(1);
			tr.setResultDescription("Failed to add");

			trType.setResultCode(1);
			trType.setResultDescription("Failed to add meta fields");
			log.info("Service Request Processing ...adding the meta data fields has been failed");
		}
		return resp;
	}

	private DocumentServicesType removeMultivalueIndex(	DocumentServicesType reqObj) throws Exception {
		log.info("Service Request Processing ...removeMultivalueIndex for the document");
		Connection con = null;
		PreparedStatement  psSelectDocid = null ,psSelectValueSet = null ,psDeleteMeta = null;
		String docGUID = null;
		int docId = 0, maxValue = 0;
		boolean success = false;
		Map<String, String> metaDataFields = new HashMap<String, String>();
		
		DocumentsType respDocs = of.createDocumentsType();
		DocumentType respDoc = of.createDocumentType();
		MetaDataFields mdfts = of.createMetaDataFields();
		respDoc.setMetaDataFields(mdfts);
		
		try {
			con = getConn();
			psSelectDocid = con.prepareStatement(selectDocid);

			for (PackageType p : reqObj.getDocumentServiceRequest().getPackages().getPackage()) {

				for (DocumentType d : p.getDocuments().getDocument()) {
					
						for (MetaDataFieldType m : d.getMetaDataFields().getMetaDataField()) {
							
							if (m.getName().equalsIgnoreCase("DocGUID")){
									if(m.getValue() != null && !m.getValue().isEmpty() ) {
										docGUID = m.getValue();
										log.info("docGUID value from the request  is " + docGUID);
										
										// Add DocGUID to response
										MetaDataFieldType mdft = of.createMetaDataFieldType();
										mdft.setName(m.getName());
										mdft.setValue(m.getValue());
										respDoc.getMetaDataFields().getMetaDataField().add(mdft);
										//respDoc.setMetaDataFields(d.getMetaDataFields());
									} else{
										log.error("DocGUID value is empty in the request  ");
										throw new NullPointerException("DocGUID value is empty in the request ");
									}
							}else{
								metaDataFields.put(m.getName(), m.getValue());
							}
					}
				}
			}
			psSelectDocid.setString(1, docGUID);
			ResultSet docIdRs = psSelectDocid.executeQuery();

			if (docIdRs.next()) {
				docId = docIdRs.getInt(1);
				success = (docId > 0 ? true : false);
				log.info("doc_id value for the given docGUID value is  " + docId + "...success is " + success);
			} else {
				log.error("doc_id value retrieved from the table for the given docGUID value is null ");
			}
			docIdRs.close();
			psSelectDocid.close();
			
			if (success) {
				psSelectValueSet = con.prepareStatement(selectCountValueSet);
				psSelectValueSet.setInt(1, docId);
				ResultSet valueSetRs = psSelectValueSet.executeQuery();
				if (valueSetRs.next()) {
					maxValue = valueSetRs.getInt(1);
				}
				valueSetRs.close();
				psSelectValueSet.close();
				
				if(maxValue > 1){
					StringBuilder deleteQuery = new StringBuilder("DELETE FROM DOCMETADATA WHERE DOC_ID =  "+docId+" AND VALUE_SET IN ( SELECT data.VALUE_SET FROM DOCMETADATA data ");
					StringBuilder joinQuery = new StringBuilder();
					StringBuilder whereQuery = new StringBuilder();
					int rowCount = 0;
					whereQuery.append(" WHERE ");
					for(Map.Entry<String, String> entry: metaDataFields.entrySet()){
						//if(!entry.getKey().equalsIgnoreCase("DocGUID")){
							rowCount++;
							joinQuery.append(" JOIN DOCMETADATA data" + rowCount + " ON data.DOC_ID = data" + rowCount + ".DOC_ID AND data.DOC_ID = "+docId+" AND data.VALUE_SET = data"+rowCount+".VALUE_SET ");
							if (rowCount == 1)
								whereQuery.append("data" + rowCount + ".META_NAME = '" + entry.getKey() + "' AND data" + rowCount + ".META_VALUE = '" + entry.getValue()
										+ "'");
							else if (rowCount > 1)
								whereQuery.append(" AND data" + rowCount + ".META_NAME = '" + entry.getKey() + "' AND data" + rowCount + ".META_VALUE = '"
										+ entry.getValue() + "'");
						//}
					}
					deleteQuery.append(joinQuery);
					deleteQuery.append(whereQuery);
					deleteQuery.append(") ");
					log.debug("deleteQuery   " + deleteQuery);
					psDeleteMeta = con.prepareStatement(deleteQuery.toString());
					//psDeleteMeta.setInt(1, docId);
					boolean result = psDeleteMeta.execute();
					log.info("Delete metadata fields query result is "+result);
					psDeleteMeta.close();
					
				}else {
					success = false;
					log.info("Only one set of meta data fields exist .So removal of multi value index is not possible");
				}
			}
			con.commit();
		} catch (Exception e) {
			if (con != null) {
				con.rollback();
				con.close();
			}
			throw e;
			// throw new RuntimeException(e);
		} finally { 
			if (psSelectDocid != null) {
				psSelectDocid.close();
			}
			if (psSelectValueSet != null) {
				psSelectValueSet.close();
			}
			if (psDeleteMeta != null) {
				psDeleteMeta.close();
			}
			if (con != null) {
				con.close();
			}
		}

		DocumentServicesType resp = of.createDocumentServicesType();
		UserAuthResponseType userAuth = of.createUserAuthResponseType();
		resp.setUserAuthResponse(userAuth);

		TransactionResultType tr = of.createTransactionResultType();
		userAuth.setTransactionResult(tr);

		DocumentServiceResponseType docResponseType = of.createDocumentServiceResponseType();
		resp.setDocumentServiceResponse(docResponseType);

		TransactionResultType trType = of.createTransactionResultType();
		docResponseType.setTransactionResult(trType);

		docResponseType.setIncludeDocumentImage(false);
		docResponseType.setTransactionGUID(reqObj.getDocumentServiceRequest().getTransactionGUID());

		respDocs.getDocument().add(respDoc);
		docResponseType.setDocuments(respDocs);

		if (success) {
			tr.setResultCode(0);
			tr.setResultDescription("Success");

			trType.setResultCode(0);
			trType.setResultDescription("Success");
			log.info("Service Request Processing ...removing the meta data fields step has been completed");

		} else {
			tr.setResultCode(1);
			tr.setResultDescription("Failed to remove");

			trType.setResultCode(1);
			trType.setResultDescription("Failed to remove meta fields");
			log.info("Service Request Processing ...removing the meta data fields step has been failed");
		}
		return resp;
	}
	
	private DocumentServicesType updateDocIndex(	DocumentServicesType reqObj) throws Exception {

		log.info("Service Request Processing ...update MultivalueIndex for the document");
		Connection con = null;
		PreparedStatement  psSelectDocid = null ,psSelectValueSet = null ,psDeleteMeta = null, psInsertMeta = null;
		String docGUID = null;
		int docId = 0, valueSet = 0;
		boolean success = false;
		MultiMap metaDataFields = new MultiValueMap();
		
		DocumentsType respDocs = of.createDocumentsType();
		DocumentType respDoc = of.createDocumentType();
		MetaDataFields mdfts = of.createMetaDataFields();
		respDoc.setMetaDataFields(mdfts);
		
		try {
			con = getConn();
			psSelectDocid = con.prepareStatement(selectDocid);

			for (PackageType p : reqObj.getDocumentServiceRequest().getPackages().getPackage()) {

				for (DocumentType d : p.getDocuments().getDocument()) {
					
						for (MetaDataFieldType m : d.getMetaDataFields().getMetaDataField()) {
							
							if (m.getName().equalsIgnoreCase("DocGUID")){
									if(m.getValue() != null && !m.getValue().isEmpty() ) {
										docGUID = m.getValue();
										log.info("docGUID value from the request  is " + docGUID);
										
										// Add DocGUID to response
										MetaDataFieldType mdft = of.createMetaDataFieldType();
										mdft.setName(m.getName());
										mdft.setValue(m.getValue());
										respDoc.getMetaDataFields().getMetaDataField().add(mdft);
										//respDoc.setMetaDataFields(d.getMetaDataFields());
									} else{
										log.error("DocGUID value is empty in the request  ");
										throw new NullPointerException("DocGUID value is empty in the request ");
									}
							} else{
								metaDataFields.put(m.getName(), m.getValue());
							}
					}
				}
			}
			psSelectDocid.setString(1, docGUID);
			ResultSet docIdRs = psSelectDocid.executeQuery();

			if (docIdRs.next()) {
				docId = docIdRs.getInt(1);
				success = (docId > 0 ? true : false);
				log.info("doc_id value for the given docGUID value is  " + docId + "...success is " + success);
			} else {
				log.error("doc_id value retrieved from the table for the given docGUID value is null ");
			}
			docIdRs.close();
			psSelectDocid.close();
			
			if (success) {
				Set<String> keys = metaDataFields.keySet();
				ArrayList  valueList = new ArrayList();
				Map<String, String> oldValueMap = new HashMap<String, String>();
				Map<String, String> NewValueMap = new HashMap<String, String>();
				
				for(String key : keys){
					valueList = (ArrayList)metaDataFields.get(key);
					if(valueList.size() > 2)
						throw new RuntimeException("Meta data field contains extra values other than updatable value");
					oldValueMap.put((String)key, (String)valueList.get(0));
					NewValueMap.put((String)key, (String)valueList.get(1));
				}
				
				//finding the value set of the meta data fields that are need to be updated
				StringBuilder selectValueSetQuery = 	new StringBuilder("SELECT data.VALUE_SET FROM DOCMETADATA data ");
				StringBuilder joinQuery = new StringBuilder();
				StringBuilder whereQuery = new StringBuilder();
				int rowCount = 0;
				whereQuery.append(" WHERE ");
				for(Map.Entry<String, String> entry: oldValueMap.entrySet()){
						rowCount++;
						joinQuery.append(" JOIN DOCMETADATA data" + rowCount + " ON data.DOC_ID = data" + rowCount + ".DOC_ID AND data.DOC_ID = "+docId+" AND data.VALUE_SET = data"+rowCount+".VALUE_SET ");
						if (rowCount == 1)
							whereQuery.append("data" + rowCount + ".META_NAME = '" + entry.getKey() + "' AND data" + rowCount + ".META_VALUE = '" + entry.getValue()
									+ "'");
						else if (rowCount > 1)
							whereQuery.append(" AND data" + rowCount + ".META_NAME = '" + entry.getKey() + "' AND data" + rowCount + ".META_VALUE = '"
									+ entry.getValue() + "'");
				}
				selectValueSetQuery.append(joinQuery);
				selectValueSetQuery.append(whereQuery);
				log.debug("selectValueSet Query   " + selectValueSetQuery);
				
				psSelectValueSet = con.prepareStatement(selectValueSetQuery.toString());
				ResultSet valueSetRs = psSelectValueSet.executeQuery();
				if (valueSetRs.next()) {
					valueSet = valueSetRs.getInt(1);
				}
				valueSetRs.close();
				psSelectValueSet.close();
				
				//delete the value set of old values 
				StringBuilder deleteQuery = new StringBuilder("DELETE FROM DOCMETADATA WHERE DOC_ID =  "+docId+" AND VALUE_SET = "+valueSet);
				psDeleteMeta = con.prepareStatement(deleteQuery.toString());
				int result = psDeleteMeta.executeUpdate();
				log.info("Delete metadata fields query result is "+result);
				psDeleteMeta.close();
				
				//insert the new meta data fields for the existing document
				if(result > 0 ){
					psInsertMeta = con.prepareStatement(insertMetadata);
					for(Map.Entry<String, String> entry: NewValueMap.entrySet()){
						psInsertMeta.setInt(1, docId);
						psInsertMeta.setString(2, entry.getKey());
						psInsertMeta.setString(3, entry.getValue());
						psInsertMeta.setInt(4, valueSet);
						psInsertMeta.executeUpdate();
					}
					psInsertMeta.close();
				} else{
					success = false;
				}
			}
			con.commit();
		} catch (Exception e) {
			if (con != null) {
				con.rollback();
				con.close();
			}
			throw e;
			// throw new RuntimeException(e);  
		} finally {
			if (psSelectDocid != null) {
				psSelectDocid.close();
			}
			if (psSelectValueSet != null) {
				psSelectValueSet.close();
			}
			if (psDeleteMeta != null) {
				psDeleteMeta.close();
			}
			if (psInsertMeta != null) {
				psInsertMeta.close();
			}
			if (con != null) {
				con.close();
			}
		}

		DocumentServicesType resp = of.createDocumentServicesType();
		UserAuthResponseType userAuth = of.createUserAuthResponseType();
		resp.setUserAuthResponse(userAuth);

		TransactionResultType tr = of.createTransactionResultType();
		userAuth.setTransactionResult(tr);

		DocumentServiceResponseType docResponseType = of.createDocumentServiceResponseType();
		resp.setDocumentServiceResponse(docResponseType);

		TransactionResultType trType = of.createTransactionResultType();
		docResponseType.setTransactionResult(trType);

		docResponseType.setIncludeDocumentImage(false);
		docResponseType.setTransactionGUID(reqObj.getDocumentServiceRequest().getTransactionGUID());

		respDocs.getDocument().add(respDoc);
		docResponseType.setDocuments(respDocs);

		if (success) {
			tr.setResultCode(0);
			tr.setResultDescription("Success");

			trType.setResultCode(0);
			trType.setResultDescription("Success");
			log.info("Service Request Processing ...updating the meta data fields has been completed");

		} else {
			tr.setResultCode(1);
			tr.setResultDescription("Failed to update");

			trType.setResultCode(1);
			trType.setResultDescription("Failed to update");
			log.info("Service Request Processing ...updating the meta data fields has been failed");
		}
		return resp;
	
	}
	
	@Override
	public String delAll(boolean delAll) {
		Connection con = null;

		if (delAll) {

			PreparedStatement ps = null;

			try {
				con = getConn();
				ps = con.prepareStatement("DELETE FROM DOCMETADATA;");
				ps.executeUpdate();
				ps = con.prepareStatement("DELETE FROM DOCIDX;");
				ps.executeUpdate();
				con.commit();
				return "All deleted";
			} catch (Exception e) {
				/*
				 * if (con != null) { con.rollback(); con.close(); } throw e;
				 */
				e.printStackTrace();
			} finally {

				try {
					ps.close();
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else
			return "Nothing deleted";
		return "Nothing deleted";
	}

	private Connection getConn() throws ClassNotFoundException, SQLException, IOException {
		log.info("service processing the request ...retrieving the db connection details from property file ");
		Connection con = null;
		Properties props = new Properties();
		FileInputStream in = new FileInputStream("D:\\properties\\db.properties");
		props.load(in);
		in.close();

		String driver = props.getProperty("jdbc.driver");
		if (driver != null) {
			Class.forName(driver);
		}

		String url = props.getProperty("jdbc.url");
		String username = props.getProperty("jdbc.username");
		String password = props.getProperty("jdbc.password");

		con = DriverManager.getConnection(url, username, password);
		log.info("DB Details are....url is  "+url+"...username is "+username+"...password is "+password);
		/*
		 * Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); con =
		 * DriverManager.getConnection(
		 * "jdbc:sqlserver://10.211.70.180:1433;DatabaseName=KARUNA;", "SONORA",
		 * "case360@");
		 */
		con.setAutoCommit(false);
		log.info("service processing the request ...db connection object returned ");
		return con;
	}
	
}