package com.gemantic.search.controller;

import com.gemantic.search.client.BaseSearchClient;
import com.gemantic.search.constant.CommonFields;
import com.gemantic.search.model.ElasticIndex;
import com.gemantic.search.model.ElasticPage;
import com.gemantic.search.repository.BaseSearchRepository;
import com.gemantic.search.support.agg.TermAgg;
import com.gemantic.search.support.query.ElasticQuery;
import com.gemantic.search.support.query.QueryItem;
import com.gemantic.search.support.query.SortItem;
import com.gemantic.search.utils.ESQueryUtil;
import com.gemantic.search.utils.ESUtils;
import com.gemantic.search.utils.QueryConverUtil;
import com.gemantic.springcloud.model.Response;
import com.gemantic.springcloud.utils.PageUtil;
import com.gemantic.springcloud.utils.ReflectUtil;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


public abstract class BaseSearchController<T extends ElasticIndex> implements BaseSearchClient<T> {

    protected Logger LOG = LoggerFactory.getLogger(this.getClass());

    protected static final List<String> EXCLUDE_PARAMS = Lists.newArrayList("groupByPs","groupByCp","groupByTopSize","groupBy","excludeFields","includeFields","async","highlighterPostTags","highlighterPreTags","plainHighlightedFields","slices","scrollId", "scrollSearch", "scrollTime", "timeField", "startAt", "endAt", "orderBy", "direction", "ids", "cp", "ps");


    @ApiOperation(value = "简单检索", notes = "简单检索")
    @GetMapping("/search")
    public Response<ElasticPage<T>> search
            (@ApiParam(value = "根据编号集合查询") @RequestParam(required = false)
                     List<String> ids,
             @ApiParam(value = "游标编号") @RequestParam(required = false) String scrollId,
             @ApiParam(value = "时间范围字段") @RequestParam(required = false, defaultValue = "createAt")
                     String timeField,
             @ApiParam(value = "查询起始时间") @RequestParam(required = false)
                     Long startAt,
             @ApiParam(value = "查询结束时间") @RequestParam(required = false) Long endAt,
             @ApiParam(value = "排序字段") @RequestParam(required = false)
                     List<String> orderBy,
             @ApiParam(value = "排序方向", allowableValues = "DESC,ASC")
             @RequestParam(required = false)
                     List<String> direction,
             @ApiParam(value = "scroll查询：false-否，true-是") @RequestParam(required = false, defaultValue = "false") Boolean scrollSearch,
             @ApiParam(value = "下一次游标滚屏时间间隔，毫秒") @RequestParam(required = false, defaultValue = "60000") Long scrollTime,
             @ApiParam(value = "标红字段") @RequestParam(required = false) List<String> plainHighlightedFields,
             @ApiParam(value = "标红起始tag样式") @RequestParam(required = false) String highlighterPreTags,
             @ApiParam(value = "标红结束tag样式") @RequestParam(required = false)String highlighterPostTags,
             @ApiParam(value = "指定获取字段") @RequestParam(value = "includeFields",required = false)List<String> includeFields,
             @ApiParam(value = "不获取字段") @RequestParam(value = "excludeFields",required = false)List<String> excludeFields,
             @ApiParam(value = "分组统计") @RequestParam(value = "groupBy",required = false)List<String> groupBy,
             @ApiParam(value = "当前页") @RequestParam(required = false, defaultValue = "1") Integer cp,
             @ApiParam(value = "每页大小") @RequestParam(required = false, defaultValue = "10") Integer ps,
             @ApiParam(value = "aggResults结果的页数") @RequestParam(value = "groupByCp",required = false) Integer groupByCp,
             @ApiParam(value = "aggResults每页大小") @RequestParam(value = "groupByPs",required = false) Integer groupByPs,
             @ApiParam(value = "aggResults前n个结果") @RequestParam(value = "groupByTopSize",required = false,defaultValue = "10") Integer groupByTopSize,
             @ApiParam(value = "其他参数") @RequestParam(required = false) Map<String, String> params) throws Exception {

        if (cp < 0) {
            cp = 1;
        }
        if (ps > CommonFields.PAGE_MAX_SIZE) {
            ps = CommonFields.PAGE_MAX_SIZE;
        }

        if (CollectionUtils.isEmpty(orderBy)) {
            orderBy = Lists.newArrayList("createAt");
        }

        startAt = MoreObjects.firstNonNull(startAt, 0L);
        endAt = MoreObjects.firstNonNull(endAt, Long.MAX_VALUE);

        ElasticQuery query = new ElasticQuery();
        if(StringUtils.isBlank(query.getIndex())){
            query.setIndex(ESUtils.getIndexName(getTClass()));
        }
        if(CollectionUtils.isNotEmpty(includeFields)){
            query.setIncludeFields(includeFields.toArray(new String[includeFields.size()]));
        }
        if(CollectionUtils.isNotEmpty(excludeFields)){
            query.setExcludeFields(excludeFields.toArray(new String[excludeFields.size()]));
        }
        if(CollectionUtils.isNotEmpty(groupBy)){
            List<TermAgg> termAggs = groupBy.stream().map(g->new TermAgg(g,null,groupByTopSize,groupByCp,groupByPs)).collect(Collectors.toList());
            query.setAggs(termAggs);
        }
        query.setCurrentPage(cp);
        query.setPageSize(ps);
        query.setScrollSearch(scrollSearch);
        query.setScrollTime(scrollTime);
        query.setScrollId(scrollId);
        query.setPlainHighlightedFields(plainHighlightedFields);
        if(StringUtils.isNotBlank(highlighterPreTags)){
            query.setHighlighterPreTags(highlighterPreTags);
        }
        if(StringUtils.isNotBlank(highlighterPostTags)){
            query.setHighlighterPostTags(highlighterPostTags);
        }
        List<SortItem> sortItems = QueryConverUtil.getSortItems(orderBy, direction);
        query.setSorts(sortItems);
        List<QueryItem> andQuery = Lists.newArrayList();
        query.setAndQuery(andQuery);
        QueryConverUtil.addQueryItem(andQuery, timeField, startAt, endAt);
        if (CollectionUtils.isNotEmpty(ids)) {
            QueryConverUtil.addQueryItem(andQuery, "_id", String.join(",", ids));
        }
        for (Map.Entry<String, String> paramEntry : params.entrySet()) {
            if (EXCLUDE_PARAMS.contains(paramEntry.getKey())) {
                continue;
            }
            QueryConverUtil.addQueryItem(andQuery, paramEntry.getKey(), paramEntry.getValue());
        }
        ElasticPage elasticPage = getRepository().searchPage(query);
        if(null != elasticPage && CollectionUtils.isNotEmpty(elasticPage.getAggResults())){
            if(null != groupByCp && null != groupByPs && groupByCp > 0 && groupByPs > 0){
                elasticPage.setAggResults(PageUtil.getPageList(elasticPage.getAggResults(),groupByCp,groupByPs));
            }
        }
        return Response.ok(elasticPage);

    }



    @ApiOperation(value = "复杂检索", notes = "复杂检索")
    @PostMapping("/query")
    public Response<ElasticPage<T>> query
            (@ApiParam(value = "查询对象", required = true) @RequestBody ElasticQuery query) throws Exception {

        if(query.getPageSize() > CommonFields.PAGE_MAX_SIZE){
            query.setPageSize(CommonFields.PAGE_MAX_SIZE);
        }
        if(query.getCurrentPage() < 0){
            query.setCurrentPage(1);
        }
        ElasticPage elasticPage = getRepository().searchPage(query);
        return Response.ok(elasticPage);

    }


    @ApiOperation(value = "简单查询删除", notes = "简单查询删除")
    @DeleteMapping("/search")
    public Response<Void> deleteBySearch
            (@ApiParam(value = "根据编号集合查询") @RequestParam(required = false)
                     List<String> ids,
             @ApiParam(value = "时间范围字段") @RequestParam(required = false, defaultValue = "createAt")
                     String timeField,
             @ApiParam(value = "查询起始时间") @RequestParam(required = false)
                     Long startAt,
             @ApiParam(value = "查询结束时间") @RequestParam(required = false) Long endAt,
             @ApiParam(value = "同步/异步执行：true-异步,false-同步") @RequestParam(required = false, defaultValue = "true") Boolean async,
             @ApiParam(value = "删除并行任务数") @RequestParam(required = false, defaultValue = "3") Integer slices,
             @ApiParam(value = "其他参数") @RequestParam(required = false) Map<String, String> params) throws Exception {

        startAt = MoreObjects.firstNonNull(startAt, 0L);
        endAt = MoreObjects.firstNonNull(endAt, Long.MAX_VALUE);

        ElasticQuery query = new ElasticQuery();
        List<QueryItem> andQuery = Lists.newArrayList();
        query.setAndQuery(andQuery);
        QueryConverUtil.addQueryItem(andQuery, timeField, startAt, endAt);
        if (CollectionUtils.isNotEmpty(ids)) {
            QueryConverUtil.addQueryItem(andQuery, "_id", String.join(",", ids));
        }
        query.setAsync(async);
        for (Map.Entry<String, String> paramEntry : params.entrySet()) {
            if (EXCLUDE_PARAMS.contains(paramEntry.getKey())) {
                continue;
            }
            QueryConverUtil.addQueryItem(andQuery, paramEntry.getKey(), paramEntry.getValue());
        }
        query.setSlices(slices);
		getRepository().deleteByQuery(query);
        return Response.ok();

    }

    @ApiOperation(value = "复杂查询删除", notes = "复杂查询删除")
    @PostMapping("/delete")
    public Response<Void> deleteByQuery(@ApiParam(value = "查询对象", required = true) @RequestBody ElasticQuery query) throws Exception{
        getRepository().deleteByQuery(query);
        return Response.ok();
    }



    @ApiOperation(value = "根据ids查询", notes = "根据id查询")
    @GetMapping
    public Response<List<T>> getObjects(@ApiParam(value = "索引唯一键,编号", required = true) @RequestParam List<String> ids
            , @ApiParam(value = "是否建立了routing:false-否；true-是") @RequestParam(required = false,defaultValue = "false") Boolean hasRouting
			, @ApiParam(value = "id一一对应的routings") @RequestParam(required = false) List<String> routings) throws Exception {
        if (hasRouting && (CollectionUtils.isEmpty(routings) || routings.size() < ids.size())) {
            List<T> list = searchByIds(ids);
            return Response.ok(list);
        }
        return Response.ok(getRepository().getObjectById(null,routings,ids));
    }

    @ApiOperation(value = "根据id集合删除", notes = "根据id集合删除")
    @DeleteMapping
    public Response<Boolean> deleteByIds(@ApiParam(value = "ids", required = true) @RequestParam List<String> ids,
            @ApiParam(value = "路由键") @RequestParam(required = false) String routingKey
            , @ApiParam(value = "id一一对应的routings") @RequestParam(required = false) List<String> routings) throws Exception {
        Boolean bRet = false;
        try {
            if (StringUtils.isNotBlank(routingKey) && (CollectionUtils.isEmpty(routings) || routings.size() < ids.size())) {
                List<T> objects = searchByIds(ids);
                Field field = ReflectUtil.getField(routingKey,getTClass());
                field.setAccessible(true);
                routings = objects.stream().map(o-> {
                    try {
                        Object r = field.get(o);
                        if(null == r){
                            return null;
                        }
                        return r.toString();
                    } catch (IllegalAccessException e) {
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toList());
                ids = objects.stream().map(u -> u.getId()).collect(Collectors.toList());
            }
            bRet = getRepository().deleteByRoutingAndId(null,routings,ids);
        } catch (Exception e) {
            LOG.error("deleteByIds " + ids, e);
        }
        return Response.ok(bRet);
    }


    private List<T> searchByIds(List<String> ids) throws Exception {
        ElasticQuery query = new ElasticQuery();
        List<QueryItem> queryItems = Lists.newArrayList();
        QueryConverUtil.addQueryItem(queryItems, "_id", String.join(",", ids));
        query.setCurrentPage(1);
        query.setPageSize(CommonFields.PAGE_MAX_SIZE);
        query.setAndQuery(queryItems);
        ElasticPage<T> elasticPage = getRepository().searchPage(query);
        return elasticPage.getList();

    }

    @ApiOperation(value = "批量索引", notes = "批量索引")
    @PostMapping
    public Response<Boolean> save(@ApiParam(value = "对象列表", required = true) @RequestBody List<T> data) throws Exception {
        int size = (null == data) ? 0 : data.size();

        Boolean bRet = false;
        try {
            /* 初始化创建时间+更新时间 */
            Long now = DateTime.now().getMillis();
            for (T d : data) {
                if (null == d.getCreateAt()) {
                    d.setCreateAt(now);
                }
                d.getId();
                d.setUpdateAt(now);
            }

            bRet = getRepository().save(data);

        } catch (Exception e) {
            LOG.error("index " + data, e);
        }

        return Response.ok(bRet);
    }


    public Class<T> getTClass() {
        Class<T> entityClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return entityClass;
    }

    public abstract BaseSearchRepository<T> getRepository();


}
