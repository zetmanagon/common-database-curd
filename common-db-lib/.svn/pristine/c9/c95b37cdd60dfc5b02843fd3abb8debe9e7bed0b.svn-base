package com.gemantic.db.controller;

import com.gemantic.db.client.BaseDBClient;
import com.gemantic.db.constant.DBOperation;
import com.gemantic.db.model.BaseModel;
import com.gemantic.db.repository.BaseRepository;
import com.gemantic.db.support.DBQuery;
import com.gemantic.db.support.DBQueryItem;
import com.gemantic.db.support.DBUpdate;
import com.gemantic.db.util.DBUtil;
import com.gemantic.springcloud.model.PageResponse;
import com.gemantic.springcloud.model.Response;
import com.gemantic.springcloud.utils.StringUtil;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BaseController<T  extends BaseModel> implements BaseDBClient<T> {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    public static final List<String> DEFAULT_ONLY_INSERT_FIELD = Lists.newArrayList("id","createAt");


    @ApiOperation(value = "对象保存", notes = "对象保存")
    @PostMapping
    public Response<List<Long>> save(
            @ApiParam(value = "对象", required = true) @RequestBody List<T> data) throws Exception {
        if (CollectionUtils.isEmpty(data)) {
            return Response.ok(Lists.newArrayList());
        }
        Long now = DateTime.now().getMillis();
        for (T d : data) {
            if (null != d.getId() && d.getId() <= 0L) {
                d.setId(null);
            }
            if (null == d.getCreateAt() || null != d.getCreateAt() && d.getCreateAt() <= 0L) {
                d.setCreateAt(now);
            }
            d.setUpdateAt(now);
        }
        data = getRepository().saveAll(data);
        return Response.ok(data.stream().map(s -> s.getId()).collect(Collectors.toList()));
    }

    @ApiOperation(value = "根据编号删除", notes = "根据编号删除")
    @DeleteMapping
    public Response<Void> delete(
            @ApiParam(value = "编号集合", required = true) @RequestParam List<Long> ids)
            throws Exception {
        if (CollectionUtils.isNotEmpty(ids)) {
            DBQuery dbQuery = new DBQuery();
            dbQuery.setIds(ids);
            getRepository().deleteByQuery(dbQuery);
        }
        return Response.ok();
    }


    @ApiOperation(value = "简单查询删除", notes = "简单查询删除")
    @DeleteMapping("/delete/params")
    public Response<Integer> delete(@ApiParam(value = "时间范围字段") @RequestParam(required = false, defaultValue = "createAt")
                                                String timeField,
                                    @ApiParam(value = "查询起始时间") @RequestParam(required = false)
                                                Long startAt,
                                    @ApiParam(value = "查询结束时间") @RequestParam(required = false) Long endAt,
                                    @ApiParam(value = "like查询字段") @RequestParam(required = false)
                                                List<String> likeFields,
                                    @ApiParam(value = "like查询值") @RequestParam(required = false)
                                                List<String> likes,
                                    @ApiParam(value = "不切割的等值查询") @RequestParam(required = false)
                                                List<String> notSplitFields,
                                    @ApiParam(value = "其他参数") @RequestParam(required = false) Map<String, String> params,
                                    @ApiParam(value = "like的与或关系", allowableValues = "AND,OR") @RequestParam(required = false, defaultValue = "AND")
                                                String likeOperation,
                                    @ApiParam(value = "params的与或关系", allowableValues = "AND,OR") @RequestParam(required = false, defaultValue = "AND")
                                                String paramsOperation)
            throws Exception {
        excludeParams.forEach(s -> {
            if (params.containsKey(s)) {
                params.remove(s);
            }
        });
        DBQuery<Long> query = new DBQuery<Long>();
        List<DBQueryItem> andQuery = Lists.newArrayList();
        List<DBQueryItem> orQuery = Lists.newArrayList();
        List<DBQueryItem> paramQuery = Lists.newArrayList();
        List<DBQueryItem> likeQuery = Lists.newArrayList();

        if (null != startAt) {
            DBQueryItem startAtQuery = new DBQueryItem();
            startAtQuery.setValues(Lists.newArrayList(startAt));
            startAtQuery.setField(timeField);
            startAtQuery.setOperation(DBOperation.GTE);
            andQuery.add(startAtQuery);
        }
        if (null != endAt) {
            DBQueryItem endAtQuery = new DBQueryItem();
            endAtQuery.setValues(Lists.newArrayList(endAt));
            endAtQuery.setField(timeField);
            endAtQuery.setOperation(DBOperation.LTE);
            andQuery.add(endAtQuery);
        }
        if (CollectionUtils.isNotEmpty(likeFields) && CollectionUtils.isNotEmpty(likes) && likeFields.size() == likes.size()) {
            for (int i = 0; i < likeFields.size(); i++) {
                DBQueryItem dbQueryItem = new DBQueryItem();
                dbQueryItem.setField(likeFields.get(i));
                dbQueryItem.setValues(Lists.newArrayList(likes.get(i)));
                dbQueryItem.setOperation(DBOperation.LIKE);
                likeQuery.add(dbQueryItem);
            }
        }

        if (MapUtils.isNotEmpty(params)) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String value = StringUtil.trim(entry.getValue());
                if (StringUtils.isBlank(value)) {
                    continue;
                }
                String fieldName = entry.getKey();
                List<String> values = Lists.newArrayList(StringUtils.split(value, ","));
                if (CollectionUtils.isNotEmpty(notSplitFields) && notSplitFields.contains(fieldName)) {
                    values = Lists.newArrayList(value);
                }
                DBQueryItem dbQueryItem = new DBQueryItem();
                dbQueryItem.setField(fieldName);
                dbQueryItem.setValues(values);
                if (values.size() > 1) {
                    dbQueryItem.setOperation(DBOperation.IN);
                } else {
                    dbQueryItem.setOperation(DBOperation.EQ);
                }
                paramQuery.add(dbQueryItem);
            }
        }
        if (CollectionUtils.isNotEmpty(likeQuery)) {
            if ("OR".equalsIgnoreCase(likeOperation)) {
                orQuery.addAll(likeQuery);
            } else {
                andQuery.addAll(likeQuery);
            }
        }
        if (CollectionUtils.isNotEmpty(paramQuery)) {
            if ("OR".equalsIgnoreCase(paramsOperation)) {
                orQuery.addAll(paramQuery);
            } else {
                andQuery.addAll(paramQuery);
            }
        }
        query.setAndQuery(andQuery);
        query.setOrQuery(orQuery);

        return Response.ok(getRepository().deleteByQuery(query));
    }


    @ApiOperation(value = "根据编号查询", notes = "根据编号查询")
    @GetMapping("/{id}")
    public Response<T> getById(
            @ApiParam(value = "编号", required = true) @PathVariable Long id)
            throws Exception {
        return Response.ok(getRepository().getOne(id));
    }

    private static final List<String> excludeParams = Lists.newArrayList("paramsOperation", "likeOperation", "notSplitFields", "likeFields", "likes", "timeField", "startAt", "endAt", "orderBy", "direction", "ids", "cp", "ps");

    @ApiOperation(value = "简单条件查询", notes = "简单条件查询")
    @GetMapping
    public Response<PageResponse<T>> find(
            @ApiParam(value = "根据编号集合查询") @RequestParam(required = false)
                    List<Long> ids,
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
            @ApiParam(value = "like查询字段") @RequestParam(required = false)
                    List<String> likeFields,
            @ApiParam(value = "like查询值") @RequestParam(required = false)
                    List<String> likes,

            @ApiParam(value = "不切割的等值查询") @RequestParam(required = false)
                    List<String> notSplitFields,
            @ApiParam(value = "当前页") @RequestParam(required = false, defaultValue = "1") Integer cp,
            @ApiParam(value = "每页大小") @RequestParam(required = false, defaultValue = "10") Integer ps,
            @ApiParam(value = "其他参数") @RequestParam(required = false) Map<String, String> params, @ApiParam(value = "like的与或关系", allowableValues = "AND,OR") @RequestParam(required = false, defaultValue = "AND")
                    String likeOperation,
            @ApiParam(value = "params的与或关系", allowableValues = "AND,OR") @RequestParam(required = false, defaultValue = "AND")
                    String paramsOperation)
            throws Exception {

        LOG.info("params [{}]", params);

        DBQuery<Long> query = new DBQuery<Long>();
        if (CollectionUtils.isNotEmpty(ids)) {
            query.setIds(ids);
            PageResponse<T> pageResponse = getRepository().findByQuery(query);
            return Response.ok(pageResponse);
        }

        if (cp <= 0) {
            cp = 1;
        }
        excludeParams.forEach(s -> {
            if (params.containsKey(s)) {
                params.remove(s);
            }
        });
        List<DBQueryItem> andQuery = Lists.newArrayList();
        List<DBQueryItem> orQuery = Lists.newArrayList();
        List<DBQueryItem> paramQuery = Lists.newArrayList();
        List<DBQueryItem> likeQuery = Lists.newArrayList();

        if (null != startAt) {
            DBQueryItem startAtQuery = new DBQueryItem();
            startAtQuery.setValues(Lists.newArrayList(startAt));
            startAtQuery.setField(timeField);
            startAtQuery.setOperation(DBOperation.GTE);
            andQuery.add(startAtQuery);
        }
        if (null != endAt) {
            DBQueryItem endAtQuery = new DBQueryItem();
            endAtQuery.setValues(Lists.newArrayList(endAt));
            endAtQuery.setField(timeField);
            endAtQuery.setOperation(DBOperation.LTE);
            andQuery.add(endAtQuery);
        }
        if (CollectionUtils.isNotEmpty(likeFields) && CollectionUtils.isNotEmpty(likes) && likeFields.size() == likes.size()) {
            for (int i = 0; i < likeFields.size(); i++) {
                DBQueryItem dbQueryItem = new DBQueryItem();
                dbQueryItem.setField(likeFields.get(i));
                dbQueryItem.setValues(Lists.newArrayList(likes.get(i)));
                dbQueryItem.setOperation(DBOperation.LIKE);
                likeQuery.add(dbQueryItem);
            }
        }

        if (MapUtils.isNotEmpty(params)) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String value = StringUtil.trim(entry.getValue());
                if (StringUtils.isBlank(value)) {
                    continue;
                }
                String fieldName = entry.getKey();
                List<String> values = Lists.newArrayList(StringUtils.split(value, ","));
                if (CollectionUtils.isNotEmpty(notSplitFields) && notSplitFields.contains(fieldName)) {
                    values = Lists.newArrayList(value);
                }
                DBQueryItem dbQueryItem = new DBQueryItem();
                dbQueryItem.setField(fieldName);
                dbQueryItem.setValues(values);
                if (values.size() > 1) {
                    dbQueryItem.setOperation(DBOperation.IN);
                } else {
                    dbQueryItem.setOperation(DBOperation.EQ);
                }
                paramQuery.add(dbQueryItem);
            }
        }

        query.setCurrentPage(cp);
        query.setPageSize(ps);
        query.setSorts(DBUtil.getSortItems(orderBy, direction));
        if (CollectionUtils.isNotEmpty(likeQuery)) {
            if ("OR".equalsIgnoreCase(likeOperation)) {
                orQuery.addAll(likeQuery);
            } else {
                andQuery.addAll(likeQuery);
            }
        }
        if (CollectionUtils.isNotEmpty(paramQuery)) {
            if ("OR".equalsIgnoreCase(paramsOperation)) {
                orQuery.addAll(paramQuery);
            } else {
                andQuery.addAll(paramQuery);
            }
        }
        query.setAndQuery(andQuery);
        query.setOrQuery(orQuery);
        PageResponse<T> pageResponse = getRepository().findByQuery(query);
        return Response.ok(pageResponse);
    }


    @ApiOperation(value = "复杂条件查询", notes = "复杂条件查询")
    @PostMapping("/query")
    public Response<PageResponse<T>> query(@ApiParam(value = "查询对象", required = true) @RequestBody DBQuery query)
            throws Exception {
        if(MapUtils.isNotEmpty(query.getIncludeFields())){
            query.setFetch(Boolean.FALSE);
        }
        return Response.ok(getRepository().findByQuery(query));
    }


    @ApiOperation(value = "复杂条件查询,指定查询字段", notes = "复杂条件查询,指定查询字段")
    @PostMapping("/query/map")
    public Response<PageResponse<Map<String,Object>>> queryMap(@ApiParam(value = "查询对象", required = true) @RequestBody DBQuery query)
            throws Exception {
        query.setFetch(Boolean.FALSE);
        return Response.ok(getRepository().findByMapQuery(query));
    }


    @ApiOperation(value = "复杂条件删除", notes = "复杂条件删除")
    @PostMapping("/delete")
    public Response<Integer> deleteByQuery(@ApiParam(value = "查询对象", required = true) @RequestBody DBQuery<Long> query)
            throws Exception {
        query.setFetch(Boolean.FALSE);
        return Response.ok(getRepository().deleteByQuery(query));
    }


    @ApiOperation(value = "查询更新指定字段值", notes = "查询更新指定字段值")
    @PostMapping("/update")
    public Response<Integer> updateByQuery(@ApiParam(value = "查询对象", required = true) @RequestBody DBUpdate query)
            throws Exception {

        return Response.ok(getRepository().updateByQuery(query));
    }

    @ApiOperation(value = "批量插入对象,唯一键存在的记录不插入(内嵌对象仅支持ManyToOne)", notes = "批量插入对象,重复唯一键不插入 使用 INSERT IGNORE INTO ")
    @PostMapping("/bulk/insert")
    public Response<Void> bulkInsert(
            @ApiParam(value = "对象", required = true) @RequestBody List<T> data) throws Exception {
        if (CollectionUtils.isEmpty(data)) {
            return Response.ok();
        }
        Long now = DateTime.now().getMillis();
        for (T d : data) {
            if (null != d.getId() && d.getId() <= 0L) {
                d.setId(null);
            }
            if (null == d.getCreateAt() || null != d.getCreateAt() && d.getCreateAt() <= 0L) {
                d.setCreateAt(now);
            }
            d.setUpdateAt(now);
        }
        getRepository().bulkInsert(data);
        return Response.ok();
    }

    @ApiOperation(value = "批量保存对象-唯一键存在则更新,不存在插入 (内嵌对象仅支持ManyToOne)", notes = "批量保存对象唯一键存在更新,不存在插入 使用 INSERT INTO ... ON DUPLICATE KEY UPDATE")
    @PostMapping("/bulk/save")
    public Response<Void> bulkSave(
            @ApiParam(value = "对象", required = true) @RequestBody List<T> data) throws Exception {
        if (CollectionUtils.isEmpty(data)) {
            return Response.ok();
        }
        Long now = DateTime.now().getMillis();
        for (T d : data) {
            if (null != d.getId() && d.getId() <= 0L) {
                d.setId(null);
            }
            if (null == d.getCreateAt() || null != d.getCreateAt() && d.getCreateAt() <= 0L) {
                d.setCreateAt(now);
            }
            d.setUpdateAt(now);
        }
        List<String> onlyInsertField = getOnlyInsertField();
        if(CollectionUtils.isEmpty(onlyInsertField)){
            onlyInsertField = DEFAULT_ONLY_INSERT_FIELD;
        }
        getRepository().bulkSave(data,onlyInsertField);
        return Response.ok();
    }



    public Class<T> getTClass() {
        Class<T> entityClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return entityClass;
    }


    public abstract BaseRepository<T,Long> getRepository();

    public abstract List<String> getOnlyInsertField();

}
