package com.gemantic.search.utils;

import com.gemantic.search.model.ElasticIndex;
import com.gemantic.search.model.ElasticPage;
import com.gemantic.search.support.agg.TermAgg;
import com.gemantic.search.support.agg.TermAggResult;
import com.gemantic.search.support.result.Hit;
import com.gemantic.search.support.result.SearchResults;
import com.gemantic.springcloud.utils.PageUtil;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class JsonConvertUtil {

	private final static Logger LOG = LoggerFactory.getLogger(JsonConvertUtil.class);
	
	private static ObjectMapper objectMapper;
	
	static {
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	

	public static <T> T str2Object(String jsonString,Class<T> valueType){
		T doc = null;
		try {
			if(StringUtils.isBlank(jsonString)){
				return doc;
			}
			doc = objectMapper.readValue(jsonString,valueType);
		} catch (Exception e) {
			LOG.warn("无效的Json格式",e);

		}
		return doc;
	}

	public static <T extends ElasticIndex> List<T> getHits(SearchResults searchResults, Class<T> valueType) {
        List<Hit> hits = searchResults.getSearchHits().getHits();
	    if(CollectionUtils.isEmpty(hits)){
	        return Lists.newArrayList();
        }

		List<T> lists = Lists.newArrayList();
        for (Hit hit : hits) {
			T doc = str2Object(hit.getSourceAsString(),valueType);
			if(null == doc) {
				continue;
			}
			doc.setElasticScore(hit.getScore());
			doc.setId(hit.getId());
            lists.add(doc);
            if(MapUtils.isEmpty(hit.getSnippets())){
                continue;
            }
            for(Map.Entry<String,String> snippetsEntry : hit.getSnippets().entrySet()){
                String highlightedContext = snippetsEntry.getValue();
                if (highlightedContext.startsWith("[")) {
                    highlightedContext = highlightedContext.substring(1);
                }
                if (highlightedContext.endsWith("]")) {
                    highlightedContext = highlightedContext.substring(0, highlightedContext.length() - 1);
                }
                hit.getSnippets().put(snippetsEntry.getKey(),highlightedContext);
            }
            doc.setSnippets(hit.getSnippets());
        }
		return lists;
	}

	public static <T extends ElasticIndex> ElasticPage<T> getElasticPage(SearchResults searchResults, Integer cp, Integer ps, List<TermAgg> termAggs, Class<T> valueType) {
		ElasticPage<T> pr = new ElasticPage<T>();
		pr.setTotalCount(searchResults.getTotalHits());
		pr.setScrollId(searchResults.getScrollId());
		pr.setCurrentPage(MoreObjects.firstNonNull(cp,1));
		Integer pageSize = MoreObjects.firstNonNull(ps,10);
		pr.setPageSize(pageSize);
		if(CollectionUtils.isNotEmpty(termAggs)){
			Map<String,TermAgg> termAggMap = termAggs.stream().collect(Collectors.toMap(s->s.getKeyField(), Function.identity(),(k1,k2)->k1));
			for(TermAggResult termAggResult :searchResults.getAggs()){
				TermAgg termAgg = termAggMap.get(termAggResult.getName());
				if(null == termAgg){
					continue;
				}
				Integer aggsCurrentPage = termAgg.getAggsCurrentPage();
				Integer aggsPageSize = termAgg.getAggsPageSize();
				if(null != aggsCurrentPage && null != aggsPageSize && aggsCurrentPage > 0 && aggsPageSize > 0){
					termAggResult.setBucketCurrentPage(aggsCurrentPage);
					termAggResult.setBucketTotalPage(PageUtil.getTotalPage(termAggResult.getBucketTotalCount(),aggsPageSize));
					termAggResult.setEntries(PageUtil.getPageList(termAggResult.getEntries(),aggsCurrentPage,aggsPageSize));
				}
			}
		}
		pr.setAggResults(searchResults.getAggs());
		pr.setElasticMaxScore(searchResults.getSearchHits().getMaxScore());
		int totalPage  = PageUtil.getTotalPage(Integer.valueOf(String.valueOf(pr.getTotalCount())),pageSize);
		pr.setTotalPage(totalPage);
		pr.setList(JsonConvertUtil.getHits(searchResults, valueType));
		return pr;
	}

}