Defines several methods that extract useful data from MARC records for Video items.

getVideoDirector

     Attempt to heuristically determine the Director of a video item based on looking in 
     the 245c, subfield, the 508a subfield and 700 fields.   Applies many, many patterns looking to 
     extract the "director" of a video, while also avoiding instances "director of photography"
     The test directory contains a files of test cases for which this routine will work.
     The test data consists of the correct director name (or names) followed by the string extracted 
     from an actual MARC record data field.   Note the test data includes cases where the word director
     is in another language or is missing entirely.
     
     returns zero or more director's names for the video, with duplicates removed.
     e.g.  directors = custom(org.solrmarc.mixin.VideoInfoMixin), getVideoDirector
     
     
Note: the rest of these custom methods can be replaced with a new-style "simple" index specification, 
possibly with a complex translation map.

getVideoRuntime

    Extract the runtime of a video item from characters 18-20 of the 008 field
    
    returns String containing runtime of video (if present)
    e.g.  runtime = custom(org.solrmarc.mixin.VideoInfoMixin), getVideoRuntime
    
getVideoTargetAudience

    Extract the targetAudience of a video item from the 521a subfield (if present)
    
    returns the exact value in that field (or in the fierst occurrance if more than one present)
    e.g.   target_audience = custom(org.solrmarc.mixin.VideoInfoMixin), getTargetAudience

getVideoRating

    Attempt to heuristically determine the "Rating" of a video item from the 521a subfield (if present)
    scans the value looking for certain patterns and tries to extract a simplified value from that value.
    However since there are many different rating schemes used around the world, and the textual description
    is not necessarily standardized, this routine merely makes a best effort.
    
    returns a string like "Rated: PG-13" or "Rated: TV-MA"
    e.g.  rating = custom(org.solrmarc.mixin.VideoInfoMixin), getVideoRating

 getOriginalReleaseDate
 
     Attempt to heuristically determine the original release of a video item based on 500a, subfield
     Makes a best effort to extract the original release date of a video based on the textual
     description in the 500a fields.
     
     returns the 4-digit year extracted as the original release date of a video
     e.g.  release_date = custom(org.solrmarc.mixin.VideoInfoMixin), getOriginalReleaseDate
     
 getVideoGenre
 
     Attempt to heuristically determine the "genre" of a video item based on 650 and 655 fields
     Uses a list of "keywords" to form a guess at the "Genre" of a video.  
     
     returns zero or more genre strings, such as "Comedy", "Action/Adventure", "Horror", "Romance".
     e.g.  genre = custom(org.solrmarc.mixin.VideoInfoMixin), getOriginalReleaseDate
