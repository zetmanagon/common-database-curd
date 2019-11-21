package com.gemantic.db.util;

import com.gemantic.db.constant.DBOperation;
import com.gemantic.db.support.DBQueryItem;
import com.gemantic.db.support.DBSortItem;
import com.gemantic.springcloud.model.PageResponse;
import com.gemantic.springcloud.utils.ReflectUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.criteria.internal.ValueHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ReflectPermission;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DBUtil {

    private  static  final Logger LOG = LoggerFactory.getLogger(DBUtil.class);


    public static <T> PageResponse<T> tuple2pageResponse(Page<Tuple> page,Class<T> tClass) throws Exception{
        PageResponse<T> pageResponse = new PageResponse<T>();
        if (page != null) {
            pageResponse.setList(tuple2Object(page.getContent(),tClass));
            pageResponse.setCurrentPage(page.getNumber() + 1);
            pageResponse.setPageSize(page.getSize());
            pageResponse.setTotalPage(page.getTotalPages());
            pageResponse.setTotalCountInt((int) page.getTotalElements());
        }
        return pageResponse;
    }

    public static Pageable getPageable(Integer cp, Integer ps, List<DBSortItem> sortItems) throws Exception {
            return getPageable(cp, ps,null, sortItems);
    }

    public static Pageable getPageable(Integer cp, Integer ps, String idField, List<DBSortItem> sortItems) throws Exception {
        String id = StringUtils.isBlank(idField) ? "id" : idField;
        if (null == sortItems) {
            sortItems = Lists.newArrayList();
        }
        if (CollectionUtils.isEmpty(sortItems.stream().filter(s -> s.getField().equalsIgnoreCase(id)).collect(Collectors.toList()))) {
            sortItems.add(new DBSortItem(id, Sort.Direction.ASC.name()));
        }

        Pageable pageable = PageRequest.of(cp - 1, ps, getSort(sortItems));
        return pageable;
    }

    public static Sort getSort(List<DBSortItem> sortItems)throws Exception{
        if(CollectionUtils.isEmpty(sortItems)){
            return Sort.by("id");
        }
        List<Sort.Order> sorts = sortItems.stream().map(s -> new Sort.Order(Sort.Direction.fromString(s.getDirection().toUpperCase()), s.getField())).collect(Collectors.toList());
        Sort sort = Sort.by(sorts);
        return sort;
    }

    public static List<DBSortItem> getSortItems(List<String> orderBys, List<String> directions) throws Exception {

        if (CollectionUtils.isEmpty(orderBys) || CollectionUtils.isEmpty(directions)) {
            return Lists.newArrayList();
        }
        List<DBSortItem> sorts = Lists.newArrayList();
        for (int i = 0; i < orderBys.size(); i++) {
            String orderBy = orderBys.get(i);
            String direction = null;
            if (i < directions.size()) {
                direction = directions.get(i);
            } else {
                direction = directions.get(directions.size() - 1);
            }
            DBSortItem sort = new DBSortItem(orderBy, direction);
            sorts.add(sort);
        }
        return sorts;


    }


    public static <T> Predicate getPredicate(List<DBQueryItem> queryItems, Root<T> root, CriteriaBuilder criteriaBuilder, DBOperation operation) {
        if (CollectionUtils.isEmpty(queryItems)) {
            return null;
        }
        List<Predicate> predicates = Lists.newArrayList();
        for (DBQueryItem queryItem : queryItems) {
            if (CollectionUtils.isEmpty(queryItem.getValues())) {
                continue;
            }
            Path path = getPath(queryItem.getField(), root);
            Object firstValue = queryItem.getValues().get(0);
            Predicate predicate = null;
            switch (queryItem.getOperation()) {
                case EQ:
                    predicate = criteriaBuilder.equal(path, getPathValue(path,firstValue));
                    break;
                case LIKE:
                    predicate = criteriaBuilder.like(path, "%" + firstValue.toString() + "%");
                    break;
                case GT:
                    predicate = criteriaBuilder.greaterThan(path, (Comparable) getPathValue(path, firstValue));
                    break;
                case LT:
                    predicate = criteriaBuilder.lessThan(path, (Comparable)getPathValue(path, firstValue));
                    break;
                case GTE:
                    predicate = criteriaBuilder.greaterThanOrEqualTo(path, (Comparable)getPathValue(path, firstValue));
                    break;
                case LTE:
                    predicate = criteriaBuilder.lessThanOrEqualTo(path, (Comparable)getPathValue(path, firstValue));
                    break;
                case IN:
                    if(queryItem.getValues().size() == 1){
                        predicate = criteriaBuilder.equal(path, getPathValue(path,firstValue));
                    }else {
                        CriteriaBuilder.In<Object> in = criteriaBuilder.in(path);
                        queryItem.getValues().forEach(v -> in.value(getPathValue(path, v)));
                        predicate = criteriaBuilder.and(in);
                    }
                    break;
                case NIN:
                    if(queryItem.getValues().size() == 1){
                        predicate = criteriaBuilder.notEqual(path, getPathValue(path,firstValue));
                    }else {
                        CriteriaBuilder.In<Object> in = criteriaBuilder.in(path);
                        queryItem.getValues().forEach(v -> in.value(getPathValue(path, v)));
                        predicate = criteriaBuilder.not(in);
                    }
                    break;
                case NEQ:
                    predicate = criteriaBuilder.notEqual(path, getPathValue(path,firstValue));
                    break;
                case NLIKE:
                    predicate = criteriaBuilder.notLike(path, "%" + firstValue.toString() + "%");
                    break;

            }
            if (null != predicate) {
                predicates.add(predicate);
            }

        }
        if (CollectionUtils.isEmpty(predicates)) {
            return null;
        }
        Predicate result = null;
        if (DBOperation.AND.equals(operation)) {
            result = criteriaBuilder.and(predicates.toArray(new Predicate[]{}));
        } else if (DBOperation.OR.equals(operation)) {
            result = criteriaBuilder.or(predicates.toArray(new Predicate[]{}));
        }
        return result;

    }


    public static Object getPathValue(Path<Object> path, Object v) {
        return ValueHandlerFactory.convert(v, path.getJavaType());

    }


    public static <O, T> Path<O> getPath(String fieldName, Root<T> root) {
        String[] fieldNamePaths = StringUtils.splitByWholeSeparator(fieldName, ".");
        Path<O> path = null;
        for (String fieldNamePath : fieldNamePaths) {
            if (null == path) {
                path = root.get(fieldNamePath);
            } else {
                path = path.get(fieldNamePath);
            }
        }
        return path;
    }

    public static final List<String> FETCH_ANNOTATIONS = Lists.newArrayList(ManyToOne.class.getTypeName(), OneToMany.class.getTypeName(), OneToOne.class.getTypeName(), ManyToMany.class.getTypeName());

    public static List<String> getFetchFields(Class classz) {
        List<String> results = Lists.newArrayList();
        Field[] fields = classz.getDeclaredFields();
        for (Field field : fields) {
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                if (FETCH_ANNOTATIONS.contains(annotation.annotationType().getTypeName())) {
                    results.add(field.getName());
                    break;
                }

            }
        }
        return results;
    }


    public static List<Map<String,Object>> tuple2Map(List<Tuple> tuples){
        if(CollectionUtils.isEmpty(tuples)){
            return Lists.newArrayList();
        }
        List<Map<String,Object>> result = Lists.newArrayList();
        for(Tuple tuple : tuples) {
            Map<String,Object> map = Maps.newHashMap();
            List<TupleElement<?>> tupleElements = tuple.getElements();
            for(TupleElement<?> tupleElement : tupleElements){
                map.put(tupleElement.getAlias(),tuple.get(tupleElement.getAlias(),tupleElement.getJavaType()));
            }
           result.add(map);
        }
        return result;

    }

    public static <T> List<T> tuple2Object(List<Tuple> tuples,Class<T> tClass) throws Exception{
        if(CollectionUtils.isEmpty(tuples) || null == tClass){
            return Lists.newArrayList();
        }
        List<T> result = Lists.newArrayList();
        for(Tuple tuple : tuples) {
            T object = tClass.newInstance();
            List<TupleElement<?>> tupleElements = tuple.getElements();
            for(TupleElement<?> tupleElement : tupleElements){
                Field field = ReflectUtil.getField(tupleElement.getAlias(),tClass);
                if(null == field){
                    continue;
                }
                field.setAccessible(true);
                field.set(object,tuple.get(tupleElement.getAlias(),tupleElement.getJavaType()));
            }
            result.add(object);
        }
        return result;
    }

}
