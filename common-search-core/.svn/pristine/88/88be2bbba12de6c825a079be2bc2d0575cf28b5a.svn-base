package com.gemantic.search.support.agg;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@ToString
@EqualsAndHashCode
public class TermAggResult implements Serializable {
	private static final long serialVersionUID = 4896217293551812208L;
	private String name;
	private String label;
	private Integer bucketTotalCount;
	private Integer bucketCurrentPage;
	private Integer bucketTotalPage;
	private long totalCount;
	private long otherCount;
	private List<TermAggEntry> entries;

}
