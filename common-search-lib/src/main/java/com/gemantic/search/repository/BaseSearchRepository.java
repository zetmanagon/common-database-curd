package com.gemantic.search.repository;

import com.gemantic.search.model.ElasticIndex;
import com.gemantic.search.model.ElasticPage;
import com.gemantic.search.support.query.ElasticQuery;
import com.gemantic.search.support.result.SearchResults;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface BaseSearchRepository<T extends ElasticIndex> {

	boolean save(Collection<T> collection) throws Exception;

	SearchResults search(ElasticQuery query) throws Exception;

	ElasticPage<T> searchPage(ElasticQuery query) throws Exception;

	List<T> getObjectById(List<String> idList) throws Exception;

	List<T> getObjectById(String index, List<String> routing, List<String> idList) throws Exception;

	void deleteByQuery(ElasticQuery query) throws Exception;


	boolean deleteByRoutingAndId(String index, List<String> routings, List<String> idList) throws Exception;

	boolean deleteById(List<String> idList) throws Exception;
		
}
