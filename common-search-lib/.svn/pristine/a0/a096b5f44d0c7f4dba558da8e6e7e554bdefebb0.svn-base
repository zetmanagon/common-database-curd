package com.gemantic.search.utils;


import com.gemantic.search.constant.CommonFields;
import com.gemantic.search.constant.ElasticOperator;
import com.gemantic.search.support.agg.TermAgg;
import com.gemantic.search.support.query.ElasticQuery;
import com.gemantic.search.support.query.QueryItem;
import com.gemantic.search.support.query.SortItem;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.StatsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.NestedSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.index.query.QueryBuilders.*;

public class ESQueryUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ESQueryUtil.class);

    public static ActionRequestBuilder requstBuilder(Client client, ElasticQuery query) {
        if (query.isScrollSearch() && StringUtils.isNotBlank(query.getScrollId())) {
            SearchScrollRequestBuilder requestBuilder =
                    client.prepareSearchScroll(query
                            .getScrollId()).setScroll(new TimeValue(query.getScrollTime()));
            return requestBuilder;
        }

        SearchRequestBuilder requestBuilder =
                client.prepareSearch(query.getIndex())
                        .setSize(query.getPageSize())
                        .setPreference("_local")
                        .setExplain(query.isExplain());

        if (query.isScrollSearch()) {
            requestBuilder.setScroll(new TimeValue(query.getScrollTime()));
        } else {
            requestBuilder.setFrom((query.getCurrentPage() - 1) * query.getPageSize());

        }
        if (null != query.getMinScore()) {
            requestBuilder.setMinScore(query.getMinScore());
        }

        if (ArrayUtils.isNotEmpty(query.getRoutings())) {
            requestBuilder.setRouting(query.getRoutings());
        }

        requestBuilder.setExplain(query.isExplain());
        requestBuilder.setFetchSource(query.isFetchSource());
        requestBuilder.setFetchSource(query.getIncludeFields(), query.getExcludeFields());

        BoolQueryBuilder boolQueryBuilder = getQueryBuilder(query);


        if (boolQueryBuilder.hasClauses()) {
            requestBuilder.setQuery(boolQueryBuilder);
        } else {
            requestBuilder.setQuery(QueryBuilders.matchAllQuery());
        }


        if (CollectionUtils.isNotEmpty(query.getAggs())) {
            for (TermAgg termAgg : query.getAggs()) {
                Integer aggsTopSize = termAgg.getAggsTopSize();
                Integer aggsCurrentPage = termAgg.getAggsCurrentPage();
                Integer aggsPageSize = termAgg.getAggsPageSize();
                if(null != aggsCurrentPage && null != aggsPageSize && aggsCurrentPage > 0 && aggsPageSize > 0){
                    aggsTopSize = Integer.MAX_VALUE;
                }
                AggregationBuilder aggBuildersGroup = null;
                if (StringUtils.isBlank(termAgg.getValueField())) {
                    aggBuildersGroup =
                            AggregationBuilders.terms(termAgg.getKeyField())
                                    .size(aggsTopSize)
                                    .field(termAgg.getKeyField());

                } else {
                    StatsAggregationBuilder aggBuildersStats =
                            AggregationBuilders.stats(termAgg.getValueField()).field(termAgg.getValueField());
                    //防止跟facetFields的查询重名
                    aggBuildersGroup =
                            AggregationBuilders.terms(termAgg.getKeyField() + "_STATS")
                                    .size(aggsTopSize).field(termAgg.getKeyField())
                                    .subAggregation(aggBuildersStats);


                }
                requestBuilder.addAggregation(aggBuildersGroup);
            }


        }


        if (CollectionUtils.isNotEmpty(query.getPlainHighlightedFields())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.fragmentSize(query.getHighlightFragmentSize());
            highlightBuilder.numOfFragments(query.getHighlightNumberOfFragments());
            highlightBuilder.preTags(query.getHighlighterPreTags());
            highlightBuilder.postTags(query.getHighlighterPostTags());
            highlightBuilder.requireFieldMatch(true);

            for (String fieldName : query.getPlainHighlightedFields()) {
                highlightBuilder.field(fieldName);
            }
            requestBuilder.highlighter(highlightBuilder);
        }

        if (CollectionUtils.isEmpty(query.getAggs())) {
            if (CollectionUtils.isNotEmpty(query.getSorts())) {
                for (SortItem sortItem : query.getSorts()) {
                    if (CommonFields.SCORE_FIELD.equals(sortItem.getField())) {
                        continue;
                    }
                    FieldSortBuilder fieldSortBuilder = SortBuilders.fieldSort(sortItem.getField());
                    fieldSortBuilder.order(SortOrder.fromString(sortItem.getDirection()));
                    fieldSortBuilder.unmappedType(sortItem.getUnmappedType());
                    if (sortItem.getField().contains(".")) {
                        String nestedPath = StringUtils.substringBeforeLast(sortItem.getField(), ".");
                        fieldSortBuilder.setNestedSort(new NestedSortBuilder(nestedPath));
                    }
                    requestBuilder.addSort(fieldSortBuilder);
                }

            } else {
                requestBuilder.addSort(SortBuilders.scoreSort());
            }
        }

        if (StringUtils.isNotBlank(query.getCollapseField())) {
            CollapseBuilder headerCollapse = new CollapseBuilder(query.getCollapseField());
            requestBuilder.setCollapse(headerCollapse);
        }

        return requestBuilder;
    }


    public static ActionRequest searchRequest(ElasticQuery query) {

        if (query.isScrollSearch() && StringUtils.isNotBlank(query.getScrollId())) {
            SearchScrollRequest scrollRequest = new SearchScrollRequest(query.getScrollId());
            scrollRequest.scroll(new TimeValue(query.getScrollTime()));
            return scrollRequest;
        }
        SearchRequest searchRequest = new SearchRequest(query.getIndex());
        searchRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
        SearchSourceBuilder searchSourceBuilder = searchRequest.source();
        searchSourceBuilder.size(query.getPageSize());
        searchRequest.preference("_local");
        searchSourceBuilder.timeout(new TimeValue(query.getRequestTimeout(), TimeUnit.SECONDS));
        if (query.isScrollSearch()) {
            searchRequest.scroll(new TimeValue(query.getScrollTime()));
        } else {
            searchSourceBuilder.from((query.getCurrentPage() - 1) * query.getPageSize());
        }
        searchSourceBuilder.explain(query.isExplain());
        searchSourceBuilder.fetchSource(query.isFetchSource());
        searchSourceBuilder.fetchSource(query.getIncludeFields(), query.getExcludeFields());
        if (ArrayUtils.isNotEmpty(query.getRoutings())) {
            searchRequest.routing(query.getRoutings());
        }

        if (null != query.getMinScore()) {
            searchSourceBuilder.minScore(query.getMinScore());
        }

        BoolQueryBuilder boolQueryBuilder = getQueryBuilder(query);


        if (boolQueryBuilder.hasClauses()) {
            searchSourceBuilder.query(boolQueryBuilder);
        } else {
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        }

        if (CollectionUtils.isNotEmpty(query.getAggs())) {
            for (TermAgg termAgg : query.getAggs()) {
                Integer aggsTopSize = termAgg.getAggsTopSize();
                Integer aggsCurrentPage = termAgg.getAggsCurrentPage();
                Integer aggsPageSize = termAgg.getAggsPageSize();
                if(null != aggsCurrentPage && null != aggsPageSize && aggsCurrentPage > 0 && aggsPageSize > 0){
                    aggsTopSize = Integer.MAX_VALUE;
                }
                AggregationBuilder aggBuildersGroup = null;
                if (StringUtils.isBlank(termAgg.getValueField())) {
                    aggBuildersGroup =
                            AggregationBuilders.terms(termAgg.getKeyField())
                                    .size(aggsTopSize)
                                    .field(termAgg.getKeyField());

                } else {
                    StatsAggregationBuilder aggBuildersStats =
                            AggregationBuilders.stats(termAgg.getValueField()).field(termAgg.getValueField());
                    //防止跟facetFields的查询重名
                    aggBuildersGroup =
                            AggregationBuilders.terms(termAgg.getKeyField() + "_STATS")
                                    .size(aggsTopSize).field(termAgg.getKeyField())
                                    .subAggregation(aggBuildersStats);


                }
                searchSourceBuilder.aggregation(aggBuildersGroup);
            }


        }


        if (CollectionUtils.isNotEmpty(query.getPlainHighlightedFields())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.fragmentSize(query.getHighlightFragmentSize());
            highlightBuilder.numOfFragments(query.getHighlightNumberOfFragments());
            highlightBuilder.preTags(query.getHighlighterPreTags());
            highlightBuilder.postTags(query.getHighlighterPostTags());
            highlightBuilder.requireFieldMatch(true);
            for (String fieldName : query.getPlainHighlightedFields()) {
                highlightBuilder.field(fieldName);
            }
            searchSourceBuilder.highlighter(highlightBuilder);
        }

        if (CollectionUtils.isEmpty(query.getAggs())) {
            if (CollectionUtils.isNotEmpty(query.getSorts())) {
                for (SortItem sortItem : query.getSorts()) {
                    if (CommonFields.SCORE_FIELD.equals(sortItem.getField())) {
                        continue;
                    }
                    FieldSortBuilder fieldSortBuilder = SortBuilders.fieldSort(sortItem.getField());
                    fieldSortBuilder.order(SortOrder.fromString(sortItem.getDirection()));
                    fieldSortBuilder.unmappedType(sortItem.getUnmappedType());
                    if (sortItem.getField().contains(".")) {
                        String nestedPath = StringUtils.substringBeforeLast(sortItem.getField(), ".");
                        fieldSortBuilder.setNestedSort(new NestedSortBuilder(nestedPath));
                    }
                    searchSourceBuilder.sort(fieldSortBuilder);

                }

            } else {
                searchSourceBuilder.sort(SortBuilders.scoreSort());
            }
        }

        if (StringUtils.isNotBlank(query.getCollapseField())) {
            CollapseBuilder headerCollapse = new CollapseBuilder(query.getCollapseField());
            searchSourceBuilder.collapse(headerCollapse);
        }
        searchSourceBuilder.explain(query.isExplain());
        LOG.info("" + searchSourceBuilder);
        return searchRequest;
    }


    public static BoolQueryBuilder getQueryBuilder(ElasticQuery query) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        addSubQueryBuilder(boolQueryBuilder, query.getAndQuery(), ElasticOperator.AND);
        addSubQueryBuilder(boolQueryBuilder, query.getOrQuery(), ElasticOperator.OR);
        addSubQueryBuilder(boolQueryBuilder, query.getNotQuery(), ElasticOperator.NOT);
        if (CollectionUtils.isNotEmpty(query.getAndFilter()) || CollectionUtils.isNotEmpty(query.getOrFilter()) || CollectionUtils.isNotEmpty(query.getNotFilter())) {
            BoolQueryBuilder boolFilterQueryBuilder = QueryBuilders.boolQuery();
            addSubQueryBuilder(boolFilterQueryBuilder, query.getAndFilter(), ElasticOperator.AND);
            addSubQueryBuilder(boolFilterQueryBuilder, query.getOrFilter(), ElasticOperator.OR);
            addSubQueryBuilder(boolFilterQueryBuilder, query.getNotFilter(), ElasticOperator.NOT);
            boolQueryBuilder.filter(boolFilterQueryBuilder);
        }
        return boolQueryBuilder;
    }

    public static void addSubQueryBuilder(BoolQueryBuilder boolQueryBuilder, List<QueryItem> queryItems, ElasticOperator operator) {
        QueryBuilder subAndQueryBuilder = getSubQueryBuilder(queryItems, operator);
        if (null != subAndQueryBuilder) {
            boolQueryBuilder.must(subAndQueryBuilder);
        }
    }


    public static QueryBuilder getSubQueryBuilder(List<QueryItem> queryItems, ElasticOperator operator) {
        if (CollectionUtils.isEmpty(queryItems)) {
            return null;
        }
        BoolQueryBuilder query = boolQuery();
        for (QueryItem queryItem : queryItems) {
            String field = queryItem.getField();
            QueryBuilder childQuery = null;
            if (CollectionUtils.isNotEmpty(queryItem.getValues())) {
                childQuery = getValuesQuery(queryItem);
            } else if (null != queryItem.getMinValue() || null != queryItem.getMaxValue()) {
                Object start = queryItem.getMinValue();
                Object end = queryItem.getMaxValue();
                RangeQueryBuilder rangeQueryBuilder = rangeQuery(field).boost(queryItem.getBoost());

                if (null != start) {
                    if (queryItem.isMinValueIncludeLower()) {
                        rangeQueryBuilder.gte(start);
                    } else {
                        rangeQueryBuilder.gt(start);
                    }
                }
                if (null != end) {
                    if (queryItem.isMaxValueIncludeLower()) {
                        rangeQueryBuilder.lte(end);
                    } else {
                        rangeQueryBuilder.lt(end);
                    }
                }
                childQuery = rangeQueryBuilder;
            }


            if (field.contains(".")) {
                String path = StringUtils.substringBeforeLast(field, ".");
                childQuery = nestedQuery(path, childQuery, ScoreMode.None);
            }

            switch (operator) {
                case OR:
                    query.should(childQuery);
                    break;
                case AND:
                    query.must(childQuery);
                    break;
                case NOT:
                    query.mustNot(childQuery);
                    break;
                default:
                    break;
            }

        }
        return query;
    }

    public static QueryBuilder getValuesQuery(QueryItem queryItem) {
        if (CollectionUtils.isEmpty(queryItem.getValues())) {
            return null;
        }
        if (queryItem.getValues().size() == 1) {
            return getValueQuery(queryItem.getValues().get(0), queryItem);
        }
        BoolQueryBuilder boolQuery = boolQuery();
        for (String fieldValue : queryItem.getValues()) {
            QueryBuilder childQuery = getValueQuery(fieldValue, queryItem);
            if (Operator.OR.name().equalsIgnoreCase(queryItem.getOperator())) {
                boolQuery.should(childQuery).minimumShouldMatch(queryItem.getMinimumNumberShouldMatch());
            } else {
                boolQuery.must(childQuery);
            }
        }
        return boolQuery;
    }


    public static QueryBuilder getValueQuery(String fieldValue, QueryItem queryItem) {
        if (StringUtils.isBlank(fieldValue)) {
            return null;
        }
        QueryBuilder childQuery = null;
        if (StringUtils.isBlank(queryItem.getQueryType())) {
            queryItem.setQueryType("item");
        }
        switch (queryItem.getQueryType()) {
            case "multiMatch":
                childQuery = multiMatchQuery(fieldValue, StringUtils.split(queryItem.getField(), ",")).type(queryItem.getMultiMatchType()).operator(Operator.fromString(queryItem.getFieldOperator())).minimumShouldMatch(queryItem.getFieldMinimumNumberShouldMatch()).slop(queryItem.getSlop());
                break;
            case "matchPhrase":
                childQuery = matchPhraseQuery(queryItem.getField(), fieldValue).slop(queryItem.getSlop());
                break;
            case "prefix":
                childQuery = prefixQuery(queryItem.getField(), fieldValue);
                break;
            case "fuzzy":
                childQuery = fuzzyQuery(queryItem.getField(), fieldValue);
                break;
            case "wildcard":
                childQuery = wildcardQuery(queryItem.getField(), fieldValue);
                break;
            case "regexp":
                childQuery = regexpQuery(queryItem.getField(), fieldValue);
                break;
            default:
                if(queryItem.getValues().size() == 1) {
                    childQuery = matchQuery(queryItem.getField(), fieldValue);
                }else {
                    childQuery = termQuery(queryItem.getField(),fieldValue);
                }
                break;
        }
       childQuery.boost(queryItem.getBoost());
        return childQuery;
    }


}
