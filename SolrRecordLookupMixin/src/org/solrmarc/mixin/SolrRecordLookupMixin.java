package org.solrmarc.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.solrmarc.index.SolrIndexerMixin;
import org.solrmarc.solr.SolrCoreLoader;
import org.solrmarc.solr.SolrProxy;
import org.solrmarc.solr.SolrRuntimeException;
import org.solrmarc.tools.SolrMarcIndexerException;

/**
 * This class facilitates connecting to a Solr core to do a search based information 
 * in a given record.  It first extracts som data field from a MARC record that is passed in,
 * then builds a query to search the Solr index.  It then takes the results of that query 
 * and returns the data to be included in the Solr add document that is being built 
 * for the current record.
 *  
 * @author rh9ec
 *
 */
public class SolrRecordLookupMixin extends SolrIndexerMixin
{
    public final static int MAX_DOCS_TO_RETRIEVE = 100;
    /**
     *  serverMap - caches connections to one or more Solr indexes, using a separate 
     *  CommonsHttpSolrServer for each separate Solr index that is used.
     */
    Map<String, SolrProxy> serverMap = null;
    /**
     *   resultMap - caches the results returned from a Solr index for a given query
     *   Because multiple SolrMarc index specifications could exist for a given record
     *   this cache of the results returned by the first index specification is used by
     *   all subsequent index specifications for that record. 
     */
    Map<String, SolrDocumentList> resultMap = null;
    
    public SolrRecordLookupMixin()
    {
        serverMap = new LinkedHashMap<String, SolrProxy>();
    }
    
    /**
     * perRecordInit - Called once per record before any index specifications are processed.
     * This method merely makes sure the resultMap of records returned for searches based on a given record
     * being indexed are deleted and reset to empty when the next record is being indexed.
     * 
     * @param record - The MARC record being indexed.
     * 
     * @see org.solrmarc.index.SolrIndexerMixin#perRecordInit(org.marc4j.marc.Record)
     */
    public void perRecordInit(Record record)
    {
        resultMap = new LinkedHashMap<String, SolrDocumentList>();
    }

    private String buildQueryString(Record record, String fieldspec, String prefixStr, String selectionPattern, String cleanUpPattern, String mustMatch)
    {
        List<VariableField> fieldsThatMatch = record.getVariableFields(fieldspec.substring(0,3));
        StringBuilder sb = new StringBuilder();
        String [] selectionPatternParts = new String[0];
        if (selectionPattern != null && selectionPattern.length() != 0 && selectionPattern.contains("=>"))
        {
            selectionPatternParts = selectionPattern.split("=>", 2);
        }  
        boolean first = true;
        for (VariableField vf : fieldsThatMatch)
        {
            if (vf instanceof DataField)
            {
                DataField df = (DataField)vf;
                List<Subfield> sfl = ((DataField)vf).getSubfields(fieldspec.substring(3));
                for (Subfield sf : sfl)
                {
                    String dataVal = sf.getData();
                    if (selectionPatternParts.length == 2)
                    {
                        dataVal = dataVal.replaceFirst(selectionPatternParts[0], selectionPatternParts[1]);
                    }
                    if (cleanUpPattern != null && cleanUpPattern.length() != 0)
                    {
                        dataVal = dataVal.replaceAll(cleanUpPattern, "");
                    }
                    if (dataVal != null  && dataVal.length() != 0 && (mustMatch == null || mustMatch.length() == 0 || dataVal.matches(mustMatch)))
                    {
                        if (first)  sb.append("(");
                        else        sb.append(" OR ");
                        first = false;
                        sb.append(dataVal);
                    }
                } 
            }
            else if (vf instanceof ControlField)
            {
                ControlField cf = (ControlField)vf;
                String dataVal = cf.getData();
                if (selectionPatternParts.length == 2)
                {
                    dataVal = dataVal.replaceFirst(selectionPatternParts[0], selectionPatternParts[1]);
                }
                if (cleanUpPattern != null && cleanUpPattern.length() != 0)
                {
                    dataVal = dataVal.replaceAll(cleanUpPattern, "");
                }
                if (dataVal != null  && dataVal.length() != 0 && (mustMatch == null || mustMatch.length() == 0 || dataVal.matches(mustMatch)))
                {
                    if (first)  sb.append("(");
                    else        sb.append(" OR ");
                    first = false;
                    sb.append(dataVal);
                }
            }
        }
        if (!first) sb.append(")");
        
        //if the record that was passed in contained no fields matching the fieldspec return null as the query string
        if (sb.length() == 0) return(null);
        
        String parameterizedQuery = prefixStr + ":" + sb.toString();
        return(parameterizedQuery);
    }
    
    
    /**
     * getServerForURL - returns the SolrServer object to use to communicate
     * to a Solr server at the provided URL.  Caches the SolrServer object in a map 
     * using the url as the key, so that only one SolrServer objects is created for a 
     * given URL.
     * 
     * @param solrUrl - The URL of a particular Solr server, including the core to access
     *                  e.g.  http://localhost:8080/solr/summ
     * @return SolrServer object
     */
    private SolrProxy getServerForURL(String solrUrl)
    {
        SolrProxy server = null;
        if (serverMap.containsKey(solrUrl))
        {
            server = serverMap.get(solrUrl);
        }
        else
        {
//            try
//            {
                server = SolrCoreLoader.loadRemoteSolrServer(solrUrl, null, true);
                serverMap.put(solrUrl, server);
//            }
//            catch (MalformedURLException e)
//            {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
        }      
        return(server);
    }
    

    /**
     * getSolrDocumentsForQuery - Retrieve the set of documents from the separate solr index 
     * that match the provided query.  Then cache the results so that subsequent index specifications
     * for the same MARC record, that query the same server can merely use the cached list of documents.
     * 
     * Note: the static value MAX_DOCS_TO_RETRIEVE controls how many matching documents are returned 
     * from the solr server.
     * 
     * @param server - The SolrServer to send query to.
     * @param responseKey - The key to retrieve the search result, or the key to use to cache it.
     * @param queryStr - The query string to send to Solr.
     * @return  A SolrDocumentList matching the provided query.
     */
    private SolrDocumentList getSolrDocumentsForQuery(SolrProxy server, String queryStr)
    {
        SolrDocumentList sdl = null;
        // if query already performed, return those results
        if (resultMap.containsKey(queryStr))
        {
            sdl = resultMap.get(queryStr);
        }
        else
        {
            SolrQuery params = new SolrQuery();
            params.setQuery(queryStr);
            params.setRows(MAX_DOCS_TO_RETRIEVE);
            QueryResponse resp = null;
            try
            {
                resp = server.query(params);
                sdl = resp.getResults();
                resultMap.put(queryStr, sdl);
            }
            catch (SolrRuntimeException e)
            {
                throw new SolrMarcIndexerException(SolrMarcIndexerException.EXIT, e.getMessage());
            }
        }
        return(sdl);

    }
    
    /**
     * fillResultSetFromSolrDocs - Traverse over the SolrDocumentList returned for the current query
     * and select fields that match the parameter  fieldToFetch.
     * 
     * @param sdl - The list of matching SolrDocuments
     * @param fieldToFetch - the field to look for within the list of matching SolrDocuments
     * @param ensureUnique - boolean specify whether duplicate values should be omitted 
     * @return
     */
    private List<String> fillResultSetFromSolrDocs(SolrDocumentList sdl, String fieldToFetch, boolean ensureUnique)
    {
        List<String> result = new ArrayList<String>();
        if (sdl != null)
        {
            long numFound = sdl.getNumFound();
            for (int rec = 0; rec < numFound && rec < MAX_DOCS_TO_RETRIEVE; rec++)
            {
                SolrDocument doc = sdl.get(rec);
                Collection<Object> vals = doc.getFieldValues(fieldToFetch);
                if (vals != null)
                {
                    for (Object val : vals)
                    {
                        String sVal = val.toString();
                        if (!ensureUnique || ! result.contains(sVal))
                        {
                            result.add(sVal);
                        }
                    }
                }
            }
        }
        return(result);
    }

    
    /**
     * getExtraSolrDataByQuery - An actual custom indexing function call-able by SolrMarc.
     * The index specification provides the URL of the Solr server (including the core name)
     * that contains the desired data, and the name of the field within that indexed data to return 
     * for the current index specification.  It also provides the fieldspec to use to extract data from 
     * the current in constructing the query against that solr index, and the field prefix string to use 
     * in building the query.
     * 
     * For example the index specification:  
     *     
     *     getExtraSolrDataByQuery(http://myserver.company.com:8080/solr/core, "020az", isbn_text, "([- 0-9]*[0-9Xx]).*=>$1", "[- ]", "([0-9]{9}[0-9Xx])|([0-9]{13})", summary_display)
     *     
     * will build a query consisting of the contents of the 020a subfields and the 020z subfields of the current record (which is usually an ISBN) 
     * it will then clean up the data to use numeric-only values that look like an ISBN to build the query to send to the solr server at "http://myserver.company.com:8080/solr/core" and return 
     * all occurrences of the "summary_display" field from the returned record(s).
     * 
     * @param record - The MARC record being indexed.
     * @param solrUrl - The URL of the Solr server containing the data to be looked up.
     * @param fieldspec - the field (or field/subfield) to extract from the current record to build the query against the solr index 
     * @param prefixStr - the query prefix to use with the above extracted data to  build the query against the solr index
     * @param selectionPattern - applied to fields/subfields extracted from the current record in building the query
     *                           the provided pattern is split into two strings at the first occurrence of "=>" the first 
     *                           part is used as the matching string for a replace first operation and the second part is the replacement value
     *                           dataVal = dataVal.replaceFirst(selectionPattern.before("=>"), selectionPattern.after("=>"))
     * @param cleanupPattern - applied to fields/subfields extracted from the current record in building the query after applying the selectionPattern
     *                           provides a set of characters that should be deleted from the data before using it in a query string
     * @param mustMatch - applied to fields/subfields extracted from the current record in building the query
     *                           consists of a pattern that must match the extracted data value for that data value to be used in the query string 
     * @param fieldToFetch - the Field in the returned solr response from which to extract one or more items of data to return.
     * @return
     */
    public List<String> getExtraSolrDataByQuery(final Record record, String solrUrl, String fieldspec, String prefixStr, String selectionPattern, String cleanUpPattern, String mustMatch, String fieldToFetch)
    {
        // Get the possibly cached SolrServer object to use
        SolrProxy server = getServerForURL(solrUrl);
        
        String queryStr = buildQueryString(record, fieldspec, prefixStr, selectionPattern, cleanUpPattern, mustMatch);
        if (queryStr == null) return (new ArrayList<String>());
        
        SolrDocumentList sdl = getSolrDocumentsForQuery(server, queryStr);
        
        List<String> result = fillResultSetFromSolrDocs(sdl, fieldToFetch, true);

        return(result);
    }
    
    /**
     * getExtraSolrDataByID - An actual custom indexing function call-able by SolrMarc.
     * The index specification provides the URL of the Solr server (including the core name)
     * that contains the desired data, and the name of the field within that indexed data to return 
     * for the current index specification.  It also provides the fieldspec to use to extract data from 
     * the current in constructing the query against that solr index, and the field prefix string to use 
     * in building the query.
     * 
     * For example the index specification:  
     *     getExtraSolrDataByID(http://myserver.company.com:8080/solr/core, "001", "id", summary_display)
     * will build a query consisting of the contents of the 001 field of the current record (which is the unique identifier) 
     * it will then send that query to the solr server at "http://myserver.company.com:8080/solr/core" and return 
     * all occurrences of the "summary_display" field from the returned record(s).    
     *  
     * @param record - The MARC record being indexed.
     * @param solrUrl - The URL of the Solr server containing the data to be looked up.
     * @param fieldspec - the field (or field/subfield) to extract from the current record to build the query against the solr index 
     * @param prefixStr - the query prefix to use with the above extracted data to  build the query against the solr index
     * @param fieldToFetch - the Field in the returned solr response from which to extract one or more items of data to return.
     * @return
     */
    public List<String> getExtraSolrDataByID(final Record record, String solrUrl, String fieldspec, String prefixStr, String fieldToFetch)
    {
        return getExtraSolrDataByQuery(record, solrUrl, fieldspec, prefixStr, null, null, null, fieldToFetch);
    } 
    
    /**
     * getExtraSolrDataByID - An actual custom indexing function call-able by SolrMarc.
     * The index specification provides the URL of the Solr server (including the core name)
     * that contains the desired data, and the name of the field within that indexed data to return 
     * for the current index specification.  It also provides the fieldspec to use to extract data from 
     * the current in constructing the query against that solr index, and the field prefix string to use 
     * in building the query.
     * 
     * For example the index specification:  
     *     getExtraSolrDataByID(http://myserver.company.com:8080/solr/core, summary_display)
     * will build a query consisting of the contents of the 001 field of the current record (which is the unique identifier) 
     * it will then send that query to the solr server at "http://myserver.company.com:8080/solr/core" and return 
     * all occurrences of the "summary_display" field from the returned record(s).
     * This method is similar to the similar one above but it makes the (usually valid) assumption that the record id is in the 001 field, and that
     * is stored in the solr index in a field named "id". 
     *  
     * @param record - The MARC record being indexed.
     * @param solrUrl - The URL of the Solr server containing the data to be looked up.
     * @param fieldToFetch - the Field in the returned solr response from which to extract one or more items of data to return.
     * @return
     */
    public List<String> getExtraSolrDataByID(final Record record, String solrUrl, String fieldToFetch)
    {
        return getExtraSolrDataByID(record, solrUrl, "001", "id", fieldToFetch);
    } 
    
    /**
     * skipIfDataExists - An actual custom indexing function call-able by SolrMarc.
     * This method calls getExtraSolrDataByID and if that method would NOT return any data, this method whould similarly not return any data.
     * However if that method WOULD return data, then this method will throw the exception that signals that the entire record ought to be skipped.
     * 
     * For example the index specification:  
     *     skipIfDataExists(http://myserver.company.com:8080/solr/core, "001", "id", "id")
     * will query the solr server at "http://myserver.company.com:8080/solr/core" and if a record exists there with that matches the id in the current record
     * then the current record will be skipped.
     * 
     * @param record - The MARC record being indexed.
     * @param solrUrl - The URL of the Solr server containing the data to be looked up.
     * @param fieldspec - the field (or field/subfield) to extract from the current record to build the query against the solr index 
     * @param prefixStr - the query prefix to use with the above extracted data to  build the query against the solr index
     * @param fieldToFetch - the field to look in to determine whether to skip this record
     * @return
     */
    public List<String> skipIfDataExists(final Record record, String solrUrl, String fieldspec, String prefixStr, String fieldToFetch)
    {     
        List<String> result = getExtraSolrDataByID(record, solrUrl, fieldspec, prefixStr, fieldToFetch);
        
        if (result.isEmpty()) 
        {
            return(result);
        }

        throw new SolrMarcIndexerException(SolrMarcIndexerException.IGNORE);
    }
    
    /**
     * skipIfDataExists - An actual custom indexing function call-able by SolrMarc.
     * This method calls getExtraSolrDataByID and if that method would NOT return any data, this method whould similarly not return any data.
     * However if that method WOULD return data, then this method will throw the exception that signals that the entire record ought to be skipped.
     * 
     * For example the index specification:  
     *     skipIfDataExists(http://myserver.company.com:8080/solr/core, "001", "id", "id")
     * will query the solr server at "http://myserver.company.com:8080/solr/core" and if a record exists there with that matches the id in the current record
     * then the current record will be skipped.
     * 
     * @param record - The MARC record being indexed.
     * @param solrUrl - The URL of the Solr server containing the data to be looked up.
     * @param fieldToFetch - the field to look in to determine whether to skip this record
     * @return
     */
    public List<String> skipIfDataExists(final Record record, String solrUrl, String fieldToFetch)
    {     
        List<String> result = getExtraSolrDataByID(record, solrUrl, "001", "id", fieldToFetch);
        
        if (result.isEmpty()) 
        {
            return(result);
        }

        throw new SolrMarcIndexerException(SolrMarcIndexerException.IGNORE);
    }    
}
