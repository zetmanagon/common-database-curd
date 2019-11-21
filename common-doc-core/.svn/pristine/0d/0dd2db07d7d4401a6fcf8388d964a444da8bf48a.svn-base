package com.gemantic.doc.support;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@ApiModel("查询对象")
@Data
@ToString
@EqualsAndHashCode
public class CollectionQuery implements Serializable {
    private static final long serialVersionUID = 4034893944620369918L;
    @ApiModelProperty(value = "页面大小")
    private Integer pageSize = 10;
    @ApiModelProperty(value = "页号")
    private Integer currentPage = 1;

    @ApiModelProperty(value = "是否分页查询",allowableValues = "true,false")
    private boolean page = Boolean.TRUE;

    @ApiModelProperty(value = "查询索引(count的时候需要用这个提高性能)")
    private String queryHint;

    @ApiModelProperty(value = "指定查询字段")
    private List<String> includeFields;

    @ApiModelProperty(value = "记录编号集合")
    private List<String> ids;
    @ApiModelProperty(value = "and 查询")
    private List<CollectionQueryItem> andQuery;
    @ApiModelProperty(value = "or 查询")
    private List<CollectionQueryItem> orQuery;
    @ApiModelProperty(value = "not 查询")
    private List<CollectionQueryItem> notQuery;
    @ApiModelProperty(value = "排序")
    private List<CollectionSortItem> sorts;

    @ApiModelProperty(value = "分组字段")
    private List<String> groupBy;

    @ApiModelProperty(value = "属性数组打平统计的字段")
    private String unwind;
}
