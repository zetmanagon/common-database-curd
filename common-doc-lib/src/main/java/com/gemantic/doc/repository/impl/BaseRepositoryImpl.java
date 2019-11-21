package com.gemantic.doc.repository.impl;

import com.gemantic.doc.constant.CollectionOperator;
import com.gemantic.doc.model.BaseCollection;
import com.gemantic.doc.repository.BaseRepository;
import com.gemantic.doc.support.*;
import com.gemantic.doc.utils.DocUtils;
import com.gemantic.springcloud.model.PageResponse;
import com.gemantic.springcloud.utils.PageUtil;
import com.gemantic.springcloud.utils.ReflectUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bson.Document;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.convert.MongoWriter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.Pair;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

public class BaseRepositoryImpl<T extends BaseCollection> implements BaseRepository<T> {

    protected Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Resource
    protected MongoOperations mongoTemplate;

    @Override
    public void saveList(List<T> data) throws Exception {
        for (T in : data) {
            mongoTemplate.save(in);
        }
    }

    @Override
    public void bulkInsert(List<T> data) throws Exception {
        List<Pair<Query, Update>> upserts = Lists.newArrayList();
        for (T o : data) {
            Query query = new Query(Criteria.where("_id").is(o.getId()));
            BasicDBObject dbObject = toDbObject(o, mongoTemplate.getConverter());
            Update update = getInsert(dbObject);
            upserts.add(Pair.of(query, update));
        }
        BulkOperations ops = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, getTClass());
        ops.upsert(upserts);
        ops.execute();
        return;
    }

    @Override
    public void bulkUpdate(List<T> data) throws Exception {
        List<Pair<Query, Update>> upserts = Lists.newArrayList();
        for (T o : data) {
            Query query = new Query(Criteria.where("_id").is(o.getId()));
            BasicDBObject dbObject = toDbObject(o, mongoTemplate.getConverter());
            Update update = getUpdate(dbObject);
            upserts.add(Pair.of(query, update));
        }
        BulkOperations ops = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, getTClass());
        ops.upsert(upserts);
        ops.execute();
        return;
    }

    protected Update getInsert(BasicDBObject object) throws Exception {
        Update update = new Update();
        for (String key : object.keySet()) {
            Object value = object.get(key);
            update.setOnInsert(key, value);
        }
        return update;
    }

    protected static final List<String> UPDATE_SETONINSERT = Lists.newArrayList("_id", "createAt");

    protected Update getUpdate(BasicDBObject object) throws Exception {
        Update update = new Update();
        for (String key : object.keySet()) {
            Object value = object.get(key);
            if (UPDATE_SETONINSERT.contains(key)) {
                update.setOnInsert(key, value);
            } else {
                update.set(key, value);
            }
        }
        return update;
    }

    protected <T> BasicDBObject toDbObject(T objectToSave, MongoWriter<T> writer) throws Exception {
        BasicDBObject dbDoc = new BasicDBObject();
        writer.write(objectToSave, dbDoc);
        return dbDoc;
    }

    protected org.apache.commons.lang3.tuple.Pair<T, Long> toObject(BasicDBObject dbObject, T bean) throws Exception {
        if (bean == null) {
            return null;
        }
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            String varName = field.getName();
            if (!dbObject.containsField(varName)) {
                continue;
            }
            Object object = dbObject.get(varName);
            field.setAccessible(true);
            field.set(bean, object);
        }
        Long count = dbObject.getLong("count");
        return org.apache.commons.lang3.tuple.Pair.of(bean, count);
    }


    @Override
    public T getOne(String id) throws Exception {
        Query query = new Query(Criteria.where("id").is(id));
        return mongoTemplate.findOne(query, getTClass());

    }


    @Override
    public List<T> search(CollectionQuery collectionQuery) throws Exception {
        if (null == collectionQuery) {
            return Lists.newArrayList();
        }
        Query query = getQuery(collectionQuery);
        if(CollectionUtils.isNotEmpty(collectionQuery.getIncludeFields())){
            collectionQuery.getIncludeFields().forEach(c->{
                query.fields().include(c);
            });

        }
        if (CollectionUtils.isEmpty(collectionQuery.getIds())) {
            if(CollectionUtils.isNotEmpty(collectionQuery.getSorts())){
                query.with(getSort(collectionQuery.getSorts()));
            }
            query.skip(PageUtil.getSkip(collectionQuery.getCurrentPage(), collectionQuery.getPageSize())).limit(collectionQuery.getPageSize());
        }
        LOG.info(String.format("%s skip %d limit %d", query, query.getSkip(), query.getLimit()));
        List<T> result = mongoTemplate.find(query, getTClass());
        return result;
    }

    @Override
    public void update(CollectionUpdate updateObj) throws Exception {
        if (CollectionUtils.isEmpty(updateObj.getUnsetFields())
                && MapUtils.isEmpty(updateObj.getAdd2SetObjects())
                && MapUtils.isEmpty(updateObj.getUpdateFields())) {
            return;
        }
        Update update = new Update();
        Map<String,String> updateFields = updateObj.getUpdateFields();
        if (MapUtils.isNotEmpty(updateObj.getUpdateFields())) {
            for(Map.Entry<String,String> updateField : updateFields.entrySet()){
                String field = updateField.getKey();
                String mainField = field;
                if (field.contains(".")) {
                    mainField = StringUtils.splitByWholeSeparator(field, ".")[0];
                }
                Field fieldF = ReflectUtil.getField(mainField, getTClass());
                String fieldType = fieldF.getType().getSimpleName();
                String value = updateField.getValue();
                switch (fieldType) {
                    case "List":
                        String fieldList = StringUtils.replace(field, ".", ".$.");
                        if (StringUtils.isBlank(value)) {
                            update.set(fieldList, Lists.newArrayList());
                        } else {
                            update.set(fieldList, value);
                        }
                        break;
                    case "Long":
                        if (StringUtils.isBlank(value)) {
                            update.set(field, null);
                        } else if (NumberUtils.isDigits(value)) {
                            update.set(field, Long.valueOf(value));
                        }
                        break;
                    case "Double":
                        if (StringUtils.isBlank(value)) {
                            update.set(field, null);
                        } else if (NumberUtils.isCreatable(value)) {
                            update.set(field, Double.valueOf(value));
                        }
                        break;
                    case "Integer":
                        if (StringUtils.isBlank(value)) {
                            update.set(field, null);
                        } else if (NumberUtils.isDigits(value)) {
                            update.set(field, Integer.valueOf(value));
                        }
                        break;
                    case "BigDecimal":
                        if (StringUtils.isBlank(value)) {
                            update.set(field, null);
                        } else if (NumberUtils.isCreatable(value)) {
                            update.set(field, new BigDecimal(value));
                        }
                        break;
                    default:
                        if (StringUtils.isBlank(value)) {
                            update.set(field, null);
                        } else {
                            update.set(field, value);
                        }
                        break;
                }
            }
        }
        Map<String, List<Object>> add2Set = updateObj.getAdd2SetObjects();
        if (MapUtils.isNotEmpty(add2Set)) {
            for (Map.Entry<String, List<Object>> add2SetEntry : add2Set.entrySet()) {
                String field = add2SetEntry.getKey();
                String mainField = field;
                if (field.contains(".")) {
                    mainField = StringUtils.splitByWholeSeparator(field, ".")[0];
                }
                Field fieldF = ReflectUtil.getField(mainField, getTClass());
                String fieldType = fieldF.getType().getSimpleName();
                if ("List".equalsIgnoreCase(fieldType)) {
                    field = StringUtils.replace(field, ".", ".$.");
                }
                List<Object> add2SetValue = add2SetEntry.getValue();
                Update.AddToSetBuilder builder = update.addToSet(field);
                builder.each(add2SetValue.toArray());
            }
        }

        List<String> unsetFields = updateObj.getUnsetFields();
        if (CollectionUtils.isNotEmpty(unsetFields)) {
            for (String upsetField : unsetFields) {
                update.unset(upsetField + ".$");
            }
        }

        update.set("updateAt", DateTime.now().getMillis());
        Query query =  new Query(getCriteria(updateObj.getIds(),updateObj.getAndQuery(),updateObj.getOrQuery(),updateObj.getNotQuery()));;
        LOG.info("update by [{}], values [{}]", query, update);
        mongoTemplate.updateMulti(query, update, getTClass());
    }

    @Override
    public PageResponse<T> searchPage(CollectionQuery collectionQuery) throws Exception {
        if (null == collectionQuery) {
            return new PageResponse<T>();
        }
        if (CollectionUtils.isNotEmpty(collectionQuery.getIds())) {
            PageResponse<T> pageResponse = new PageResponse<T>();
            List<T> result = search(collectionQuery);
            pageResponse.setList(result);
            pageResponse.setTotalCountInt(result.size());
            pageResponse.setTotalPage(1);
            pageResponse.setCurrentPage(1);
            pageResponse.setPageSize(result.size());
            return pageResponse;
        }

        Query query = getQuery(collectionQuery);
        if(CollectionUtils.isNotEmpty(collectionQuery.getIncludeFields())){
            collectionQuery.getIncludeFields().forEach(c->{
                query.fields().include(c);
            });
        }
        Query queryCount = getQuery(collectionQuery);
        if(StringUtils.isNotBlank(collectionQuery.getQueryHint())) {
            queryCount.withHint(collectionQuery.getQueryHint());
            LOG.info(String.format("queryCount hint %s",queryCount.getHint()));
        }
        Long count = mongoTemplate.count(queryCount, getTClass());
        PageResponse<T> page = new PageResponse<T>();
        page.setTotalCount(count);
        page.setCurrentPage(collectionQuery.getCurrentPage());
        page.setPageSize(collectionQuery.getPageSize());
        page.setTotalPage(PageUtil.getTotalPage(count.intValue(), collectionQuery.getPageSize()));
        if(CollectionUtils.isNotEmpty(collectionQuery.getSorts())) {
            query.with(getSort(collectionQuery.getSorts()));
        }
        if(collectionQuery.getPageSize() > 0) {
            query.skip(PageUtil.getSkip(collectionQuery.getCurrentPage(), collectionQuery.getPageSize())).limit(collectionQuery.getPageSize());
            LOG.info(String.format("%s skip %d limit %d", query, query.getSkip(), query.getLimit()));
            List<T> result = mongoTemplate.find(query, getTClass());
            page.setList(result);
        }
        return page;
    }


    @Override
    public List<CollectionGroupResult> group(CollectionQuery collectionQuery) throws Exception {
        Criteria criteria = getCriteria(collectionQuery);
        List<AggregationOperation> operations = Lists.newArrayList();
        if (StringUtils.isNotBlank(collectionQuery.getUnwind())) {
            operations.add(Aggregation.unwind(collectionQuery.getUnwind(), Boolean.TRUE));
        }
        operations.add(Aggregation.match(criteria));
        GroupOperation groupOperation  = Aggregation.group(collectionQuery.getGroupBy().toArray(new String[]{})).count().as("count");
        operations.add(groupOperation);
        operations.add(Aggregation.sort(Sort.Direction.DESC, "count"));

        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<T> aggregationResults = mongoTemplate.aggregate(aggregation, StringUtils.uncapitalize(getTClass().getSimpleName()), getTClass());
        if (LOG.isDebugEnabled()) {
            LOG.debug("aggregation results [{}]", aggregationResults.getRawResults());
        }
        List<Document> rawResults = (List<Document>) aggregationResults.getRawResults().get("results");
        List<CollectionGroupResult> result = Lists.newArrayList();
        Map<Object, Integer> resultTmp = Maps.newHashMap();
        for (Document d : rawResults) {
            Integer count = d.getInteger("count", 0);
            CollectionGroupResult cg = new CollectionGroupResult();
            cg.setCount(count);
            Map<String, Object> groupby = Maps.newHashMap();
            if (collectionQuery.getGroupBy().size() >= 2) {
                Document groupDict = (Document) d.get("_id");
                for (String key : collectionQuery.getGroupBy()) {
                    String dictKey = key;
                    if (dictKey.contains(".")) {
                        dictKey = StringUtils.substringAfter(dictKey, ".");
                    }
                    if (!groupDict.containsKey(dictKey)) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("group no key [{}] [{}]", key, dictKey);
                        }
                        continue;
                    }
                    Object value = DocUtils.getAggregationResultValue(groupDict.get(dictKey));
                    groupby.put(key, value);
                }
                cg.setGroupBy(groupby);
                result.add(cg);
            } else {
//                Object value = AiDocUtils.getAggregationResultValue(d.get("_id"));
                List<Object> values = Lists.newArrayList();
                DocUtils.fillAggregationResultValue(values, d.get("_id"));
                if (CollectionUtils.isEmpty(values)) {
                    continue;
                }
                for (Object v : values) {
                    Integer tcount = count;
                    if (resultTmp.containsKey(v)) {
                        tcount = resultTmp.get(v) + tcount;
                    }
                    resultTmp.put(v, tcount);
                }
//                groupby.put(keys.get(0), value);
            }

        }
        if (MapUtils.isNotEmpty(resultTmp)) {
            for (Map.Entry<Object, Integer> entry : resultTmp.entrySet()) {
                CollectionGroupResult cg = new CollectionGroupResult();
                cg.setCount(entry.getValue());
                Map<String, Object> groupby = Maps.newHashMap();
                groupby.put(collectionQuery.getGroupBy().get(0), entry.getKey());
                cg.setGroupBy(groupby);
                result.add(cg);
            }
        }
        result = result.stream().sorted(comparing(CollectionGroupResult::getCount).reversed()).collect(Collectors.toList());
        return result;
    }


    @Override
    public void delete(CollectionQuery collectionQuery) throws Exception {
        if (null == collectionQuery) {
            return;
        }
        Query query = getQuery(collectionQuery);
        LOG.info("delete by query [{}]", query);
        mongoTemplate.remove(query, getTClass());
    }


    protected Query getQuery(CollectionQuery collectionQuery) {

        Query query = new Query(getCriteria(collectionQuery));

        return query;
    }





    protected Criteria getCriteria(CollectionQuery collectionQuery) {
        return getCriteria(collectionQuery.getIds(),collectionQuery.getAndQuery(),collectionQuery.getOrQuery(),collectionQuery.getNotQuery());
    }


    protected Criteria getCriteria(List<String> ids,List<CollectionQueryItem> andQuery,List<CollectionQueryItem> orQuery,List<CollectionQueryItem> notQuery) {
        if (CollectionUtils.isNotEmpty(ids)) {
            return Criteria.where("id").in(ids);
        }
        Criteria andCriteria = getCriteria(andQuery, CollectionOperator.AND);
        Criteria orCriteria = getCriteria(orQuery, CollectionOperator.OR);
        Criteria notCriteria = getCriteria(notQuery, CollectionOperator.NOT);
        Criteria criteria = new Criteria();
        criteria.andOperator(andCriteria, orCriteria, notCriteria);
        return criteria;
    }


    protected Criteria getCriteria(List<CollectionQueryItem> queryItems, CollectionOperator operator) {
        if (CollectionUtils.isEmpty(queryItems)) {
            return new Criteria();
        }
        List<Criteria> criteriaList = Lists.newArrayList();
        for (CollectionQueryItem queryItem : queryItems) {
            if (CollectionUtils.isEmpty(queryItem.getNotChildren()) && CollectionUtils.isEmpty(queryItem.getOrChildren()) && CollectionUtils.isEmpty(queryItem.getAndChildren()) && CollectionUtils.isEmpty(queryItem.getValues()) && null == queryItem.getValueStartAt() && null == queryItem.getValueEndAt() || StringUtils.isBlank(queryItem.getField())) {
                continue;
            }
            Criteria tmp = Criteria.where(queryItem.getField());
            if (CollectionUtils.isNotEmpty(queryItem.getAndChildren()) || CollectionUtils.isNotEmpty(queryItem.getOrChildren()) || CollectionUtils.isNotEmpty(queryItem.getNotChildren())) {
                Criteria andCriteria = getCriteria(queryItem.getAndChildren(), CollectionOperator.AND);
                Criteria orCriteria = getCriteria(queryItem.getOrChildren(), CollectionOperator.OR);
                Criteria notCriteria = getCriteria(queryItem.getNotChildren(), CollectionOperator.NOT);
                Criteria criteria = new Criteria();
                criteria.andOperator(andCriteria, orCriteria, notCriteria);
                tmp.elemMatch(criteria);
            } else {
                switch (queryItem.getSearchType()) {
                    case LIKE:
                        tmp.regex(DocUtils.getLikeRegex(queryItem.getValues().get(0).toString()));
                        break;
                    case FULL_VALUE:
                        if (null != queryItem.getValueStartAt() || null != queryItem.getValueEndAt()) {
                            if (null != queryItem.getValueStartAt() && null != queryItem.getValueEndAt()) {
                                tmp.gte(queryItem.getValueStartAt()).lte(queryItem.getValueEndAt());
                            } else if (null != queryItem.getValueStartAt()) {
                                tmp.gte(queryItem.getValueStartAt());
                            } else if (null != queryItem.getValueEndAt()) {
                                tmp.lte(queryItem.getValueEndAt());
                            }
                        } else {
                            if (queryItem.getValues().size() > 1) {
                                tmp.in(queryItem.getValues());
                            } else {
                                tmp.is(queryItem.getValues().get(0));
                            }

                        }
                        break;
                }
            }
            criteriaList.add(tmp);
        }

        if (CollectionUtils.isEmpty(criteriaList)) {
            return new Criteria();
        }
        Criteria criteria = new Criteria();
        Criteria[] criterias = criteriaList.toArray(new Criteria[]{});
        switch (operator) {
            case AND:
                criteria.andOperator(criterias);
                break;
            case OR:
                criteria.orOperator(criterias);
                break;
            case NOT:
                criteria.norOperator(criterias);
                break;
        }
        return criteria;
    }


    public Class<T> getTClass() {
        Class<T> entityClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return entityClass;
    }

    protected Sort getSort(List<CollectionSortItem> sortItems) {
        List<Sort.Order> orders = Lists.newArrayList();
        if (CollectionUtils.isEmpty(sortItems)) {
            orders.add(Sort.Order.asc("id"));
            return Sort.by(orders);
        }
        for (CollectionSortItem sortItem : sortItems) {
            if ("DESC".equalsIgnoreCase(sortItem.getDirection())) {
                orders.add(Sort.Order.desc(sortItem.getField()));
            } else {
                orders.add(Sort.Order.asc(sortItem.getField()));
            }
        }
        return Sort.by(orders);
    }

}
