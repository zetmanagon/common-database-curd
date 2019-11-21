package com.gemantic.db.support;

import com.gemantic.db.constant.DBOperation;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;
import java.util.List;


@ApiModel("查询字段")
@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class DBQueryItem implements Serializable {
    private static final long serialVersionUID = -616185699125912712L;

    @ApiModelProperty(value = "字段名")
    private String field;

    @ApiModelProperty(value = "操作符号：EQ-等值查询,LIKE-like查询,GT-大于查询,GTE-大于等于查询,LT-小于查询,LTE-小于等于查询,NEQ-不等于,NLIKE-not like",allowableValues = "EQ,LIKE,GT,GTE,LT,LTE,NEQ,NLIKE")
    private DBOperation operation = DBOperation.EQ;

    @ApiModelProperty(value = "字段值")
    private List values;

}
