package com.gemantic.search.client;

import com.gemantic.search.model.ElasticIndex;
import com.gemantic.search.model.ElasticPage;
import com.gemantic.search.support.query.ElasticQuery;
import com.gemantic.springcloud.model.Response;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


public interface BaseSearchClient<T extends ElasticIndex> {

    @ApiOperation(value = "对象保存", notes = "对象保存")
    @PostMapping
    Response<Boolean> save(
            @ApiParam(value = "对象", required = true) @RequestBody List<T> data) throws Exception;

    @ApiOperation(value = "根据条件检索", notes = "根据条件检索")
    @PostMapping("/query")
    Response<ElasticPage<T>> query
            (@ApiParam(value = "查询对象", required = true) @RequestBody ElasticQuery query) throws Exception ;

    @ApiOperation(value = "根据条件删除", notes = "根据条件删除")
    @PostMapping("/delete")
    Response<Void> deleteByQuery(@ApiParam(value = "查询对象", required = true) @RequestBody ElasticQuery query) throws Exception;


        @ApiOperation(value = "根据条件检索", notes = "根据条件检索")
    @GetMapping("/search")
    Response<ElasticPage<T>> search
            (@ApiParam(value = "根据编号集合查询") @RequestParam(required = false,value = "ids")
                     List<String> ids,
             @ApiParam(value = "游标编号") @RequestParam(required = false,value = "scrollId") String scrollId,
             @ApiParam(value = "时间范围字段") @RequestParam(required = false,value = "timeField", defaultValue = "createAt")
                     String timeField,
             @ApiParam(value = "查询起始时间") @RequestParam(required = false,value = "startAt")
                     Long startAt,
             @ApiParam(value = "查询结束时间") @RequestParam(required = false,value = "endAt") Long endAt,
             @ApiParam(value = "排序字段") @RequestParam(required = false,value = "orderBy")
                     List<String> orderBy,
             @ApiParam(value = "排序方向", allowableValues = "DESC,ASC")
             @RequestParam(required = false,value = "direction")
                     List<String> direction,
             @ApiParam(value = "scroll查询：false-否，true-是") @RequestParam(required = false,value = "scrollSearch", defaultValue = "false") Boolean scrollSearch,
             @ApiParam(value = "下一次游标滚屏时间间隔，毫秒") @RequestParam(required = false,value = "scrollTime", defaultValue = "60000") Long scrollTime,
             @ApiParam(value = "标红字段") @RequestParam(value = "plainHighlightedFields",required = false) List<String> plainHighlightedFields,
             @ApiParam(value = "标红起始tag样式") @RequestParam(value = "highlighterPreTags",required = false) String highlighterPreTags,
             @ApiParam(value = "标红结束tag样式") @RequestParam(value = "highlighterPostTags",required = false)String highlighterPostTags,
             @ApiParam(value = "指定获取字段") @RequestParam(value = "includeFields",required = false)List<String> includeFields,
             @ApiParam(value = "不获取字段") @RequestParam(value = "excludeFields",required = false)List<String> excludeFields,
             @ApiParam(value = "分组统计") @RequestParam(value = "groupBy",required = false)List<String> groupBy,
             @ApiParam(value = "当前页") @RequestParam(required = false,value = "cp", defaultValue = "1") Integer cp,
             @ApiParam(value = "每页大小") @RequestParam(required = false,value = "ps", defaultValue = "10") Integer ps,
             @ApiParam(value = "aggResults结果的页数") @RequestParam(value = "groupByCp",required = false) Integer groupByCp,
             @ApiParam(value = "aggResults每页大小") @RequestParam(value = "groupByPs",required = false) Integer groupByPs,
             @ApiParam(value = "aggResults前n个结果") @RequestParam(value = "groupByTopSize",required = false) Integer groupByTopSize,
             @ApiParam(value = "其他参数") @SpringQueryMap Map<String, String> params) throws Exception;

    @ApiOperation(value = "根据条件删除", notes = "根据条件删除")
    @DeleteMapping("/search")
    Response<Void> deleteBySearch
            (@ApiParam(value = "根据编号集合查询") @RequestParam(required = false,value = "ids")
                     List<String> ids,
             @ApiParam(value = "时间范围字段") @RequestParam(required = false,value = "timeField", defaultValue = "createAt")
                     String timeField,
             @ApiParam(value = "查询起始时间") @RequestParam(required = false,value = "startAt")
                     Long startAt,
             @ApiParam(value = "查询结束时间") @RequestParam(required = false,value = "endAt") Long endAt,
             @ApiParam(value = "同步/异步执行：true-异步,false-同步") @RequestParam(required = false,value = "async", defaultValue = "true") Boolean async,
             @ApiParam(value = "删除并行任务数") @RequestParam(required = false,value = "slices", defaultValue = "3") Integer slices,
             @ApiParam(value = "其他参数") @SpringQueryMap Map<String, String> params) throws Exception;


    @ApiOperation(value = "根据ids查询", notes = "根据id查询")
    @GetMapping
    Response<List<T>> getObjects(@ApiParam(value = "索引唯一键,编号", required = true) @RequestParam(value = "ids") List<String> ids
            , @ApiParam(value = "是否建立了routing:false-否；true-是") @RequestParam(required = false,value = "hasRouting",defaultValue = "false") Boolean hasRouting
            , @ApiParam(value = "id一一对应的routings") @RequestParam(value = "routings",required = false) List<String> routings) throws Exception;


    @ApiOperation(value = "根据id集合删除", notes = "根据id集合删除")
    @DeleteMapping
    Response<Boolean> deleteByIds(@ApiParam(value = "ids", required = true) @RequestParam(value = "ids") List<String> ids,
                                         @ApiParam(value = "路由键") @RequestParam(required = false,value = "routingKey") String routingKey
            , @ApiParam(value = "id一一对应的routings") @RequestParam(required = false,value = "routings") List<String> routings) throws Exception;

}
