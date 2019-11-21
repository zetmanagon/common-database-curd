package com.gemantic.db.client;

import com.gemantic.db.support.DBQuery;
import com.gemantic.db.support.DBUpdate;
import com.gemantic.springcloud.model.PageResponse;
import com.gemantic.springcloud.model.Response;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


public interface BaseDBClient<T> {

    @ApiOperation(value = "对象保存", notes = "对象保存")
    @PostMapping
    Response<List<Long>> save(
            @ApiParam(value = "对象", required = true) @RequestBody List<T> data) throws Exception;

    @ApiOperation(value = "根据编号删除", notes = "根据编号删除")
    @DeleteMapping
    Response<Void> delete(@ApiParam(value = "编号集合", required = true) @RequestParam("ids") List<Long> ids) throws Exception;

    @ApiOperation(value = "根据查询删除", notes = "根据查询删除")
    @DeleteMapping("/delete/params")
    Response<Integer> delete(@ApiParam(value = "时间范围字段") @RequestParam(value = "timeField", required = false, defaultValue = "createAt")
                                     String timeField,
                             @ApiParam(value = "查询起始时间") @RequestParam(value = "startAt", required = false)
                                     Long startAt,
                             @ApiParam(value = "查询结束时间") @RequestParam(value = "endAt", required = false) Long endAt,
                             @ApiParam(value = "like查询字段") @RequestParam(value = "likeFields", required = false)
                                     List<String> likeFields,
                             @ApiParam(value = "like查询值") @RequestParam(value = "likes", required = false)
                                     List<String> likes,
                             @ApiParam(value = "不切割的等值查询") @RequestParam(value = "notSplitFields", required = false)
                                     List<String> notSplitFields,
                             @ApiParam(value = "其他参数") @SpringQueryMap Map<String, String> params,
                             @ApiParam(value = "like的与或关系", allowableValues = "AND,OR") @RequestParam(value = "likeOperation", required = false, defaultValue = "AND")
                                     String likeOperation,
                             @ApiParam(value = "params的与或关系", allowableValues = "AND,OR") @RequestParam(value = "paramsOperation", required = false, defaultValue = "AND")
                                     String paramsOperation)
            throws Exception;

    @ApiOperation(value = "根据条件查询删除", notes = "根据条件查询删除")
    @PostMapping("/delete")
    Response<Integer> deleteByQuery(@ApiParam(value = "查询对象", required = true) @RequestBody DBQuery<Long> query)
            throws Exception;

    @ApiOperation(value = "根据编号查询", notes = "根据编号查询")
    @GetMapping("/{id}")
    Response<T> getById(
            @ApiParam(value = "编号", required = true) @PathVariable("id") Long id)
            throws Exception;

    @ApiOperation(value = "根据条件查询", notes = "根据条件查询")
    @GetMapping
    Response<PageResponse<T>> find(
            @ApiParam(value = "根据编号集合查询") @RequestParam(value = "ids", required = false)
                    List<Long> ids,
            @ApiParam(value = "时间范围字段") @RequestParam(value = "timeField", required = false, defaultValue = "createAt")
                    String timeField,
            @ApiParam(value = "查询起始时间") @RequestParam(value = "startAt", required = false)
                    Long startAt,
            @ApiParam(value = "查询结束时间") @RequestParam(value = "endAt", required = false) Long endAt,
            @ApiParam(value = "排序字段") @RequestParam(value = "orderBy", required = false)
                    List<String> orderBy,
            @ApiParam(value = "排序方向", allowableValues = "DESC,ASC")
            @RequestParam(value = "direction", required = false)
                    List<String> direction,
            @ApiParam(value = "like查询字段") @RequestParam(value = "likeFields", required = false)
                    List<String> likeFields,
            @ApiParam(value = "like查询值") @RequestParam(value = "likes", required = false)
                    List<String> likes,
            @ApiParam(value = "不切割的等值查询") @RequestParam(value = "notSplitFields", required = false)
                    List<String> notSplitFields,
            @ApiParam(value = "当前页") @RequestParam(value = "cp", required = false, defaultValue = "1") Integer cp,
            @ApiParam(value = "每页大小") @RequestParam(value = "ps", required = false, defaultValue = "10") Integer ps,
            @ApiParam(value = "其他参数") @SpringQueryMap Map<String, String> params,
            @ApiParam(value = "like的与或关系", allowableValues = "AND,OR") @RequestParam(value = "likeOperation", required = false, defaultValue = "AND")
                    String likeOperation,
            @ApiParam(value = "params的与或关系", allowableValues = "AND,OR") @RequestParam(value = "paramsOperation", required = false, defaultValue = "AND")
                    String paramsOperation)
            throws Exception;

    @ApiOperation(value = "复杂条件查询", notes = "复杂条件查询")
    @PostMapping("/query")
    Response<PageResponse<T>> query(@ApiParam(value = "查询对象", required = true) @RequestBody DBQuery query)
            throws Exception;


    @ApiOperation(value = "复杂条件查询,指定查询字段,返回map", notes = "复杂条件查询,指定查询字段，返回map")
    @PostMapping("/query/map")
    Response<PageResponse<Map<String,Object>>> queryMap(@ApiParam(value = "查询对象", required = true) @RequestBody DBQuery query)
            throws Exception;


    @ApiOperation(value = "根据条件查询更新指定字段值", notes = "根据条件查询更新指定字段值")
    @PostMapping("/update")
    Response<Integer> updateByQuery(@ApiParam(value = "查询对象", required = true) @RequestBody DBUpdate query)
            throws Exception;

    @ApiOperation(value = "批量插入对象,重复唯一键不插入,内嵌对象仅支持多对一的对象id插入", notes = "批量插入对象,重复唯一键不插入 使用 INSERT IGNORE INTO ")
    @PostMapping("/bulk/insert")
    Response<Void> bulkInsert(
            @ApiParam(value = "对象", required = true) @RequestBody List<T> data) throws Exception;


    @ApiOperation(value = "批量保存对象-唯一键存在更新,不存在插入,内嵌对象仅支持多对一的对象id插入", notes = "批量保存对象唯一键存在更新,不存在插入 使用 INSERT INTO ... ON DUPLICATE KEY UPDATE")
    @PostMapping("/bulk/save")
    Response<Void> bulkSave(
            @ApiParam(value = "对象", required = true) @RequestBody List<T> data) throws Exception;


}
