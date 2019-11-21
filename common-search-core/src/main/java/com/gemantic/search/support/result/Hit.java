package com.gemantic.search.support.result;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

@Data
@ToString
@EqualsAndHashCode
public final class Hit implements Serializable {

	private static final long serialVersionUID = -2650142505237787759L;
	private float score;
	private String id;
	private String sourceAsString;
	private Map<String, String> snippets = Maps.newHashMap();

	
}
