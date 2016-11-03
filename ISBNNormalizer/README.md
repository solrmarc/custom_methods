Defines a single method that can be used for normalizing an ISBN string.

ISBN can be either 10 digits or 13-digits.  In both cases the final digit is a check digit that can
help to determine whether an ISBN is valid or not.   A 13-digit ISBN can be formed from a 10-digit one
by adding the characters "978" to the front of the number, and then computing a new check digit.  
Conversely a 10-digit ISBN can be formed from a 13-digit one (if the 13-digit one starts with "978") 
by discarding those three initial digits, and then computing a new check digit.
 

filterISBN(Collection<String>ISBNs, String whatToReturn)

    A custom mapping method.
    Receives a collection of ISBN fields that may have additional characters at the front or at the end
    of the ISBN and which may also have one or more dashes (-) within the number.  This method strips out 
    the dashes and the characters at the start or end of the number and then normalizes the ISBN based on the
    parameter "whatToReturn".   The valid values for that parameter are "10", "13", or "both".  If it finds a 
    13-digit ISBN and you request a 10-digit one (or vice versa) it will transform the number that is found, 
    and recompute the check digit.   If you request "both" then it will return the ISBN as-found and also 
    return the other valid form.

    returns a String extracted from the provided file, associated with the 001 field of the current record.
    e.g.  value = 020a, custom_map(org.solrmarc.mixin.ISBNNormalizer), filterISBN(10), unique
    or    value = 020a, custom_map(org.solrmarc.mixin.ISBNNormalizer), filterISBN(13), unique
    or    value = 020a, custom_map(org.solrmarc.mixin.ISBNNormalizer), filterISBN(both), unique
    
