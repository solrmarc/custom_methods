Defines a few methods that can be used for looking up a value from an external datafile 

For the first several methods the expected format of the file is for each line to contain the key 
for the data, a separator character, and the value to return for that key.   
Since the file is merely read into a hashMap as key, value pairs, the file 
doesn't _need_ to be sorted, but it is helpful for the maintainer of the file if it is sorted.  
Note that if multiple lines contain the same key, only the last one encountered will actually be used.

getFromFileBy001(Record record, String filename, String separatorPattern, String defaultValue)

    A custom extractor method.
    Open the file specified in the parameter "filename" search that file for 
    the control number from the current record, and return the corresponding 
    value from that file.  Or if that control number isn't found, return the 
    supplied defaultValue

    returns a String extracted from the provided file, associated with the 001 field of the current record.
         
          value = custom(org.solrmarc.mixin.FileLookupMixin), 
                  getFromFileBy001(extra_data/new_recs_dates_sorted.txt, "\t", 20101201)
    
getFromFileBy001(Record record, String filename, String defaultValue)

    A custom extractor method.
    Similar to the above but with the assumption that a tab character will separate the key from the value

    returns a String extracted from the provided file, associated with the 001 field of the current record.
    
       value = custom(org.solrmarc.mixin.FileLookupMixin), 
                  getFromFileBy001(extra_data/new_recs_dates_sorted.txt, 20101201)
    


mapFromFileByKey(Collection<String>keys, String filename, String separatorPattern, String defaultValue) 

    A custom mapping method.
    Open the file specified in the parameter "filename" search that file for 
    each of the values contained in the collection "keys", and return the corresponding 
    values for each of those keys from that file.  Or if no matches are found, return the 
    supplied defaultValue

         value = 020a, custom_map(org.solrmarc.mixin.FileLookupMixin, 
                        mapFromFileByKey(extra_data/new_recs_dates_sorted.txt, "\t", ""))
         OR
         
         value = 020a, mapFromFileByKey(extra_data/new_recs_dates_sorted.txt, "\t", "")
  
  
The next set of methods can be applied after one of the above in the instance where the data returned 
for a key actually consists of multiple values like the following, which lists the date an item was recieved
by the library, and the internal funding code(s) that were used to pay for the item

    u4851188|20111219|ART-M
    u4853449|20131101|MUSI-M:UL-POSTAGE
    u4855673|20140325|SAS-YY00196
    u4856068|20131014|PARRISH-ER00600
    u4874816|20121024|CL-RESERVES

mapLookupSelect(Collection<String> values, String sepPattern, String select)
    
    A custom mapping method.
    Select only a part of the returned data value. The parameter "select" is interpreted as a zero-based
    integer array index to select the desired piece of the passed in data value.
    
    If it were used after the extractor getFromFileBy001, for record number u4851188 that extractor would 
    return "20111219|ART-M", if a particular field only needed the first part of that (the date received) 
    the following mapping method could select it.
   
        date_received = custom, getFromFileBy001("extra_data/booklists_all_20161128.txt", "[|]", null), 
                               custom_map(mapLookupSelect("[|]",0))
        
        OR
        
        date_received = getFromFileBy001("extra_data/booklists_all_20161128.txt", "[|]", null), 
                               mapLookupSelect("[|]",0)


mapLookupSplit(Collection<String> values, String sepPattern)
    
    A custom mapping method.
    Divide the returned data value into separate pieces and return each of them as a value for the field. 
    
    If it were used after the extractor getFromFileBy001, for record number u4853449 that extractor would 
    return "20131101|MUSI-M:UL-POSTAGE", if the above mapping method was then used to select the fund 
    code portion MUSI-M:UL-POSTAGE, this method could split that string on the ":" character and add 
    multiple values to the index, by doing the following:particular field only needed the first part 
    of that (the date received) this mapping method could select it.
   
        date_received = custom, getFromFileBy001("extra_data/booklists_all_20161128.txt", "[|]", null), 
                               custom_map(mapLookupSelect("[|]",1)), 
                               custom_map(mapLookupSplit(":"))
        
        OR
        
        date_received = getFromFileBy001("extra_data/booklists_all_20161128.txt", "[|]", null), 
                               mapLookupSelect("[|]",1), mapLookupSplit(":")
        


For the last two methods the expected format of the file is for each line of the external data file 
to contain only a key.  These methods will merely determine whether the value in question exists 
in the file of not.  Since the file is merely read into a hashMap as key, value pairs, the file 
doesn't _need_ to be sorted, but it is helpful for the maintainer of the file if it is sorted.  
Note that if multiple lines contain the same key, only the last one encountered will actually be used.

getFromFileKeyExists(Record record, String filename, String exists, String notExists)
    
    A custom extractor method.
    Open the file specified in the parameter "filename" search that file for 
    the control number from the current record, and if it exists, return the value supplied in the parameter 
    exists.  If is doesn't exist in the file, return the value for the parameter notExists.  If either 
    parameter (exists or notExists) is set to the empty string "", the method will return no value for the 
    extractor.   (If they are both set to "", you will merely waste time)

          shadowed_facet = custom(org.solrmarc.mixin.FileLookupMixin), 
                  getFromFileKeyExists(extra_data/shadowedids.txt, "HIDDEN", "VISIBLE")
          
          OR
       
          shadowed_facet = getFromFileKeyExists(extra_data/AllShadowedIds.txt, "VISIBLE", "HIDDEN")
 
                  
mapFromFileKeyExists(Collection<String> keys, String filename, String exists, String notExists)

    A custom mapping method.
    Open the file specified in the parameter "filename" search that file for 
    each of the values contained in the collection "keys", and if any of them occur in that
    file return the value supplied in the parameter exists.  If is doesn't exist in the file, 
    return the value for the parameter notExists.  If either parameter (exists or notExists) 
    is set to the empty string "", the method will return no value for the extractor.   
    (If they are both set to "", you will merely waste time)
   
        No example yet
