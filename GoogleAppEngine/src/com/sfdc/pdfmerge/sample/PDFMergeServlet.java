package com.sfdc.pdfmerge.sample;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class PDFMergeServlet extends HttpServlet {
	private String accessToken;
	private String instanceURL;
	
	private static final Logger log = Logger.getLogger(PDFMergeServlet.class.getName());
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
	    log.severe("PDF Merge Servlet Called");
		String sfdcInstance = req.getParameter("instanceURL");
		
		if (sfdcInstance != null)
		{	
			salesforceOAuthLogin(sfdcInstance);
		}
		else
		{
			salesforceOAuthLogin("https://login.salesforce.com");
		}
		
		if (accessToken == null)
			return;
		
		String ids = req.getParameter("ids");
		String parentId = req.getParameter("parentId");
		String mergedDocName = req.getParameter("mergedDocName");
		
		List<InputStream> pdfs = new ArrayList<InputStream>();
		ByteArrayOutputStream o = null;
		if (accessToken != null && ids != null && parentId != null)
		{
			Salesforce sfdc = new Salesforce(instanceURL, accessToken);
			for (String id : ids.split(","))
			{
				pdfs.add(sfdc.getAttachment(id));
			}

			o = new ByteArrayOutputStream();
			MergePDF.concatPDFs(pdfs, o, false);
			
			sfdc.saveAttachment(parentId, mergedDocName, o, "application/pdf");
		}
		
		try
		{
			if (o != null)
			{	
				o.flush();
				o.close();
			}
			
			for (InputStream pdf : pdfs)
			{
				pdf.close();
			}		
		}
		catch (Exception e){e.printStackTrace();}
		
		resp.setContentType("text/plain");
		resp.getWriter().println("Documents merged successfully");
	}
	
	private void salesforceOAuthLogin(String sfdcInstance)
	{
		log.severe("Starting OAuth");
		String consumerKey = getServletContext().getInitParameter("salesforce.consumerKey");
		String consumerSecret = getServletContext().getInitParameter("salesforce.consumerSecret");
		String username = getServletContext().getInitParameter("salesforce.username");
		String securityToken = getServletContext().getInitParameter("salesforce.securityToken");
		String password = getServletContext().getInitParameter("salesforce.password") + securityToken;
		String oauthURL = sfdcInstance + Salesforce.sfdcOAuthURI;

		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("grant_type", "password"));
		qparams.add(new BasicNameValuePair("client_id", consumerKey));
		qparams.add(new BasicNameValuePair("client_secret", consumerSecret));
		qparams.add(new BasicNameValuePair("username", username));
		qparams.add(new BasicNameValuePair("password", password));
		
		try
		{
			URL url = new URL(oauthURL);
			URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
			HTTPRequest httpRequest = new HTTPRequest(url, HTTPMethod.POST);
			
			httpRequest.setPayload(URLEncodedUtils.format(qparams, "UTF-8").getBytes());
			HTTPResponse response = urlFetchService.fetch(httpRequest);
		    log.severe("After OAuth Callout: HTTP Code:"+response.getResponseCode());
			String respBody = new String(response.getContent());

			if (response.getResponseCode() == 200)
	    	{
				JSONObject json = new JSONObject(respBody);
			    accessToken = json.getString("access_token");
			    instanceURL = json.getString("instance_url");
	    	}
	    } catch (Exception e) {
		    log.severe("OAuth Exception:"+ e.getMessage());
	    	e.printStackTrace();
	    }	
	}
}
