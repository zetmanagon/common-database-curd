package com.gemantic.search.repository.impl;

import com.gemantic.search.model.ElasticIndex;
import com.gemantic.search.model.ElasticPage;
import com.gemantic.search.repository.BaseSearchRepository;
import com.gemantic.search.support.query.ElasticQuery;
import com.gemantic.search.support.result.SearchResults;
import com.gemantic.search.utils.ESQueryUtil;
import com.gemantic.search.utils.ESRestUtils;
import com.gemantic.search.utils.ESUtils;
import com.gemantic.search.utils.JsonConvertUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryAction;
import org.elasticsearch.index.reindex.UpdateByQueryRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.NoRepositoryBean;

import javax.annotation.Resource;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@NoRepositoryBean
public class BaseSearchRestRepositoryImpl <T extends ElasticIndex> implements BaseSearchRepository<T> {

    protected Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Resource(name="esRestClient")
    protected RestHighLevelClient client;

    @Override
    public boolean save(Collection<T> collection) throws Exception {
        if(CollectionUtils.isEmpty(collection)){
            return true;
        }
        return ESRestUtils.bulkIndexing(client, collection);
    }


    @Override
    public SearchResults search(ElasticQuery query) throws Exception {
        if(StringUtils.isBlank(query.getIndex())) {
            query.setIndex(ESUtils.getIndexName(getTClass()));
        }
        if(StringUtils.isNotBlank(query.getScrollId())){
            query.setScrollSearch(Boolean.TRUE);
        }
        ActionRequest request = ESQueryUtil.searchRequest(query);
        LOG.info(request+"");
        SearchResponse response = null;
        if (SearchScrollRequest.class.equals(request.getClass())){
            response = client.scroll((SearchScrollRequest)request, RequestOptions.DEFAULT);
        }else {
            response = client.search((SearchRequest)request, RequestOptions.DEFAULT);;
        }
        return ESUtils.convertToSearchResults(response);
    }

    @Override
    public ElasticPage<T> searchPage(ElasticQuery query) throws Exception {
        if(StringUtils.isBlank(query.getIndex())) {
            query.setIndex(ESUtils.getIndexName(getTClass()));
        }
        SearchResults result = search(query);
        return JsonConvertUtil.getElasticPage(result,query.getCurrentPage(),query.getPageSize(),query.getAggs(),getTClass());
    }

    @Override
    public List<T> getObjectById(List<String> idList) throws Exception {
        return getObjectById(null,null,idList);
    }

    @Override
    public List<T> getObjectById(String index, List<String> routings, List<String> idList) throws Exception {
        if(CollectionUtils.isEmpty(idList)){
            return Lists.newArrayList();
        }

        if(StringUtils.isBlank(index)){
            index = ESUtils.getIndexName(getTClass());
        }
        if(idList.size() == 1) {
            String routing = null;
            if (CollectionUtils.isNotEmpty(routings)) {
                routing = routings.get(0);
            }
            T obj = ESRestUtils.getObject(client, index,routing,idList.get(0),getTClass());
            if(null != obj){
                return Lists.newArrayList(obj);
            }else {
                return Lists.newArrayList();
            }
        }
        return ESRestUtils.getObjects(client,index,routings,idList,getTClass());
    }

    @Override
    public void deleteByQuery(ElasticQuery query) throws Exception {
        if(StringUtils.isBlank(query.getIndex())) {
            query.setIndex(ESUtils.getIndexName(getTClass()));
        }
        BoolQueryBuilder queryBuilder = ESQueryUtil.getQueryBuilder(query);
        LOG.info("Delete by query {}",queryBuilder);
        DeleteByQueryRequest request =
                new DeleteByQueryRequest(query.getIndex());
        request.setIndicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
        request.setQuery(queryBuilder);
        if(null != query.getSlices() && query.getSlices() > 0) {
            request.setSlices(query.getSlices());
        }
        if(null != query.getBatchSize() && query.getBatchSize() > 0){
            request.setBatchSize(query.getBatchSize());
        }
        if(null != query.getDeleteCount() && query.getDeleteCount() > 0){
            request.setSize(query.getDeleteCount());
        }
        if(query.isAsync()){
            client.deleteByQueryAsync(request, RequestOptions.DEFAULT, new ActionListener<BulkByScrollResponse>() {
                @Override
                public void onResponse(BulkByScrollResponse response) {
                    long deleted = response.getDeleted();
                    LOG.info("deleteByQuery "+deleted+", total:"+response.getTotal());
                }
                @Override
                public void onFailure(Exception e) {
                    LOG.error("deleteByQuery ",e);
                }
            });
        }else {
            request.setScroll(new TimeValue(query.getScrollTime()));
            BulkByScrollResponse bulkResponse =
                    client.deleteByQuery(request, RequestOptions.DEFAULT);
            LOG.info("deleteByQuery "+bulkResponse.getDeleted()+", total:"+bulkResponse.getTotal());
        }

    }


    @Override
    public boolean deleteByRoutingAndId(String index, List<String> routings, List<String> idList) throws Exception {
        if(CollectionUtils.isEmpty(idList)){
            return Boolean.TRUE;
        }

        if(StringUtils.isBlank(index)){
            index = ESUtils.getIndexName(getTClass());
        }
        if(idList.size() == 1){
            String routing = null;
            if(CollectionUtils.isNotEmpty(routings)){
                routing = routings.get(0);
            }
            return ESRestUtils.delete(client, index,routing, idList.get(0));
        }
        return ESRestUtils.bulkDelete(client, index,routings, idList);
    }

    @Override
    public boolean deleteById(List<String> idList) throws Exception {
        return deleteByRoutingAndId(null,null,idList);
    }

    public Class<T> getTClass() {
        Class<T> entityClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return entityClass;
    }

}
