In the XSD for ActiveSync there are elements with the same name 
used in the Request and Response XML (for example FolderDelete
element is the same in FolderDeleteRequest.xsd and 
FolderDeleteResponse.xsd). The xjc command cannot create the
objects because it is the same element. 

The list of repeteated objects are the following:

1.  FolderDelete: FolderDeleteRequest.xsd and FolderDeleteResponse.xsd
2.  ResolveRecipients: ResolveRecipientsRequest.xsd and ResolveRecipientsResponse.xsd
3.  ValidateCert: ValidateCertRequest.xsd and ValidateCertResponse.xsd
4.  SendMail: SendMailRequest.xsd and SendMailResponse.xsd
5.  Ping: PingRequest.xsd and PingResponse.xsd
6.  FolderCreate: FolderCreateRequest.xsd and FolderCreateResponse.xsd
7.  MeetingResponse: MeetingResponseRequest.xsd and MeetingResponseResponse.xsd
8.  FolderSync: FolderSyncRequest.xsd and FolderSyncResponse.xsd
9.  Search: SearchRequest.xsd and SearchResponse.xsd
10. Sync: SyncRequest.xsd and SyncResponse.xsd
11. GetItemEstimate: GetItemEstimate.xsd and GetItemEstimateResponse.xsd
12. FolderUpdate: FolderUpdateRequest.xsd and FolderUpdateResponse.xsd
13. DeviceInformation: SettingsRequest.xsd and SettingsResponse.xsd
14. Settings: SettingsRequest.xsd and SettingsResponse.xsd
15. Provision: ProvisionRequest.xsd and ProvisionResponse.xsd
16. SmartReply: SmartReplyRequest.xsd and SmartReplyResponse.xsd
17. MoveItems: MoveItemsRequest.xsd and MoveItemsResponse.xsd
18. SmartForward: SmartForwardRequest.xsd and SmartForwardResponse.xsd
19. ItemOperations: ItemOperationsRequest.xsd and ItemOperationsResponse.xsd

I am going to change the XSD call then *Request and *Response but
then I will change the generated JAVA files to change the name again
to the short one (the same in both java classes).

In order to create the JAVA classes:

$ xjc -b jaxb.binding -d kk/ .

Then move from kk directory to the src and do some rename of the packages...
Besides in order to work the ObjectFactory should be constructed such 
ObjectFactoryRequest has only the Request objects and ObjectFactoryResponse
the Response objects. If this is not done the Request or Response object
can be created indistintibely.

