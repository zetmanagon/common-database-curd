package com.gemantic.search.utils;

import com.gemantic.search.support.query.QueryItem;
import com.gemantic.search.support.query.SortItem;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class QueryConverUtil {

    public static QueryItem addQueryItem(List<QueryItem> list, String field, String value) throws Exception{
        if(StringUtils.isBlank(field) || StringUtils.isBlank(value) ){
            return null;
        }
        return addQueryItem(list,field,value,null,null);
    }

    public static QueryItem addQueryItem(List<QueryItem> list, String field,String value,String operator,String minimumNumberShouldMatch) throws Exception{
        if(StringUtils.isBlank(field) || StringUtils.isBlank(value) ){
            return null;
        }
        QueryItem queryItem = new QueryItem();
        queryItem.setField(field);
        queryItem.setValues(Lists.newArrayList(StringUtils.split(value,",")));
        if(null != operator){
            queryItem.setOperator(operator);
        }
        if(StringUtils.isNotBlank(minimumNumberShouldMatch)){
            queryItem.setMinimumNumberShouldMatch(minimumNumberShouldMatch);
        }
        list.add(queryItem);
        return queryItem;
    }

    public static QueryItem addQueryItem(List<QueryItem> list, String field,Object minValue,Object maxValue) throws Exception{
        if(StringUtils.isBlank(field) || (null == minValue && null == maxValue) ){
            return null;
        }
        QueryItem queryItem = new QueryItem();
        queryItem.setField(field);
        queryItem.setMinValue(minValue);
        queryItem.setMaxValue(maxValue);
        list.add(queryItem);
        return queryItem;
    }


    public static List<SortItem> getSortItems(List<String> fields, List<String> directions) throws Exception{
        if(CollectionUtils.isEmpty(fields)){
            return Lists.newArrayList();
        }
        int directLastIndex = null == directions ? -1:directions.size()-1;
        List<SortItem> sortItems = Lists.newArrayList();
        String direction = null;
        for(int i = 0;i<=fields.size()-1;i++){
            String field = fields.get(i);
            SortItem sortItem = new SortItem();
            sortItem.setField(field);
            if(directLastIndex>=0 && i <= directLastIndex){
                direction = directions.get(i);
            }
            if(StringUtils.isNotBlank(direction)){
                sortItem.setDirection(direction);
            }
            sortItems.add(sortItem);
        }
        return sortItems;
    }
}
