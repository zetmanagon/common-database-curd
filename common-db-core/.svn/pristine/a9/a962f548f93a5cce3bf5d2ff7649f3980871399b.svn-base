package com.gemantic.db.support;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@ApiModel("数据库查询对象")
@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class DBQuery<ID> implements Serializable {
    private static final long serialVersionUID = 5053571207761529903L;

    @ApiModelProperty(value = "分页大小")
    private Integer pageSize = 10;
    @ApiModelProperty(value = "页数")
    private Integer currentPage = 1;

    @ApiModelProperty(value = "编号集合")
    private List<ID> ids;

    @ApiModelProperty(value = "and 查询")
    private List<DBQueryItem> andQuery;

    @ApiModelProperty(value = "or 查询")
    private List<DBQueryItem> orQuery;

    @ApiModelProperty(value = "排序字段")
    private List<DBSortItem> sorts;

    @ApiModelProperty(value = "是否查询级联对象",allowableValues = "true,false")
    private boolean fetch = Boolean.TRUE;

    @ApiModelProperty(value = "是否分页查询,传false则不统计总数",allowableValues = "true,false")
    private boolean page = Boolean.TRUE;

    @ApiModelProperty(value = "指定查询字段,key为查询字段名,value为映射字段值,value无值默认使用key")
    private Map<String,String> includeFields;


}
