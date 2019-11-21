package com.gemantic.search.utils;

import com.gemantic.search.model.ElasticIndex;
import com.gemantic.search.support.elastic.IndexInfo;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.flush.FlushResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.*;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ESRestUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ESRestUtils.class);



    public static <T extends ElasticIndex> Boolean indexing(
            RestHighLevelClient client, T object) {
        String index = ESUtils.getIndexName(object.getClass());
        return indexing(client,index,object);
    }

    public static <T extends ElasticIndex> Boolean indexing(
            RestHighLevelClient client,String index, T object) {
        if(StringUtils.isBlank(index) || null == object){
            LOG.warn("params error index [{}] object {}",index,object);
            return Boolean.FALSE;
        }
        try {
            IndexRequest request = new IndexRequest(index);
            object.initId();
            request.id(object.getId());
            if(StringUtils.isNotBlank(object.getRouting())) {
                request.routing(object.getRouting());
            }
            request.source(ESUtils.getMappingFields(object));
            IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
            if(LOG.isDebugEnabled()) {
                LOG.debug("routing: " + object.getRouting() + " index id:" + indexResponse.getId() + "  " + indexResponse.getVersion());
            }
            return Boolean.TRUE;
        } catch (Exception e) {
            LOG.error("index [{}] id={} {}" ,index, object.getId(), e);
            return Boolean.FALSE;
        }
    }

    public static Boolean refresh(RestHighLevelClient client, String index) {
        try {
            RefreshRequest request = new RefreshRequest(index);
            request.indicesOptions(IndicesOptions.lenientExpandOpen());
            RefreshResponse refreshResponse = client.indices().refresh(request, RequestOptions.DEFAULT);
            LOG.info(
                    String.format(
                            "refresh %s success shard=%d total shard=%d",
                            index, refreshResponse.getSuccessfulShards(), refreshResponse.getTotalShards()));
            return Boolean.TRUE;
        } catch (Exception e) {

            LOG.error("refresh [{}] {}",index, e);
            return Boolean.FALSE;
        }
    }

    public static Boolean flush(RestHighLevelClient client, String index) {
        try {
            FlushRequest request = new FlushRequest(index);
            request.indicesOptions(IndicesOptions.lenientExpandOpen());
            request.waitIfOngoing(true);
            request.force(true);
            FlushResponse flushResponse = client.indices().flush(request, RequestOptions.DEFAULT);

            LOG.info(
                    String.format(
                            "flush %s success shard=%d total shard=%d",
                            index, flushResponse.getSuccessfulShards(), flushResponse.getTotalShards()));
            return Boolean.TRUE;
        } catch (Exception e) {

            LOG.error("flush", e);
            return Boolean.FALSE;
        }
    }

    public static <T extends ElasticIndex> Boolean bulkIndexing(
            RestHighLevelClient client, Collection<T> collection) {
        if(CollectionUtils.isEmpty(collection)){
            return Boolean.TRUE;
        }
        return bulkIndexing(client,ESUtils.getIndexName(collection.iterator().next().getClass()),collection);
    }

    public static <T extends ElasticIndex> Boolean bulkIndexing(
            RestHighLevelClient client, String index, Collection<T> collection) {
        if(StringUtils.isBlank(index) || CollectionUtils.isEmpty(collection)){
            LOG.warn("params error index [{}] objects {}",index,collection);
            return Boolean.FALSE;
        }
        try {
            BulkRequest request = new BulkRequest(index);
            for (T c : collection) {
                c.initId();
                IndexRequest indexRequest = new IndexRequest(index);
                indexRequest.id(c.getId());
                if(StringUtils.isNotBlank(c.getRouting())){
                    indexRequest.routing(c.getRouting());
                }
                indexRequest.source(ESUtils.getMappingFields(c));

                if(LOG.isDebugEnabled()) {
                    LOG.debug(" bulkIndexing id:" + c.getId() + " ,routing:" + c.getRouting());
                }
                request.add(indexRequest);
            }
            BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);
            if (bulkResponse.hasFailures()) {
                Map<String, T> cMap = collection.stream().collect(Collectors.toMap(T::getId, Function.identity(),(k1,k2)->k1));
                for (BulkItemResponse item : bulkResponse.getItems()) {
                    if (item.isFailed()) {
                        LOG.warn("bulkIndexing failed id:" + item.getId());
                        indexing(client, index, cMap.get(item.getId()));
                    }
                }
            }
            return Boolean.TRUE;
        } catch (Exception e) {
            LOG.error("index=" + index + " size:" + collection.size(), e);
            return Boolean.FALSE;
        }
    }





    public static Boolean delete(RestHighLevelClient client, String index, String docID) {
        return delete(client, index, null, docID);
    }

    public static Boolean delete(RestHighLevelClient client, String index, String routing, String id) {
        if(StringUtils.isBlank(index) || StringUtils.isBlank(id)){
            LOG.warn("params error index [{}] id [{}]",index,id);
            return Boolean.FALSE;
        }
        try {
            DeleteRequest request = new DeleteRequest(index);
            request.id(id);
            if(StringUtils.isNotBlank(routing)){
                request.routing(routing);
            }
            DeleteResponse deleteResponse = client.delete(
                    request, RequestOptions.DEFAULT);
            if(LOG.isDebugEnabled()) {
                LOG.debug("delete [{}] routing [{}] id [{}] version [{}]", index, routing, id, deleteResponse.getVersion());
            }
            return Boolean.TRUE;
        } catch (Exception e) {
            LOG.error("delete [{}] routing [{}] id [{}] ",index,routing,id,e);
            return Boolean.FALSE;
        }
    }

    public static Boolean bulkDelete(
            RestHighLevelClient client, String index, List<String> idList) {
        return bulkDelete(client, index, null, idList);
    }


    public static Boolean bulkDelete(
            RestHighLevelClient client, String index, List<String> routings, List<String> idList) {
        if (StringUtils.isBlank(index) || CollectionUtils.isEmpty(idList)) {
            LOG.warn("params error index [{}] id [{}]",index,idList);
            return Boolean.FALSE;
        }
        try {
            BulkRequest request = new BulkRequest(index);
            boolean hasRouting = CollectionUtils.isNotEmpty(routings) && routings.size() >= idList.size();

            for (int i = 0; i < idList.size(); i++) {
                String docID = idList.get(i);
                DeleteRequest deleteRequest = new DeleteRequest(index);
                deleteRequest.id(docID);
                String routing = null;
                if (hasRouting) {
                    routing = routings.get(i);
                    deleteRequest.routing(routing);
                }
                request.add(deleteRequest);
                if(LOG.isDebugEnabled()) {
                    LOG.debug("bulkDelete routing: " + routing + " id:" + docID);
                }
            }
            BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);
            for (BulkItemResponse item : bulkResponse.getItems()) {
                if (item.isFailed()) {
                    LOG.warn("bulkDelete failed id:" + item.getId());
                    delete(client, index, item.getId());
                }
            }
            return Boolean.TRUE;
        } catch (Exception e) {
            LOG.error("bulkDelete=" + index + " size:" + idList.size(), e);
        }
        return Boolean.FALSE;
    }

    public static <T extends ElasticIndex> T getObject(
            RestHighLevelClient client, String index, String _id, Class<T> classz) {
        return getObject(client, index,null, _id, classz);
    }

    public static <T extends ElasticIndex> T getObject(
            RestHighLevelClient client, String index,String routing, String id, Class<T> classz) {
        if(StringUtils.isBlank(index) || StringUtils.isBlank(id)){
            LOG.warn("params error index [{}] id [{}]",index,id);
            return null;
        }
        T res = null;
        try {

            GetRequest request = new GetRequest(
                    index,
                    id);
            if(StringUtils.isNotBlank(routing)){
                request.routing(routing);
            }
            GetResponse response = client.get(request, RequestOptions.DEFAULT);
            if (response.isExists()) {
                res = JsonConvertUtil.str2Object(response.getSourceAsString(), classz);
                res.setId(response.getId());
            }
        } catch (Exception e) {
            LOG.error("getObjects=" + index +  " id:" + id, e);
        }
        return res;
    }


    public static <T extends ElasticIndex> List<T> getObjects(
            RestHighLevelClient client, String index, List<String> idList, Class<T> classz) {

        return getObjects(client, index,null, idList, classz);
    }


    public static <T extends ElasticIndex> List<T> getObjects(
            RestHighLevelClient client, String index, List<String> routings, List<String> idList, Class<T> classz) {
        if (StringUtils.isBlank(index) || CollectionUtils.isEmpty(idList)) {
            LOG.warn("params error index [{}] id [{}]",index,idList);
            return Lists.newArrayList();
        }

        List<T> res = new ArrayList<T>();
        try {
            boolean hasRouting = CollectionUtils.isNotEmpty(routings) && routings.size() >= idList.size();
            MultiGetRequest request = new MultiGetRequest();
            for (int i = 0; i < idList.size(); i++) {
                String id = idList.get(i);
                MultiGetRequest.Item item = new MultiGetRequest.Item(index, id);
                if (hasRouting) {
                    String routing = routings.get(i);
                    item.routing(routing);
                }
                request.add(item);
            }
            MultiGetResponse responses = client.mget(request, RequestOptions.DEFAULT);
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
        }
        return res;
    }




    public static boolean isTemplateExist(RestHighLevelClient client,String template) throws Exception {
        IndexTemplatesExistRequest request = new IndexTemplatesExistRequest(template);
        request.setMasterNodeTimeout(TimeValue.timeValueMinutes(1));
        return client.indices().existsTemplate(request, RequestOptions.DEFAULT);
    }

    public static boolean deleteTemplate(RestHighLevelClient client,String template) throws Exception {
        DeleteIndexTemplateRequest request = new DeleteIndexTemplateRequest();
        request.name(template);
        AcknowledgedResponse deleteTemplateAcknowledge = client.indices().deleteTemplate(request, RequestOptions.DEFAULT);
        return deleteTemplateAcknowledge.isAcknowledged();
    }

    public static boolean putTemplate(RestHighLevelClient client,String template, String source) throws Exception {
        PutIndexTemplateRequest request = new PutIndexTemplateRequest(template);
        request.mapping(source, XContentType.JSON);
        request.create(true);
        request.masterNodeTimeout(TimeValue.timeValueMinutes(1));
        AcknowledgedResponse putTemplateResponse = client.indices().putTemplate(request, RequestOptions.DEFAULT);
        return putTemplateResponse.isAcknowledged();
    }


    public static boolean isIndexExist(RestHighLevelClient client,String index) throws Exception {
        GetIndexRequest request = new GetIndexRequest(index);
        return client.indices().exists(request, RequestOptions.DEFAULT);
    }


    public static boolean createIndex(RestHighLevelClient client,IndexInfo index) throws Exception {
        CreateIndexRequest request = new CreateIndexRequest(index.getIndex());
        if (StringUtils.isNotBlank(index.getSetting())) {
            request.settings(index.getSetting(), XContentType.JSON);
        }
        request.setTimeout(TimeValue.timeValueMinutes(2));
        request.setMasterTimeout(TimeValue.timeValueMinutes(1));
        request.waitForActiveShards(ActiveShardCount.DEFAULT);
        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
        return createIndexResponse.isAcknowledged();
    }

    public static boolean putMapping(RestHighLevelClient client,IndexInfo index) throws Exception {
        if (StringUtils.isBlank(index.getMapping())) {
            return Boolean.TRUE;
        }
        PutMappingRequest request = new PutMappingRequest(index.getIndex());
        request.source(index.getMapping(), XContentType.JSON);
        request.setTimeout(TimeValue.timeValueMinutes(2));
        request.setMasterTimeout(TimeValue.timeValueMinutes(1));
        AcknowledgedResponse putMappingResponse = client.indices().putMapping(request, RequestOptions.DEFAULT);
        return putMappingResponse.isAcknowledged();
    }


    public static boolean mergeSetting(RestHighLevelClient client,IndexInfo index) throws Exception {
        if (StringUtils.isBlank(index.getSetting())) {
            return Boolean.TRUE;
        }
        UpdateSettingsRequest request = new UpdateSettingsRequest(index.getSetting());
        request.setPreserveExisting(false);
        request.settings(index.getSetting(), XContentType.JSON);
        request.timeout(TimeValue.timeValueMinutes(2));
        request.masterNodeTimeout(TimeValue.timeValueMinutes(1));
        request.indicesOptions(IndicesOptions.lenientExpandOpen());
        AcknowledgedResponse updateSettingsResponse =
                client.indices().putSettings(request, RequestOptions.DEFAULT);
        return updateSettingsResponse.isAcknowledged();
    }


    public static boolean initAliases(RestHighLevelClient client,Map<String, String> indexAliases) throws Exception {
        if (MapUtils.isEmpty(indexAliases)) {
            return Boolean.TRUE;
        }
        IndicesAliasesRequest request = new IndicesAliasesRequest();
        request.timeout(TimeValue.timeValueMinutes(2));
        request.masterNodeTimeout(TimeValue.timeValueMinutes(1));
        for (Map.Entry<String, String> indexAliaseEntry : indexAliases.entrySet()) {
            IndicesAliasesRequest.AliasActions aliasAction =
                    new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
                            .index(indexAliaseEntry.getKey())
                            .alias(indexAliaseEntry.getValue());
            request.addAliasAction(aliasAction);
        }
        AcknowledgedResponse indicesAliasesResponse =
                client.indices().updateAliases(request, RequestOptions.DEFAULT);
        return indicesAliasesResponse.isAcknowledged();
    }

}
