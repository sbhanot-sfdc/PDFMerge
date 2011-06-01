public with sharing class PDFController {

    private String oppId;
    public Boolean isTest = false;
    
    private static final String GAE_PDF_MERGE_SVC_URL = 'http://cloudysan.appspot.com/pdfmerge';
    
    public PDFController (ApexPages.StandardController theController)
    {
        oppId = theController.getId();
    }
    
    public PageReference mergeAttachments()
    {
        String queryStr = '?ids=';
        
        List<Attachment> attachments = [select id from Attachment where parentId = :oppId 
                                        and contentType = 'application/pdf' 
                                        order by createdDate];
        if (attachments.size() == 0)
        {
            ApexPages.Message msgError = new ApexPages.Message(ApexPages.Severity.Error, 'No PDF attachments found');  
            ApexPages.addMessage(msgError);
            return null;  
        }
        
        for (Attachment a: attachments)
        {
            queryStr += a.Id + ',';
        }
        
        queryStr = queryStr.subString(0, queryStr.length() - 1 );
        queryStr += '&parentId=' + oppId + '&mergedDocName=QuoteWithTandC.pdf';
        
        try
        {
            HttpRequest req = new HttpRequest();
            req.setEndpoint(GAE_PDF_MERGE_SVC_URL + queryStr);
            req.setMethod('GET');
            req.setTimeout(60000);
            HttpResponse res;
            
            if (!isTest)
            {
               res  = new Http().send(req);
               if (res.getStatusCode() != 200)
               {
                   ApexPages.Message msgError = new ApexPages.Message(ApexPages.Severity.Error, 'Could not merge docs - '+ res.getBody());  
                   ApexPages.addMessage(msgError);
                   return null;
               }
            }
            else
            {
                res = new HttpResponse();
            }    
            
            
        }
        catch (Exception e)
        {
            ApexPages.Message msgError = new ApexPages.Message(ApexPages.Severity.Error, 'Could not merge docs - '+e.getMessage());  
            ApexPages.addMessage(msgError);
        }
        
        ApexPages.Message msgError = new ApexPages.Message(ApexPages.Severity.Info, 'Merge successful');  
        ApexPages.addMessage(msgError);

        return null;
    }
}