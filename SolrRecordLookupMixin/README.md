Defines a few methods that can be used for looking up a value from a Solr index
It can either be the index into which the records are being inserted, or a different separate one.
However if it references the index that it currently writing records to, it will only be able to see already committed changes.

