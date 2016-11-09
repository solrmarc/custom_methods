Defines several methods that can be used for determining the "format" of an item 
Attempts to not only extract the format from the usual fields and the values defined for those fields
but also to perform some sanity checking and error flagging for formats that seem to be errors, such as
a video tape that is in the DVD format.

getContentTypesAndMediaTypesMapped(Record record, String mapFileName)

    A custom extractor method.
    Determines the format of the given MARC record in a verbose "internal form" and then use the supplied map file
    to translate those value to usable strings.

    returns the format(s) for a given record.
    e.g.  value = custom(org.solrmarc.mixin.GetFormatMixin), getContentTypesAndMediaTypesMapped("getformat_mixin_map.properties")
    

getContentTypesAndMediaTypes(Record record) 

    A custom extractor method.
    Determines the format of the given MARC record in a verbose "internal form".

    returns the format(s) for a given record, in 
    e.g.  value = custom(org.solrmarc.mixin.GetFormatMixin), getContentTypesAndMediaTypes, getformat_mixin_map.properties


getPrimaryContentType(Record record)
    A custom extractor method.
    Determines the broad format of the given MARC record in a verbose "internal form".

    returns a single, broad format(s) for a given record, in a verbose "internal form". 
    e.g.  value = custom(org.solrmarc.mixin.GetFormatMixin), getPrimaryContentType, getformat_mixin_map.properties
    

getPrimaryContentTypePlusOnline(Record record)
    A custom extractor method.
    Determines the broad format of the given MARC record in a verbose "internal form".

    returns a single, broad format for a given record, in a verbose "internal form", plus FormOfItem.Online for electronic items. 
    e.g.  value = custom(org.solrmarc.mixin.GetFormatMixin), getPrimaryContentTypePlusOnline, getformat_mixin_map.properties

getContentTypes(Record record)
    A custom extractor method.
    Determines the all applicable broad formats for the given MARC record in a verbose "internal form".

    returns a one or more, broad formats for a given record, in a verbose "internal form". 
    e.g.  value = custom(org.solrmarc.mixin.GetFormatMixin), getContentTypes, getformat_mixin_map.properties

getMediaTypes(Record record)
    A custom extractor method.
    Determines the all applicable low-level formats for the given MARC record in a verbose "internal form".
    Such as  Braille, or VHS.  Usually you would use the method getContentTypesAndMediaTypes above to get the
    low-level format at the same time as the broad format, but with this method being separate you could construct 
    a two-level hierarchy of facets.

    returns a one or more, low-level formats for a given record, in a verbose "internal form". 
    e.g.  value = custom(org.solrmarc.mixin.GetFormatMixin), getMediaTypes, getformat_mixin_map.properties
    
Note this custom method pretty much requires a translation map to provide useful results.  In this directory that is 
one translation map named getformat_mixin_map.properties which is used at the University of Virginia.  Note how for
some of the MediaType entries (such as MediaType.VHS) the map value is "Video|VHS" this will be expanded into two 
format value for that case.   

The other translation map named getformat_mixin_unmap_map.properties could be used to store or display what criteria 
were used to determine that particular "internal form" value.   
   