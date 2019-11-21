package com.gemantic.doc.client;

import com.gemantic.doc.support.CollectionGroupResult;
import com.gemantic.doc.support.CollectionQuery;
import com.gemantic.doc.support.CollectionUpdate;
import com.gemantic.springcloud.model.PageResponse;
import com.gemantic.springcloud.model.Response;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


public interface BaseDocClient<T> {

    @ApiOperation(value = "对象保存", notes = "对象保存")
    @PostMapping
    Response<Void> save(
            @ApiParam(value = "对象", required = true) @RequestBody List<T> data) throws Exception;

    @ApiOperation(value = "批量插入", notes = "批量插入,重复跳过")
    @PostMapping("/bulk/insert")
    Response<Void> bulkInsert(
            @ApiParam(value = "对象", required = true) @RequestBody List<T> data) throws Exception;

    @ApiOperation(value = "批量更新记录", notes = "批量更新记录:没有插入重复更新")
    @PostMapping("/bulk/update")
    Response<Void> bulkUpdate(
            @ApiParam(value = "对象", required = true) @RequestBody List<T> data) throws Exception;


    @ApiOperation(value = "查询更新字段", notes = "查询更新字段")
    @PostMapping("/update")
    Response<Void> update(
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
            @ApiParam(value = "其他参数") @SpringQueryMap Map<String, String> params,
            @ApiParam(value = "更新的field", required = true) @RequestParam("updateKeys") List<String> updateKeys,
            @ApiParam(value = "更新的value", required = true) @RequestParam("updateValues") List<String> updateValues,
            @ApiParam(value = "集合属性移除满足条件的对象") @RequestParam(value = "unsetFields", required = false) List<String> unsetFields
            , @ApiParam(value = "集合属性添加对象") @RequestParam(value = "addToSet", required = false) String addToSet


    ) throws Exception;

    @ApiOperation(value = "根据查询条件删除", notes = "根据编号删除")
    @DeleteMapping
    Response<Void> delete(
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
            @ApiParam(value = "其他参数") @SpringQueryMap Map<String, String> params

    ) throws Exception;

    @ApiOperation(value = "复杂查询条件删除", notes = "复杂查询条件删除")
    @PostMapping("/delete")
    Response<Void> deleteByQuery(@ApiParam(value = "查询对象", required = true) @RequestBody CollectionQuery query)
            throws Exception;

    @ApiOperation(value = "根据编号查询", notes = "根据编号查询")
    @GetMapping("/{id}")
    Response<T> getById(
            @ApiParam(value = "编号", required = true) @PathVariable("id") String id)
            throws Exception;

    @ApiOperation(value = "根据条件查询", notes = "根据条件查询")
    @GetMapping
    Response<PageResponse<T>> find(
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
            @ApiParam(value = "其他参数") @SpringQueryMap Map<String, String> params
            , @ApiParam(value = "指定mongo使用的索引") @RequestParam(value = "queryHint", required = false)
                    String queryHint)
            throws Exception;


    @ApiOperation(value = "根据条件检索", notes = "根据条件检索")
    @PostMapping("/search")
    Response<PageResponse<T>> search
            (@ApiParam(value = "查询对象", required = true) @RequestBody CollectionQuery collectionQuery) throws Exception;

    @ApiOperation(value = "复杂查询更新", notes = "复杂查询更新")
    @PostMapping("/update/query")
    Response<Void> updateByQuery(@ApiParam(value = "查询对象", required = true) @RequestBody CollectionUpdate updateObj) throws Exception;


    @ApiOperation(value = "简单查询分组统计", notes = "简单查询分组统计")
    @GetMapping("/group")
    Response<List<CollectionGroupResult>> group(
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
            @ApiParam(value = "分组字段", required = true) @RequestParam("groupBy")
                    List<String> groupBy,
            @ApiParam(value = "数组拆分字段名") @RequestParam(value = "unwind", required = false)
                    String unwind,
            @ApiParam(value = "其他参数") @SpringQueryMap Map<String, String> params)
            throws Exception;


    @ApiOperation(value = "复杂分组统计", notes = "复杂分组统计")
    @PostMapping("/group")
    Response<List<CollectionGroupResult>> group(@ApiParam(value = "查询对象", required = true) @RequestBody CollectionQuery query)
            throws Exception;
}
