package com.gemantic.db.support;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@ApiModel("查询更新")
@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class DBUpdate implements Serializable {

    private static final long serialVersionUID = 3729789750142438333L;
    @ApiModelProperty(value = "and 查询")
    private List<DBQueryItem> andQuery;

    @ApiModelProperty(value = "or 查询")
    private List<DBQueryItem> orQuery;

    @ApiModelProperty(value = "更新值:key-字段名;value-更新值")
    private Map<String,Object> updateValues;
}
