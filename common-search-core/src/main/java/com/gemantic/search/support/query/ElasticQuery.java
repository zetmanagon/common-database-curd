package com.gemantic.search.support.query;

import com.gemantic.search.support.agg.TermAgg;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@ToString
@EqualsAndHashCode
public class ElasticQuery implements Serializable {
    private static final long serialVersionUID = 4034893944620369918L;

    @ApiModelProperty("当前页")
    private Integer currentPage = 1;

    @ApiModelProperty("页面大小")
    private Integer pageSize = 10;

    @ApiModelProperty("返回_source的字段：例如title, innerObject.*")
    private String[] includeFields;

    @ApiModelProperty("返回_source不包含字段")
    private String[] excludeFields;

    @ApiModelProperty(value = "是否返回_source数据",allowableValues = "true,false")
    private boolean fetchSource = Boolean.TRUE;


    @ApiModelProperty(value = "and query 查询")
    private List<QueryItem> andQuery;

    @ApiModelProperty(value = "or query 查询")
    private List<QueryItem> orQuery;

    @ApiModelProperty(value = "not query 查询")
    private List<QueryItem> notQuery;

    @ApiModelProperty(value = "and filter 查询")
    private List<QueryItem> andFilter;

    @ApiModelProperty(value = "or filter 查询")
    private List<QueryItem> orFilter;

    @ApiModelProperty(value = "not filter 查询")
    private List<QueryItem> notFilter;

    @ApiModelProperty(value = "span 查询")
    private List<QueryItem> spanQuery;

    @ApiModelProperty(value = "排序")
    private List<SortItem> sorts;

    @ApiModelProperty(value = "索引名称")
    private String index;


    @ApiModelProperty(value = "游标查询:true-是;false-否",allowableValues = "false,true")
    private boolean scrollSearch = false;

    @ApiModelProperty(value = "游标编号")
    private String scrollId;

    @ApiModelProperty(value = "路由键")
    private String[] routings;

    @ApiModelProperty(value = "折叠字段")
    private String collapseField;

    @ApiModelProperty(value = "游标查询间隔最长时间")
    private Long scrollTime = 60000L;

    @ApiModelProperty(value = "请求超时时间,单位：秒")
    private Integer requestTimeout = 60;

    @ApiModelProperty(value = "agg查询")
    private List<TermAgg> aggs;

    @ApiModelProperty(value = "返回高亮字段")
    private List<String> plainHighlightedFields;

    @ApiModelProperty(value = "高亮开始标签")
    private String highlighterPreTags = "<font color=\"red\">";
    @ApiModelProperty(value = "高亮结束标签")
    private String highlighterPostTags = "</font>";

    private int highlightFragmentSize = 100;

    private int highlightNumberOfFragments = 5;

    private boolean explain = false;

    @ApiModelProperty(value = "异步删除：true-是;false-否")
    private boolean async = Boolean.FALSE;

    /**
     * 查询删除每批次文档数
     */
    @ApiModelProperty(value = "异步删除每批次文档数")
    private Integer batchSize = 1000;

    @ApiModelProperty(value = "删除总记录数")
    private Integer deleteCount;

    /**
     * 任务并行数
     */
    @ApiModelProperty(value = "异步删除并行任务数")
    private Integer slices = 1;

    @ApiModelProperty(value = "最小得分")
    private Float minScore;



}
