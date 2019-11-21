package com.gemantic.search.support.query;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
@EqualsAndHashCode
public class SortItem implements Serializable{

    @ApiModelProperty(value = "字段名")
    private String field;

    @ApiModelProperty(value = "排序方向",allowableValues = "DESC,ASC")
    private String direction = "DESC";

    @ApiModelProperty(value = "该字段无值时默认字段类型设置")
    private String unmappedType = "long";
}
