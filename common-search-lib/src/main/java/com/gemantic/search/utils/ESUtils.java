package com.gemantic.search.utils;

import com.gemantic.search.constant.CommonFields;
import com.gemantic.search.model.ElasticIndex;
import com.gemantic.search.support.agg.TermAggEntry;
import com.gemantic.search.support.agg.TermAggResult;
import com.gemantic.search.support.elastic.IndexInfo;
import com.gemantic.search.support.result.Hit;
import com.gemantic.search.support.result.SearchHits;
import com.gemantic.search.support.result.SearchResults;
import com.gemantic.springcloud.utils.ReflectUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.flush.FlushResponse;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.open.OpenIndexResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequestBuilder;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Stats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Transient;

import java.lang.reflect.Field;
import java.util.*;

public final class ESUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ESUtils.class);

    public static  <T extends ElasticIndex> String getIndexName(Class<T> classz){
        StringBuilder builder = new StringBuilder(classz.getSimpleName().replace('.', '_'));
        for(int i = 1; i < builder.length() - 1; ++i) {
            if (isUnderscoreRequired(builder.charAt(i - 1), builder.charAt(i), builder.charAt(i + 1))) {
                builder.insert(i++, '_');
            }
        }
        return builder.toString().toLowerCase();

    }

    public static boolean isUnderscoreRequired(char before, char current, char after) {
        return Character.isLowerCase(before) && Character.isUpperCase(current) && Character.isLowerCase(after);
    }

    public static <T extends ElasticIndex> Boolean indexing(
            Client client, T object) {
        String index = getIndexName(object.getClass());
        return indexing(client,index,object);
    }

    public static <T extends ElasticIndex> Boolean indexing(
            Client client,String index, T object) {
        if(null == object){
            return Boolean.TRUE;
        }
        try {

            IndexRequestBuilder rb = client.prepareIndex(index, CommonFields.DEFALUT_TYPE);
            if (StringUtils.isNotBlank(object.getRouting())) {
                rb.setRouting(object.getRouting());
            }
            object.initId();
            IndexResponse indexResponse = rb.setId(object.getId()).setSource(getMappingFields(object)).execute().actionGet();
            LOG.info("routing: " + object.getRouting() + " index id:" + indexResponse.getId() + "  " + indexResponse.getVersion());
            return Boolean.TRUE;
        } catch (Exception e) {
            LOG.error("index [{}] id={}" ,index, object.getId(), e);
            return Boolean.FALSE;
        }
    }



    public static Boolean refresh(Client client, String index) {
        try {
            RefreshResponse res = client.admin().indices().prepareRefresh(index).execute().actionGet();
            LOG.info(
                    String.format(
                            "refresh %s success shard=%d total shard=%d",
                            index, res.getSuccessfulShards(), res.getTotalShards()));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("refresh", e);
            return false;
        }
    }

    public static Boolean flush(Client client, String index) {
        try {
            FlushResponse res = client.admin().indices().prepareFlush(index).execute().actionGet();
            LOG.info(
                    String.format(
                            "flush %s success shard=%d total shard=%d",
                            index, res.getSuccessfulShards(), res.getTotalShards()));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("flush", e);
            return false;
        }
    }


    public static Boolean bulkIndexing(
            Client client, String index, Collection<? extends ElasticIndex> collection) {
        if(CollectionUtils.isEmpty(collection)){
            return Boolean.TRUE;
        }
        try {

            BulkRequestBuilder bulkRequest = client.prepareBulk();
            for (ElasticIndex c : collection) {
                if(StringUtils.isBlank(c.getId())){
                    c.initId();
                }
                IndexRequestBuilder rb = client.prepareIndex(index, CommonFields.DEFALUT_TYPE, c.getId()).setSource(getMappingFields(c));
                if(StringUtils.isNotBlank(c.getRouting())){
                    rb.setRouting(c.getRouting());
                }
                LOG.info(" bulkIndexing id:" + c.getId() + " ,routing:"+c.getRouting());
                bulkRequest.add(rb);
            }
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                Map<String, ElasticIndex> cMap = new HashMap<String, ElasticIndex>();
                for (ElasticIndex ci : collection) {
                    cMap.put(ci.getId(), ci);
                }
                for (BulkItemResponse item : bulkResponse.getItems()) {
                    if (item.isFailed()) {
                        LOG.info("bulkIndexing failed id:" + item.getId());
                        indexing(client, index, cMap.get(item.getId()));
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("index=" + index  + " size:" + collection.size(), e);
            return false;
        }
    }




    public static Boolean delete(Client client, String index, String docID) {
        return delete(client, index, null, docID);
    }

    public static Boolean delete(Client client, String index, String routing, String docID) {
        try {
            DeleteRequestBuilder rb = client.prepareDelete(index, CommonFields.DEFALUT_TYPE, docID);
            if (StringUtils.isNotBlank(routing)) {
                rb.setRouting(routing);
            }
            DeleteResponse res = rb.execute().actionGet();
            LOG.info("delete " + index +" routing: " + routing + " docID:" + docID + "  " + res.getVersion());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("delete index docID:" + docID, e);
            return false;
        }
    }

    public static Boolean bulkDelete(
            Client client, String index, List<String> docIDList) {
        return bulkDelete(client, index, null, docIDList);
    }


    public static Boolean bulkDelete(
            Client client, String index, List<String> routings, List<String> docIDList) {
        if (CollectionUtils.isEmpty(docIDList)) {
            return Boolean.TRUE;
        }
        try {
            boolean hasRouting = CollectionUtils.isNotEmpty(routings) && routings.size() >= docIDList.size();
            BulkRequestBuilder bulkRequest = client.prepareBulk(index,CommonFields.DEFALUT_TYPE);
            for (int i = 0; i < docIDList.size(); i++) {
                String docID = docIDList.get(i);
                DeleteRequestBuilder rb = client.prepareDelete(index, CommonFields.DEFALUT_TYPE, docID);
                String routing = null;
                if (hasRouting) {
                    routing = routings.get(i);
                    rb.setRouting(routing);
                }
                bulkRequest.add(rb);
                LOG.info("bulkDelete routing: " + routing + " id:" + docID);
            }


            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            for (BulkItemResponse item : bulkResponse.getItems()) {
                if (item.isFailed()) {
                    LOG.info("bulkDelete failed id:" + item.getId());
                    delete(client, index, item.getId());
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("bulkDelete=" + index+ " size:" + docIDList.size(), e);
            return false;
        }
    }

    public static <T extends ElasticIndex> T getObject(
            Client client, String index, String _id, Class<T> classz) {
        return getObject(client, index,null, _id, classz);
    }

    public static <T extends ElasticIndex> T getObject(
            Client client, String index,String routing, String _id, Class<T> classz) {
        if (null == _id) {
            return null;
        }
        T res = null;
        try {

            GetRequestBuilder requestBuilder = client.prepareGet(index,CommonFields.DEFALUT_TYPE, _id);
            if(StringUtils.isNotBlank(routing)){
                requestBuilder.setRouting(routing);
            }
            GetResponse response = requestBuilder.execute().actionGet();
            if (response.isExists()) {
                res = JsonConvertUtil.str2Object(response.getSourceAsString(), classz);
                res.setId(response.getId());
            }
        } catch (Exception e) {
            LOG.error("getObjects=" + index + " id:" + _id, e);
            e.printStackTrace();
        }
        return res;
    }


    public static <T extends ElasticIndex> List<T> getObjects(
            Client client, String index, List<String> idList, Class<T> classz) {

        return getObjects(client, index,null, idList, classz);
    }


    public static <T extends ElasticIndex> List<T> getObjects(
            Client client, String index, List<String> routings, List<String> idList, Class<T> classz) {
        if (CollectionUtils.isEmpty(idList)) {
            return new ArrayList<T>();
        }

        List<T> res = new ArrayList<T>();
        try {
            boolean hasRouting = CollectionUtils.isNotEmpty(routings) && routings.size() >= idList.size();
            MultiGetRequestBuilder requestBuilder = client.prepareMultiGet();
            for (int i = 0; i < idList.size(); i++) {
                String id = idList.get(i);
                MultiGetRequest.Item item = new MultiGetRequest.Item(index, id);
                if (hasRouting) {
                    String routing = routings.get(i);
                    item.routing(routing);
                }
                requestBuilder.add(item);
            }


            MultiGetResponse responses = requestBuilder.execute().actionGet();
            for (MultiGetItemResponse response : responses.getResponses()) {
                T o = null;
                if (response.isFailed()) {
                    o = getObject(client, index, response.getId(), classz);
                } else if (response.getResponse().isExists()) {
                    o = JsonConvertUtil.str2Object(response.getResponse().getSourceAsString(), classz);
                    o.setId(response.getResponse().getId());
                }
                if (null != o) {
                    res.add(o);
                }
            }


        } catch (Exception e) {
            LOG.error("getObjects=" + index + " size:" + idList.size(), e);
            e.printStackTrace();
        }
        return res;
    }


    public static SearchResults convertToSearchResults(SearchResponse response) {
        SearchHits searchHits = new SearchHits();
        searchHits.setTotalHits(response.getHits().getTotalHits().value);
        searchHits.setMaxScore(response.getHits().getMaxScore());
        searchHits.setHits(new ArrayList<Hit>());

        for (Iterator<SearchHit> iterator = response.getHits().iterator(); iterator.hasNext(); ) {
            SearchHit searchHit = iterator.next();
            Hit hit = new Hit();
            hit.setId(searchHit.getId());
            hit.setScore(searchHit.getScore());
            hit.setSourceAsString(searchHit.getSourceAsString());

            for (String highlightedFields : searchHit.getHighlightFields().keySet()) {
                StringBuilder highlightedContextSB = new StringBuilder();
                for (Text hlf : searchHit.getHighlightFields().get(highlightedFields).getFragments()) {
                    highlightedContextSB.append(hlf.string());
                }

                hit.getSnippets().put(highlightedFields, highlightedContextSB.toString());
            }

            searchHits.getHits().add(hit);
        }

        SearchResults searchResults = new SearchResults();
        searchResults.setTookInMillis(response.getTook().getMillis());
        searchResults.setScrollId(response.getScrollId());
        searchResults.setTotalHits(searchHits.getTotalHits());
        searchResults.setSearchHits(searchHits);

        //解析Facet
        List<TermAggResult> itemAggResults = Lists.newArrayList();
        searchResults.setAggs(itemAggResults);
        Aggregations aggs = response.getAggregations();
        if (null == aggs) {
            return searchResults;
        }
        Set<String> aggNames = aggs.asMap().keySet();
        for (String aggName : aggNames) {
            Aggregation agg = aggs.get(aggName);
            if (!(agg instanceof Terms)) {
                continue;
            }

            Terms termsAgg = (Terms) agg;
            //      System.out.println(termsAgg);
            List<? extends Terms.Bucket> buckets = termsAgg.getBuckets();

            //      System.out.println(aggName + "*****1*****" + buckets.size());
            if (CollectionUtils.isEmpty(buckets)) {
                continue;
            }
            String itemAggName = termsAgg.getName();
            boolean isStat = Boolean.FALSE;
            if (itemAggName.endsWith("_STATS")) {
                isStat = Boolean.TRUE;
                itemAggName = itemAggName.substring(0, termsAgg.getName().length() - "_STATS".length());
            }
            TermAggResult itemAggResult = new TermAggResult();
            itemAggResult.setOtherCount(termsAgg.getSumOfOtherDocCounts());
            List<TermAggEntry> itemAggEntries = Lists.newArrayList();
            itemAggResult.setEntries(itemAggEntries);
            itemAggResult.setName(itemAggName);
            Long totalCount = 0L;
            for (Terms.Bucket bucket : buckets) {
                TermAggEntry e = new TermAggEntry();
                if (isStat) {
                    for (Aggregation bucketAgg : bucket.getAggregations().asList()) {
                        Stats stats = (Stats) bucketAgg;
                        e.setCount(stats.getCount());
                        e.setMax(stats.getMax());
                        e.setMean(stats.getAvg());
                        e.setMin(stats.getMin());
                        e.setField(bucket.getKeyAsString());
                        e.setTotal(stats.getSum());
                    }
                } else {
                    e.setCount(Long.valueOf(bucket.getDocCount()).intValue());
                    e.setField(bucket.getKeyAsString());
                }

                totalCount += bucket.getDocCount();
                itemAggEntries.add(e);
            }
            itemAggResult.setTotalCount(totalCount);
            itemAggResult.setBucketTotalCount(buckets.size());
            itemAggResults.add(itemAggResult);
        }

        return searchResults;
    }


    public static Map<String, Object> getMappingFields(Object obj) {
        Map<String, Object> mappingFields = new HashMap<String, Object>();

        if (null == obj) {
            return mappingFields;
        }

        List<Field> fields = ReflectUtil.getFieldAllList(obj.getClass());
        for(Field field :fields){
            if(field.isAnnotationPresent(Transient.class)){
                continue;
            }
            field.setAccessible(true);
            Object fieldValue =  null;
            try {
                fieldValue = field.get(obj);
            }catch (Exception e){
                LOG.error("getMappingFields [{}] {}",obj.getClass().getSimpleName(), e);
            }
            if(null != fieldValue){
                mappingFields.put(field.getName(), fieldValue);
            }

        }
        return mappingFields;
    }




    public static boolean isIndexExist(Client client,String index) throws Exception {
        return client.admin().indices().prepareExists(index).execute().actionGet().isExists();
    }


    public static boolean createIndex(Client client, IndexInfo index) throws Exception {
        CreateIndexRequestBuilder cirb = client.admin().indices().prepareCreate(index.getIndex());
        if(StringUtils.isNotBlank(index.getSetting())) {
            cirb.setSettings(index.getSetting(), XContentType.JSON);
        }
        CreateIndexResponse createIndexResponse = cirb.execute().actionGet();
        return createIndexResponse.isAcknowledged();
    }

    public static boolean putMapping(Client client,IndexInfo index) throws Exception {
        if (StringUtils.isBlank(index.getMapping())) {
            return Boolean.TRUE;
        }
        AcknowledgedResponse response = client.admin().indices()
                .preparePutMapping(index.getIndex())
                .setSource(index.getMapping(), XContentType.JSON)
                .execute().actionGet();
        return response.isAcknowledged();
    }


    public static boolean mergeSetting(Client client,IndexInfo index) throws Exception {
        if (StringUtils.isBlank(index.getSetting())) {
            return Boolean.TRUE;
        }
        UpdateSettingsRequestBuilder usrb = client.admin().indices().prepareUpdateSettings(index.getIndex());
        usrb.setSettings(index.getSetting(), XContentType.JSON);
        usrb.execute().actionGet();
        OpenIndexRequestBuilder oirb = client.admin().indices().prepareOpen(index.getIndex());
        OpenIndexResponse openIndexResponse = oirb.execute().actionGet();
        return openIndexResponse.isAcknowledged();
    }


    public static boolean initAliases(Client client,Map<String, String> indexAliases) throws Exception {
       return Boolean.TRUE;
    }


//    public static void main(String[] args) throws Exception {
//        ElasticRestClient client = new ElasticRestClient();
//        client.afterPropertiesSet();
//    }
}
