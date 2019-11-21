package com.gemantic.db.support;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;


@ApiModel("排序对象")
@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class DBSortItem implements Serializable {

    @ApiModelProperty(value = "字段名")
    private String field;

    @ApiModelProperty(value = "排序方向:DESC-从大到小,ASC-从小到大",allowableValues = "DESC,ASC")
    private String direction = "DESC";
}
