package com.gemantic.search.model;

import com.gemantic.search.support.agg.TermAggResult;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@ToString
@EqualsAndHashCode
public class ElasticPage<T extends ElasticIndex> implements Serializable {

    private static final long serialVersionUID = -4645705123327576146L;

    @ApiModelProperty("当前页")
    private Integer currentPage = 1;

    @ApiModelProperty("每页大小")
    private Integer pageSize = 10;

    @ApiModelProperty("elastic最高得分")
    private float elasticMaxScore;

    @ApiModelProperty("总记录数")
    private Long totalCount;

    @ApiModelProperty("总页数")
    private Integer totalPage;

    @ApiModelProperty("记录数据")
    private List<T> list;

    @ApiModelProperty("滚动编号")
    private String scrollId;

    @ApiModelProperty("agg结果")
    private List<TermAggResult> aggResults;
}