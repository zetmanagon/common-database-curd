package com.gemantic.search.repository.impl;

import com.gemantic.search.model.ElasticIndex;
import com.gemantic.search.model.ElasticPage;
import com.gemantic.search.repository.BaseSearchRepository;
import com.gemantic.search.support.query.ElasticQuery;
import com.gemantic.search.support.result.SearchResults;
import com.gemantic.search.utils.ESQueryUtil;
import com.gemantic.search.utils.ESUtils;
import com.gemantic.search.utils.JsonConvertUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.reindex.*;
import org.elasticsearch.script.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.NoRepositoryBean;

import javax.annotation.Resource;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Map;


@NoRepositoryBean
public class BaseSearchRepositoryImpl<T extends ElasticIndex> implements BaseSearchRepository<T> {

    protected Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Resource(name = "esClient")
    protected Client client;


    @Override
    public boolean save(Collection<T> collection) throws Exception {
        if (CollectionUtils.isEmpty(collection)) {
            return true;
        }
        return ESUtils.bulkIndexing(client, ESUtils.getIndexName(getTClass()), collection);
    }


    @Override
    public SearchResults search(ElasticQuery query) throws Exception {
        if (StringUtils.isBlank(query.getIndex())) {
            query.setIndex(ESUtils.getIndexName(getTClass()));
        }
        if (StringUtils.isNotBlank(query.getScrollId())) {
            query.setScrollSearch(Boolean.TRUE);
        }
        ActionRequestBuilder requestBuilder = ESQueryUtil.requstBuilder(client, query);
        LOG.info(requestBuilder.toString());
        SearchResponse response = null;
        if (SearchScrollRequestBuilder.class.equals(requestBuilder.getClass())) {
            response = ((SearchScrollRequestBuilder) requestBuilder).get();
        } else {
            response = ((SearchRequestBuilder) requestBuilder).execute().actionGet();
        }
        return ESUtils.convertToSearchResults(response);
    }

    @Override
    public ElasticPage<T> searchPage(ElasticQuery query) throws Exception {
        if (StringUtils.isBlank(query.getIndex())) {
            query.setIndex(ESUtils.getIndexName(getTClass()));
        }
        SearchResults result = search(query);
        return JsonConvertUtil.getElasticPage(result, query.getCurrentPage(), query.getPageSize(),query.getAggs(), getTClass());
    }

    @Override
    public List<T> getObjectById(List<String> idList) throws Exception {
        return getObjectById(null, null, idList);
    }

    @Override
    public List<T> getObjectById(String index, List<String> routings, List<String> idList) throws Exception {
        if (CollectionUtils.isEmpty(idList)) {
            return Lists.newArrayList();
        }

        if (StringUtils.isBlank(index)) {
            index = ESUtils.getIndexName(getTClass());
        }
        if (idList.size() == 1) {
            String routing = null;
            if (CollectionUtils.isNotEmpty(routings)) {
                routing = routings.get(0);
            }
            T obj = ESUtils.getObject(client, index, routing, idList.get(0), getTClass());
            if (null != obj) {
                return Lists.newArrayList(obj);
            } else {
                return Lists.newArrayList();
            }
        }
        return ESUtils.getObjects(client, index, routings, idList, getTClass());
    }

    @Override
    public void deleteByQuery(ElasticQuery query) throws Exception {
        if (StringUtils.isBlank(query.getIndex())) {
            query.setIndex(ESUtils.getIndexName(getTClass()));
        }
        BoolQueryBuilder queryBuilder = ESQueryUtil.getQueryBuilder(query);
        LOG.info("Delete by query {}", queryBuilder);
        DeleteByQueryRequestBuilder deleteByQueryRequestBuilder = new DeleteByQueryRequestBuilder(client, DeleteByQueryAction.INSTANCE);
        //删除并行任务数
        if (null != query.getSlices() && query.getSlices() > 0) {
            deleteByQueryRequestBuilder.setSlices(query.getSlices());
        }
        if (null != query.getDeleteCount() && query.getDeleteCount() > 0) {
            deleteByQueryRequestBuilder.size(query.getDeleteCount());
        }
        deleteByQueryRequestBuilder.filter(queryBuilder)
                .source(query.getIndex());

        if (query.isAsync()) {
            deleteByQueryRequestBuilder.execute(new ActionListener<BulkByScrollResponse>() {
                @Override
                public void onResponse(BulkByScrollResponse response) {
                    long deleted = response.getDeleted();
                    LOG.info("async deleteByQuery " + deleted + ", total:" + response.getTotal());
                }

                @Override
                public void onFailure(Exception e) {
                    LOG.error("deleteByQuery ", e);
                }
            });
        } else {

            BulkByScrollResponse response = deleteByQueryRequestBuilder.get();
            long deleted = response.getDeleted();
            LOG.info("scroll deleteByQuery " + deleted + ", total:" + response.getTotal());
        }
    }


    @Override
    public boolean deleteByRoutingAndId(String index, List<String> routings, List<String> idList) throws Exception {
        if (CollectionUtils.isEmpty(idList)) {
            return Boolean.TRUE;
        }

        if (StringUtils.isBlank(index)) {
            index = ESUtils.getIndexName(getTClass());
        }
        if (idList.size() == 1) {
            String routing = null;
            if (CollectionUtils.isNotEmpty(routings)) {
                routing = routings.get(0);
            }
            return ESUtils.delete(client, index, routing, idList.get(0));
        }
        return ESUtils.bulkDelete(client, index, routings, idList);
    }

    @Override
    public boolean deleteById(List<String> idList) throws Exception {
        return deleteByRoutingAndId(null, null, idList);
    }

    public Class<T> getTClass() {
        Class<T> entityClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return entityClass;
    }


}
