package com.armedia.acm.services.search.model;

/**
 * Created by armdev on 2/12/15.
 */
public interface SearchConstants
{
    String CATCH_ALL_QUERY = "catch_all:";

    String DATE_FACET_PRE_KEY = "facet.date.";
    String FACET_PRE_KEY = "facet.";

    String FACET_FILED = "facet.field=";
    String FACET_FILED_WITH_AND_AS_A_PREFIX = "&facet.field=";
    String FACET_QUERY = "facet.query=";
    String FACET_QUERY_WITH_AND_AS_A_PREFIX = "&facet.query=";

    String SOLR_FILTER_QUERY_ATTRIBUTE_NAME = "&fq=";
    String SOLR_FACET_NAME_CHANGE_COMMAND = "!key=";


    String TIME_PERIOD_KEY = "search.time.period";
    String TIME_PERIOD_DESCRIPTION = "desc";
    String TIME_PERIOD_VALUE = "value";

    String QUOTE_SPLITTER = "\"";
    String DOTS_SPLITTER = ":";
    String PIPE_SPLITTER = "\\|";
    String AND_SPLITTER = "&";

    String PROPERTY_NUMBER_FOUND = "numFound";
    String PROPERTY_RESPONSE = "response";

    String USER = "${user}";
    /**
     * The date format SOLR expects.  Any other date format causes SOLR to throw an exception.
     */
    String SOLR_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
}