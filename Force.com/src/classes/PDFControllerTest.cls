@isTest
private class PDFControllerTest
{
    static testMethod void testPDFMerge() {
        Opportunity testOpp = setupOppTestData();
        Test.startTest();
        
        PageReference mergePDFPage = Page.MergePDFs;
        Test.setCurrentPage(mergePDFPage);
                    
        // Set the model action
        ApexPages.currentPage().getParameters().put('id', testOpp.Id);

        // Create a standard controller
        ApexPages.Standardcontroller controller = new ApexPages.Standardcontroller(testOpp); 

        //create instance of pages controller
        PDFController mergeController = new PDFController (controller);
        mergeController.isTest = true;
        
        //First test error condition - no PDF attachments
        mergeController.mergeAttachments();
        
        ApexPages.Message msg = ApexPages.getMessages().get(0);
        System.assertEquals(msg.getDetail(),'No PDF attachments found');
        
        //Now test success condition
        setupPDFAttachmentTestData(testOpp.Id);
        mergeController.mergeAttachments();
        
        msg = ApexPages.getMessages().get(1);
        System.assertEquals(msg.getDetail(),'Merge successful');
        
        Test.stopTest();
    }
    
    private static Opportunity setupOppTestData()
    {
        Account a = new Account (Name = 'My Test Account');
        insert a;
        Opportunity o = new Opportunity (AccountId = a.Id,
                                         Name = 'testPDFMerge Opportunity',
                                         StageName = 'Prospecting',
                                         CloseDate = System.today());
        insert o;                                 
        return o;                               
    }
    
    private static void setupPDFAttachmentTestData(Id parentOppId)
    {
        Attachment a1 = new Attachment (ParentId = parentOppId,
                                       name = 'Test attachment 1',
                                       contentType = 'application/pdf',
                                       Body = Blob.valueOf('abc'));
        insert a1;                                 

        Attachment a2 = new Attachment (ParentId = parentOppId,
                                       name = 'Test attachment 2',
                                       contentType = 'application/pdf',
                                       Body = Blob.valueOf('xyz'));
        insert a2;                                 
    }
}