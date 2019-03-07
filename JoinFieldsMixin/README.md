Defines a few methods that can be used for combining the contents of two different fields in a MARC record into a single field to be sent to Solr.


The first method named  getSimpleJoinedFields takes two simple field/subfield specifications (e.g. "245a" or "610a") and using those it will look up the corresponding field/subfield values in the record and joins the strings together with the provided separator string between them.
If either of the sets of results of the lookup of the two specified field specifications contains more than one item, the sets of resutls will be joined pairwise in the output until one oor the other is empty, then all of the remaining entries for the other index spec will merely be copied to output field being produced. 

    getSimpleJoinedFields(String firstFieldSpec, String secondFieldSpec, String separator)
    
The second method named  getComplexJoinedFields  allows more complex index specifications to be used, including allowing conditional specifications, specifications modifiers, translation maps, and even allowing other custom methods to be invoked.

    getComplexJoinedFields(String firstFieldSpec, String secondFieldSpec, String separator)
