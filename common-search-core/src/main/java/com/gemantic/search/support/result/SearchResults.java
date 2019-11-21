package com.gemantic.search.support.result;

import com.gemantic.search.support.agg.TermAggResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@ToString
@EqualsAndHashCode
public class SearchResults implements Serializable {

	private static final long serialVersionUID = 755642660931960741L;
	private long tookInMillis;
	private long totalHits;
	private String scrollId;
	private SearchHits searchHits;
	private List<TermAggResult> aggs;

	
}
