package com.gemantic.doc.support;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@ApiModel("查询更新对象")
@Data
@ToString
@EqualsAndHashCode
public class CollectionUpdate<T> implements Serializable {

    private static final long serialVersionUID = 5138033604553143518L;

    @ApiModelProperty(value = "记录编号集合")
    private List<String> ids;
    @ApiModelProperty(value = "and 查询")
    private List<CollectionQueryItem> andQuery;
    @ApiModelProperty(value = "or 查询")
    private List<CollectionQueryItem> orQuery;
    @ApiModelProperty(value = "not 查询")
    private List<CollectionQueryItem> notQuery;

    @ApiModelProperty(value = "修改字段对应的值")
    private Map<String,String> updateFields;

    @ApiModelProperty(value = "属性是集合，集合添加元素")
    private Map<String, List<Object>> add2SetObjects;

    @ApiModelProperty(value = "属性是集合，移除集合内满足查询条件的元素")
    private List<String> unsetFields;

}
