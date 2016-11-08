# Custom SolrMarc Index Methods
This repo contain standalone custom indexing methods for use in SolrMarc, as well as examples of how those methods can be used

One possibility for downloading source code from this repo for inclusion in your SolrMarc3 configuration would be to
change directory to where your index_java directory used by SolrMarc can be found, and then submit this command:

    svn export --force https://github.com/solrmarc/custom_methods/trunk/ISBNNormalizer/src index_java/src
    or
    svn export --force https://github.com/solrmarc/custom_methods/trunk/VideoIndoMixin/src index_java/src
    
