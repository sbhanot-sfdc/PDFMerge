package com.sfdc.pdfmerge.sample;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.repackaged.com.google.common.util.Base64;
import com.google.appengine.repackaged.org.json.JSONObject;


public class Salesforce {

	public static final String sfdcApiVersion = "21.0";
	public static final String sfdcOAuthURI = "/services/oauth2/token";

	private String instanceURL;
	private String accessToken;
	
	private static final Logger log = Logger.getLogger(PDFMergeServlet.class.getName());
	
	public Salesforce(String instanceURL, String accessToken)
	{
		this.instanceURL = instanceURL;
		this.accessToken = accessToken;
	}
	public InputStream getAttachment (String recordId)
	{
		log.severe("Getting Attachment:"+ recordId);		
		String restURI = instanceURL + "/services/data/v" + sfdcApiVersion + 
						 "/sobjects/attachment/" + recordId + "/body";
		try
		{
			URL url = new URL(restURI);
			URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
			HTTPRequest httpRequest = new HTTPRequest(url, HTTPMethod.GET, 
													  FetchOptions.Builder.withDeadline(10));
			
			httpRequest.setHeader(new HTTPHeader("Authorization", "OAuth "+ accessToken));
			
			HTTPResponse response = urlFetchService.fetch(httpRequest);
			log.severe("HTTP code:"+response.getResponseCode());

			InputStream i = null;
			if (response.getResponseCode() == 200)
	    	{
				i = new ByteArrayInputStream(response.getContent());
	    	}
			return i;
	    } catch (Exception e) {
		    log.severe("Getting Attachment Exception:"+ e.getMessage());
	    	e.printStackTrace();
	    }			
	    return null;
	}
	
	public void saveAttachment (String parentId, String name, ByteArrayOutputStream body, String contentType)
	{
		log.severe("Saving Attachment:"+ name);		
		String restURI = instanceURL + "/services/data/v" + sfdcApiVersion + "/sobjects/attachment/";
		try
		{
			URL url = new URL(restURI);
			URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
			HTTPRequest httpRequest = new HTTPRequest(url, HTTPMethod.POST, 
													  FetchOptions.Builder.withDeadline(10));
			
			httpRequest.setHeader(new HTTPHeader("Authorization", "OAuth "+ accessToken));
			httpRequest.setHeader(new HTTPHeader("Content-Type", "application/json"));

			JSONObject attachment = new JSONObject();

			String mergedPdf = new String(Base64.encode(body.toByteArray()));

			attachment.put("parentId", parentId);
			attachment.put("name", (name == null || name.equals(""))? "MergedDoc.pdf":name);
			attachment.put("ContentType", contentType);
			attachment.put("body", mergedPdf);

			httpRequest.setPayload(attachment.toString().getBytes());

			HTTPResponse response = urlFetchService.fetch(httpRequest);
			log.severe("HTTP code:"+response.getResponseCode());

	    } catch (Exception e) {
		    log.severe("Saving Attachment Exception:"+ e.getMessage());
	    	e.printStackTrace();
	    }
	}
}
