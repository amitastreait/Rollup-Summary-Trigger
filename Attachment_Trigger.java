/* 
    @Author : Amit Singh
    @Description : Trigger to count the #of attachment related to any account whenever any attachment is 
                   created, deleted or restored from Recyclebin
*/


trigger Attachment_Trigger on Attachment (After insert, After delete, After undelete) {
     
    /*
        Parent Record Map in out case we are testing so we will use Account Key as Parent recordId 
        And a Parent record with zero value in Rollupfield initally
    */
    Map<Id, Account> accountMaptoUpdate = new Map<Id, Account > ();
    
    // List of Attachment that are being inserted, deleted or undeleted
    List<Attachment> attachmentList = new List<Attachment>();
    
    attachmentList = Trigger.Isinsert || Trigger.isUndelete ? Trigger.New : Trigger.old;
    
    FOR(Attachment att : attachmentList){
        IF(att.ParentId!=null){
            
            /* Put the values in the Map if Map does not contains the value already */
            if(!accountMaptoUpdate.containsKey(att.ParentId))
                accountMaptoUpdate.put(att.ParentId , new Account(Id = att.ParentId, Count_Attachent__c =0));
        }
    }
    
    
    List<AggregateResult> aggResult = new List<AggregateResult>();
    
    /* 
        Make Aggregate Query to count the Attachment related to a Partucular Account as here we will count attachment 
        If your need is to sum the values then use SUM(Number_Field__c) to count the Sum with Parent RecordId field.
    */
    aggResult = [Select count(id) , ParentId From Attachment Where ParentId IN : accountMaptoUpdate.keySet() Group By ParentId];
    
    FOR(AggregateResult ar : aggResult){
        Id parentId = (ID)ar.get('ParentId');
        if(accountMaptoUpdate.containsKey(parentId)){
            Account acc = accountMaptoUpdate.get(parentId);
            Integer countAtt = (Integer)ar.get('expr0');
            acc.Count_Attachent__c = countAtt;
            accountMaptoUpdate.put(parentId , acc);
        }
    }
    
    /* Update the value in the Map. values() method returns the list of records */
    
    update accountMaptoUpdate.values();
    
}
