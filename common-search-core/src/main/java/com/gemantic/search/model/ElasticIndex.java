/**
 *
 */
package com.gemantic.search.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

@Data
@ToString
@EqualsAndHashCode
public class ElasticIndex implements Serializable {

    @ApiModelProperty("编号")
    @Transient
    protected String id;

    @ApiModelProperty("elastic得分")
    @Transient
    protected float elasticScore;

    @ApiModelProperty("创建时间")
    protected Long createAt;

    @ApiModelProperty("更新时间")
    protected Long updateAt;


    @ApiModelProperty("高亮数据：key-字段名称,value-高亮后的数据")
    @Transient
    protected Map<String, String> snippets;

    public String getRouting() {
        return StringUtils.EMPTY;
    }

    public void initId() {
        if (StringUtils.isBlank(this.id)) {
            this.id = DigestUtils.md5Hex(UUID.randomUUID().toString());
        };
    }

}
