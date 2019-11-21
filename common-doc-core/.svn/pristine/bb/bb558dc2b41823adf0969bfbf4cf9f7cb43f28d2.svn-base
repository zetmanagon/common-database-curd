package com.gemantic.doc.support;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

@EqualsAndHashCode
@ApiModel("分组统计结果")
@Data
@ToString
public class CollectionGroupResult implements Serializable {
    private static final long serialVersionUID = 9034131807462939969L;
    @ApiModelProperty(value = "分组字段及对应字段值")
    private Map<String,Object> groupBy;
    @ApiModelProperty(value = "分组统计总数")
    private Integer count;


}
