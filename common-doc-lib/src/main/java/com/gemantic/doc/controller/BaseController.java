package com.gemantic.doc.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.gemantic.doc.client.BaseDocClient;
import com.gemantic.doc.constant.SearchType;
import com.gemantic.doc.model.BaseCollection;
import com.gemantic.doc.repository.BaseRepository;
import com.gemantic.doc.support.*;
import com.gemantic.springcloud.model.PageResponse;
import com.gemantic.springcloud.model.Response;
import com.gemantic.springcloud.utils.ConvertUtil;
import com.gemantic.springcloud.utils.ReflectUtil;
import com.gemantic.springcloud.utils.StringUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BaseController<T extends BaseCollection> implements BaseDocClient<T> {

    protected Logger LOG = LoggerFactory.getLogger(this.getClass());

    @ApiOperation(value = "对象保存", notes = "对象保存")
    @PostMapping
    public Response<Void> save(
            @ApiParam(value = "对象", required = true) @RequestBody List<T> data) throws Exception {
        if (CollectionUtils.isEmpty(data)) {
            return Response.ok();
        }
        Long now = DateTime.now().getMillis();
        for (T d : data) {

            if (null == d.getCreateAt() || null != d.getCreateAt() && d.getCreateAt() <= 0L) {
                d.setCreateAt(now);
            }
            d.setUpdateAt(now);
        }
        getRepository().saveList(data);
        return Response.ok();
    }

    @ApiOperation(value = "批量插入对象,重复id不插入", notes = "批量插入对象,重复id不插入")
    @PostMapping("/bulk/insert")
    public Response<Void> bulkInsert(
            @ApiParam(value = "对象", required = true) @RequestBody List<T> data) throws Exception {
        if (CollectionUtils.isEmpty(data)) {
            return Response.ok();
        }
        Long now = DateTime.now().getMillis();
        for (T d : data) {

            if (null == d.getCreateAt() || null != d.getCreateAt() && d.getCreateAt() <= 0L) {
                d.setCreateAt(now);
            }
            d.setUpdateAt(now);
        }
        getRepository().bulkInsert(data);
        return Response.ok();
    }

    @ApiOperation(value = "批量更新对象", notes = "批量更新对象")
    @PostMapping("/bulk/update")
    public Response<Void> bulkUpdate(
            @ApiParam(value = "对象", required = true) @RequestBody List<T> data) throws Exception {
        if (CollectionUtils.isEmpty(data)) {
            return Response.ok();
        }
        Long now = DateTime.now().getMillis();
        for (T d : data) {

            if (null == d.getCreateAt() || null != d.getCreateAt() && d.getCreateAt() <= 0L) {
                d.setCreateAt(now);
            }
            d.setUpdateAt(now);
        }
        getRepository().bulkUpdate(data);
        return Response.ok();
    }

    @ApiOperation(value = "简单查询更新", notes = "简单查询更新")
    @PostMapping("/update")
    public Response<Void> update(
            @ApiParam(value = "编号集合") @RequestParam(required = false) List<String> ids,
            @ApiParam(value = "时间范围字段") @RequestParam(required = false, defaultValue = "createAt")
                    String timeField,
            @ApiParam(value = "查询起始时间") @RequestParam(required = false)
                    Long startAt,
            @ApiParam(value = "查询结束时间") @RequestParam(required = false) Long endAt,
            @ApiParam(value = "like查询字段") @RequestParam(required = false)
                    List<String> likeFields,
            @ApiParam(value = "like查询值") @RequestParam(required = false)
                    List<String> likes,
            @ApiParam(value = "其他参数") @RequestParam(required = false) Map<String, String> params,
            @ApiParam(value = "更新的field") @RequestParam(required = false) List<String> updateKeys,
            @ApiParam(value = "更新的value") @RequestParam(required = false) List<String> updateValues,
            @ApiParam(value = "集合属性移除满足条件的对象") @RequestParam(required = false) List<String> unsetFields,
            @ApiParam(value = "集合添加对象的属性名") @RequestParam(required = false) String addToSet

    ) throws Exception {
        CollectionQuery collectionQuery = getCollectionQuery(ids, timeField, startAt, endAt, null, null, likeFields, likes, params, Boolean.TRUE);
        Map<String, List<Object>> add2SetMap = Maps.newHashMap();

        if (StringUtils.isNotBlank(addToSet)) {
            TypeReference<Map<String, List<Object>>> typeReference = new TypeReference<Map<String, List<Object>>>() {
            };
            add2SetMap = JSON.parseObject(addToSet, typeReference);
        }
        Map<String,String> updateFields = Maps.newHashMap();
        if(CollectionUtils.isNotEmpty(updateKeys) && CollectionUtils.isNotEmpty(updateValues) && updateKeys.size() == updateValues.size()){
            for(int i = 0;i<updateKeys.size();i++){
                String field = updateKeys.get(i);
                String value = updateValues.get(i);
                updateFields.put(field,value);
            }
        }
        CollectionUpdate updateObj = new CollectionUpdate();
        updateObj.setUpdateFields(updateFields);
        updateObj.setIds(collectionQuery.getIds());
        updateObj.setAdd2SetObjects(add2SetMap);
        updateObj.setUnsetFields(unsetFields);
        updateObj.setAndQuery(collectionQuery.getAndQuery());
        updateObj.setOrQuery(collectionQuery.getOrQuery());
        updateObj.setNotQuery(collectionQuery.getNotQuery());
        LOG.info("update by [{}] ", updateObj);
        getRepository().update(updateObj);
        return Response.ok();
    }


    @ApiOperation(value = "复杂查询更新", notes = "复杂查询更新")
    @PostMapping("/update/query")
    public Response<Void> updateByQuery(@ApiParam(value = "查询对象", required = true) @RequestBody CollectionUpdate updateObj) throws Exception {
        getRepository().update(updateObj);
        return Response.ok();
    }

    @ApiOperation(value = "简单查询条件删除", notes = "简单查询条件删除")
    @DeleteMapping
    public Response<Void> delete(
            @ApiParam(value = "编号集合") @RequestParam(required = false) List<String> ids,
            @ApiParam(value = "时间范围字段") @RequestParam(required = false, defaultValue = "createAt")
                    String timeField,
            @ApiParam(value = "查询起始时间") @RequestParam(required = false)
                    Long startAt,
            @ApiParam(value = "查询结束时间") @RequestParam(required = false) Long endAt,
            @ApiParam(value = "like查询字段") @RequestParam(required = false)
                    List<String> likeFields,
            @ApiParam(value = "like查询值") @RequestParam(required = false)
                    List<String> likes,
            @ApiParam(value = "其他参数") @RequestParam(required = false) Map<String, String> params)
            throws Exception {

        CollectionQuery collectionQuery = getCollectionQuery(ids, timeField, startAt, endAt, null, null, likeFields, likes, params, Boolean.TRUE);
        LOG.info("delete by [{}] ", collectionQuery);
        getRepository().delete(collectionQuery);

        return Response.ok();
    }


    @ApiOperation(value = "复杂查询条件删除", notes = "复杂查询条件删除")
    @PostMapping("/delete")
    public Response<Void> deleteByQuery(@ApiParam(value = "查询对象", required = true) @RequestBody CollectionQuery query)
            throws Exception {
        LOG.info("delete by [{}] ", query);
        getRepository().delete(query);
        return Response.ok();
    }


    @ApiOperation(value = "根据编号查询", notes = "根据编号查询")
    @GetMapping("/{id}")
    public Response<T> getById(
            @ApiParam(value = "编号", required = true) @PathVariable String id)
            throws Exception {
        return Response.ok(getRepository().getOne(id));
    }

    private static final List<String> excludeParams = Lists.newArrayList("queryHint", "addToSet", "unwind", "unsetFields", "groupBy", "updateKeys", "updateValues", "page", "likeFields", "likes", "timeField", "startAt", "endAt", "orderBy", "direction", "ids", "cp", "ps");

    @ApiOperation(value = "简单条件查询", notes = "简单条件查询")
    @GetMapping
    public Response<PageResponse<T>> find(
            @ApiParam(value = "编号集合") @RequestParam(value = "ids", required = false) List<String> ids,
            @ApiParam(value = "时间范围字段") @RequestParam(value = "timeField", required = false, defaultValue = "createAt")
                    String timeField,
            @ApiParam(value = "查询起始时间") @RequestParam(value = "startAt", required = false)
                    Long startAt,
            @ApiParam(value = "查询结束时间") @RequestParam(value = "endAt", required = false) Long endAt,
            @ApiParam(value = "like查询字段") @RequestParam(value = "likeFields", required = false)
                    List<String> likeFields,
            @ApiParam(value = "like查询值") @RequestParam(value = "likes", required = false)
                    List<String> likes,
            @ApiParam(value = "是否返回分页信息") @RequestParam(value = "page", required = false, defaultValue = "true")
                    Boolean page,
            @ApiParam(value = "排序字段") @RequestParam(value = "orderBy", required = false)
                    List<String> orderBy,

            @ApiParam(value = "排序方向", allowableValues = "DESC,ASC")
            @RequestParam(value = "direction", required = false)
                    List<String> direction,

            @ApiParam(value = "当前页") @RequestParam(value = "cp", required = false, defaultValue = "1") Integer cp,
            @ApiParam(value = "每页大小") @RequestParam(value = "ps", required = false, defaultValue = "10") Integer ps,
            @ApiParam(value = "其他参数") @RequestParam Map<String, String> params
            , @ApiParam(value = "指定mongo使用的索引") @RequestParam(value = "queryHint", required = false)
                    String queryHint)
            throws Exception {

        LOG.info("params:" + params);

        if (cp <= 0) {
            cp = 1;
        }
        CollectionQuery collectionQuery = getCollectionQuery(ids, timeField, startAt, endAt, orderBy, direction, likeFields, likes, params, Boolean.TRUE);
        collectionQuery.setQueryHint(queryHint);
        collectionQuery.setCurrentPage(cp);
        collectionQuery.setPageSize(ps);
        collectionQuery.setQueryHint(queryHint);
        if (LOG.isDebugEnabled()) {
            LOG.debug(collectionQuery + "");
        }
        PageResponse<T> pageResponse = null;
        if (page) {
            pageResponse = getRepository().searchPage(collectionQuery);

        } else {
            pageResponse = new PageResponse<T>();
            List<T> list = getRepository().search(collectionQuery);
            pageResponse.setPageSize(ps);
            pageResponse.setCurrentPage(cp);
            pageResponse.setList(list);

        }
        return Response.ok(pageResponse);
    }


    @ApiOperation(value ="复杂条件查询", notes = "复杂条件查询")
    @PostMapping("/search")
    public Response<PageResponse<T>> search
            (@ApiParam(value = "查询对象", required = true) @RequestBody CollectionQuery collectionQuery) throws Exception {
        PageResponse<T> pageResponse = null;
        if (collectionQuery.isPage()) {
            pageResponse = getRepository().searchPage(collectionQuery);

        } else {
            pageResponse = new PageResponse<T>();
            List<T> list = getRepository().search(collectionQuery);
            pageResponse.setPageSize(collectionQuery.getPageSize());
            pageResponse.setCurrentPage(collectionQuery.getCurrentPage());
            pageResponse.setList(list);
        }
        return Response.ok(pageResponse);
    }


    @ApiOperation(value = "复杂分组统计", notes = "复杂分组统计")
    @PostMapping("/group")
    public Response<List<CollectionGroupResult>> group(@ApiParam(value = "查询对象", required = true) @RequestBody CollectionQuery query)
            throws Exception {

        if(CollectionUtils.isEmpty(query.getGroupBy())){
            return Response.error(Lists.newArrayList());
        }
        return Response.ok(getRepository().group(query));
    }



    @ApiOperation(value = "简单分组统计", notes = "简单分组统计")
    @GetMapping("/group")
    public Response<List<CollectionGroupResult>> group(
            @ApiParam(value = "根据编号集合查询") @RequestParam(required = false)
                    List<String> ids,
            @ApiParam(value = "时间范围字段") @RequestParam(required = false, defaultValue = "createAt")
                    String timeField,
            @ApiParam(value = "查询起始时间") @RequestParam(required = false)
                    Long startAt,
            @ApiParam(value = "查询结束时间") @RequestParam(required = false) Long endAt,

            @ApiParam(value = "like查询字段") @RequestParam(required = false)
                    List<String> likeFields,
            @ApiParam(value = "like查询值") @RequestParam(required = false)
                    List<String> likes,
            @ApiParam(value = "分组字段", required = true) @RequestParam
                    List<String> groupBy,
            @ApiParam(value = "数组拆分字段名") @RequestParam(required = false)
                    String unwind,
            @ApiParam(value = "其他参数") @RequestParam(required = false) Map<String, String> params)
            throws Exception {

        LOG.info("params:" + params);
        boolean hasChildren = Boolean.TRUE;
        if (StringUtils.isNotBlank(unwind)) {
            hasChildren = Boolean.FALSE;
        }
        CollectionQuery collectionQuery = getCollectionQuery(ids, timeField, startAt, endAt, null, null, likeFields, likes, params, hasChildren);

        if (LOG.isDebugEnabled()) {
            LOG.debug(collectionQuery + "");
        }
        collectionQuery.setGroupBy(groupBy);
        collectionQuery.setUnwind(unwind);
        return Response.ok(getRepository().group(collectionQuery));
    }


    private CollectionQuery getCollectionQuery(
            List<String> ids, String timeField,
            Long startAt,
            Long endAt,
            List<String> orderBy,
            List<String> direction,
            List<String> likeFields,
            List<String> likes,
            Map<String, String> params, Boolean hasChildren) {
        excludeParams.forEach(s -> {
            if (params.containsKey(s)) {
                params.remove(s);
            }
        });


        CollectionQuery collectionQuery = new CollectionQuery();
        collectionQuery.setIds(ids);
        List<CollectionQueryItem> andQuery = Lists.newArrayList();
        if (null != startAt || null != endAt) {
            CollectionQueryItem queryItem = new CollectionQueryItem();
            queryItem.setField(timeField);
            queryItem.setValueStartAt(startAt);
            queryItem.setValueEndAt(endAt);
            andQuery.add(queryItem);
        }

        if (CollectionUtils.isNotEmpty(likeFields) && CollectionUtils.isNotEmpty(likes) && likeFields.size() == likes.size()) {
            for (int i = 0; i < likeFields.size(); i++) {
                CollectionQueryItem queryItem = new CollectionQueryItem();
                queryItem.setField(likeFields.get(i));
                queryItem.setValues(Lists.newArrayList(likes.get(i)));
                queryItem.setSearchType(SearchType.LIKE);
                andQuery.add(queryItem);
            }
        }

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String value = StringUtil.trim(entry.getValue().toString());
            if (StringUtils.isBlank(value)) {
                continue;
            }
            String fieldName = entry.getKey();
            CollectionQueryItem queryItem = new CollectionQueryItem();
            List<String> values = Lists.newArrayList(StringUtils.split(value, ","));
            queryItem.setValues(values);
            if (!fieldName.contains(".")) {
                Field field = ReflectUtil.getField(fieldName, getTClass());
                switch (field.getType().getSimpleName()) {
                    case "Long":
                        List<Long> valuesConvert = values.stream().map(s -> Long.valueOf(s)).collect(Collectors.toList());
                        queryItem.setValues(valuesConvert);
                        break;
                    case "BigDecimal":
                        List<BigDecimal> valuesConvert2 = values.stream().map(s -> new BigDecimal(s)).collect(Collectors.toList());
                        queryItem.setValues(valuesConvert2);
                        break;
                    case "Double":
                        List<Double> valuesConvert3 = values.stream().map(s -> Double.valueOf(s)).collect(Collectors.toList());
                        queryItem.setValues(valuesConvert3);
                        break;
                }
            }
            queryItem.setField(fieldName);
            andQuery.add(queryItem);
        }

        List<Sort.Order> orders = ConvertUtil.convertOrders(orderBy, direction);
        List<CollectionSortItem> sorts = Lists.newArrayList();
        for (Sort.Order order : orders) {
            CollectionSortItem sort = new CollectionSortItem();
            sort.setField(order.getProperty());
            sort.setDirection(order.getDirection().name());
            sorts.add(sort);
        }
        if (hasChildren) {
            collectionQuery.setAndQuery(buildChildren(andQuery));
        } else {
            collectionQuery.setAndQuery(andQuery);
        }

        collectionQuery.setSorts(sorts);
        return collectionQuery;

    }


    private List<CollectionQueryItem> buildChildren(List<CollectionQueryItem> queryItems) {
        List<CollectionQueryItem> flat = Lists.newArrayList();
        for (CollectionQueryItem queryItem : queryItems) {
            String itemfield = queryItem.getField();
            if (!itemfield.contains(".")) {
                flat.add(queryItem);
                continue;
            }
            String[] fields = StringUtils.splitByWholeSeparator(itemfield, ".");
            CollectionQueryItem mainQuery = null;
            CollectionQueryItem parentQuery = null;
            Class classz = getTClass();
            List<String> fieldNames = Lists.newArrayList();
            for (int i = 0; i < fields.length; i++) {
                String fieldName = fields[i];
                fieldNames.add(fieldName);
                Field field = ReflectUtil.getField(fieldName, classz);
                if (!field.getType().getSimpleName().equalsIgnoreCase("List")) {
                    Class subClass = null;
                    if (field.getType().getSimpleName().equalsIgnoreCase("Map")) {
                        ParameterizedType mapType = (ParameterizedType) field.getGenericType();
                        subClass = (Class<?>) mapType.getActualTypeArguments()[1];
                    } else {
                        subClass = field.getType();
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(" query {} {} {} {}", classz.getSimpleName(), fieldName, field.getType().getSimpleName(), subClass);
                    }
                    if (null != subClass) {
                        classz = subClass;
                    }
                    if (i >= fields.length - 1) {
                        CollectionQueryItem children = new CollectionQueryItem();
                        BeanUtils.copyProperties(queryItem, children);
                        children.setField(String.join(".", fieldNames));
                        parentQuery.setAndChildren(Lists.newArrayList(children));
                        break;
                    } else {
                        continue;
                    }
                }
                ParameterizedType listType = (ParameterizedType) field.getGenericType();
                Class<?> listClass = (Class<?>) listType.getActualTypeArguments()[0];
                if (LOG.isDebugEnabled()) {
                    LOG.debug("list query {} {} {}", classz.getSimpleName(), fieldName, listClass);
                }
                classz = listClass;
                if (null == mainQuery) {
                    mainQuery = new CollectionQueryItem();
                    mainQuery.setField(String.join(".", fieldNames));
                    fieldNames = Lists.newArrayList();
                    parentQuery = mainQuery;
                    continue;
                }

                CollectionQueryItem children = new CollectionQueryItem();
                if (i >= fields.length - 1) {
                    BeanUtils.copyProperties(queryItem, children);
                }
                children.setField(String.join(".", fieldNames));
                fieldNames = Lists.newArrayList();
                parentQuery.setAndChildren(Lists.newArrayList(children));
                parentQuery = children;
            }
            flat.add(mainQuery);
        }
        return mergeChildren(flat);

    }


    private List<CollectionQueryItem> mergeChildren(List<CollectionQueryItem> data) {
        List<CollectionQueryItem> result = data.stream().filter(r -> CollectionUtils.isEmpty(r.getAndChildren())).collect(Collectors.toList());
        Map<String, List<CollectionQueryItem>> groupby = data.stream().filter(r -> CollectionUtils.isNotEmpty(r.getAndChildren())).collect(Collectors.groupingBy(CollectionQueryItem::getField));
        if (MapUtils.isEmpty(groupby)) {
            return result;
        }
        for (Map.Entry<String, List<CollectionQueryItem>> entry : groupby.entrySet()) {
            CollectionQueryItem parent = entry.getValue().get(0);
            parent.setAndChildren(mergeChildren(entry.getValue().stream().flatMap(s -> s.getAndChildren().stream()).collect(Collectors.toList())));
            result.add(parent);
        }
        return result;
    }


    public Class<T> getTClass() {
        Class<T> entityClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return entityClass;
    }

    public abstract BaseRepository<T> getRepository();
}
