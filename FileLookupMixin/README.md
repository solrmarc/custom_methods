Defines a two methods that can be used for looking up a value from an external datafile 

The expected format of this file is for each line to contain the key for the data, a tab character, and the 
value to return for that key.   Since the file is merely read into a hashMap as key, value pairs, the file 
doesn't _need_ to be sorted, but it is _recommended_ for it to be sorted.  Note that if multiple lines 
contain the same key, only the last one encountered will actually be used.

getFromFileBy001(Record record, String filename, String defaultValue)

    A custom extractor method.
    Open the file specified in the parameter "filename" search that file for 
    the control number from the current record, and return the corresponding 
    value from that file.  Or if that control number isn't found, return the 
    supplied defaultValue

    returns a String extracted from the provided file, associated with the 001 field of the current record.
    e.g.  value = custom(org.solrmarc.mixin.FileLookupMixin), getFromFileBy001(extra_data/new_recs_dates_sorted.txt, 20101201)
    

getFromFileByKey(Collection<String>keys, String filename, String defaultValue) 

    A custom mapping method.
    Open the file specified in the parameter "filename" search that file for 
    each of the values contained in the collection "keys", and return the corresponding 
    values for each of those keys from that file.  Or if no matches are found, return the 
    supplied defaultValue

    e.g.  value = 020a, custom_map(org.solrmarc.mixin.FileLookupMixin), getFromFileByKey(extra_data/new_recs_dates_sorted.txt, "")
  