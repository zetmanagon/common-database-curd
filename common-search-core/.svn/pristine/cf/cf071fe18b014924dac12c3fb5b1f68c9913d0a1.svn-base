package com.gemantic.search.support.agg;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;

@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TermAgg implements Serializable {
	private static final long serialVersionUID = 1817225030031309041L;

	private String keyField;

	private String valueField;

	private Integer aggsTopSize = 10;

	@ApiModelProperty("aggs当前页")
	private Integer aggsCurrentPage;

	@ApiModelProperty("aggs页面大小")
	private Integer aggsPageSize;


}
