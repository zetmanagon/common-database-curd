package com.gemantic.db.repository.impl;

import com.gemantic.db.constant.DBOperation;
import com.gemantic.db.model.BaseModel;
import com.gemantic.db.repository.BaseRepository;
import com.gemantic.db.support.*;
import com.gemantic.db.util.DBUtil;
import com.gemantic.springcloud.model.PageResponse;
import com.gemantic.springcloud.utils.PageUtil;
import com.gemantic.springcloud.utils.ReflectUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.jdbc.Work;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.query.QueryUtils;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class BaseRepositoryImpl<T, ID> extends SimpleJpaRepository<T, ID> implements BaseRepository<T, ID> {

    private static final Logger LOG = LoggerFactory.getLogger(BaseRepositoryImpl.class);


    private EntityManager em;

    private JpaEntityInformation<T, ?> ei;

    public BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager em) {
        super(entityInformation, em);
        this.em = em;
        this.ei = entityInformation;

    }

    public BaseRepositoryImpl(Class<T> domainClass, EntityManager em) {
        super(domainClass, em);
        this.em = em;
    }


    @Override
    @Transactional
    public int deleteByQuery(Specification var) throws Exception {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaDelete criteriaDelete = criteriaBuilder.createCriteriaDelete(getDomainClass());
        Root<T> root = criteriaDelete.from(getDomainClass());
        CriteriaQuery<T> query = criteriaBuilder.createQuery(getDomainClass());
        criteriaDelete.where(var.toPredicate(root, query, criteriaBuilder));
        return em.createQuery(criteriaDelete).executeUpdate();
    }

    @Override
    @Transactional
    public int updateByQuery(Specification var, Map<String, Object> values) throws Exception {
        if (null == var || MapUtils.isEmpty(values)) {
            return 0;
        }
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaUpdate criteriaUpdate = criteriaBuilder.createCriteriaUpdate(getDomainClass());
        Root<T> root = criteriaUpdate.from(getDomainClass());
        CriteriaQuery<T> query = criteriaBuilder.createQuery(getDomainClass());
        criteriaUpdate.where(var.toPredicate(root, query, criteriaBuilder));
        for (Map.Entry<String, Object> valueEntry : values.entrySet()) {
            String field = valueEntry.getKey();
            Object value = valueEntry.getValue();
            Path<Object> path = DBUtil.getPath(field, root);
            criteriaUpdate.set(path, DBUtil.getPathValue(path, value));
        }
        return em.createQuery(criteriaUpdate).executeUpdate();
    }

    @Override
    @Transactional
    public int updateByQuery(DBUpdate update) throws Exception {
        if (null == update) {
            return 0;
        }
        return updateByQuery(getSpecification(update.getAndQuery(), update.getOrQuery(), Boolean.FALSE), update.getUpdateValues());
    }

    private String INSERT_IGNORE_TPL = "INSERT IGNORE INTO %s (%s) values(%s) %s";

    private String INSERT_ON_UPDATE_TPL = "INSERT INTO %s (%s) values(%s) ON DUPLICATE KEY UPDATE %s";

    @Override
    @Transactional
    public void bulkInsert(List<T> data) throws Exception {
        if (CollectionUtils.isEmpty(data)) {
            return;
        }
        executeBatchWork(INSERT_IGNORE_TPL, null, data);
    }


    @Override
    @Transactional
    public void bulkSave(List<T> data, List<String> onlyInsertFields) throws Exception {
        if (CollectionUtils.isEmpty(data) || CollectionUtils.isEmpty(onlyInsertFields)) {
            return;
        }
        executeBatchWork(INSERT_ON_UPDATE_TPL, onlyInsertFields, data);
    }


    private void executeBatchWork(String tpl, List<String> onlyInsertFields, List<T> data) {
        Session session = em.unwrap(Session.class);
        List<Field> fields = Lists.newArrayList();
        String sql = getInsertSqlAndFillFields(tpl, onlyInsertFields, fields);
        session.doWork(new Work() {
            @Override
            public void execute(Connection connection) throws SQLException {
                PreparedStatement statement = connection.prepareStatement(sql);
                for (T object : data) {
                    try {
                        for (int i = 0; i < fields.size(); i++) {
                            int stateInt = i + 1;
                            Object value = fields.get(i).get(object);

                            if (null != value && value instanceof BaseModel) {
                                statement.setObject(stateInt, ((BaseModel) value).getId());
                            } else {
                                statement.setObject(stateInt, value);
                            }
                        }
                        statement.addBatch();
                    } catch (Exception e) {
                        LOG.error("bulkSave {} error {}", sql, object, e);
                    }
                }
                statement.executeBatch();
            }
        });
    }


    private String getInsertSqlAndFillFields(String tpl, List<String> onlyInsertFields, List<Field> fields) {
        EntityType<T> entityType = em.getMetamodel().entity(getDomainClass());
        SessionFactoryImpl sessionFactory = em.getEntityManagerFactory().unwrap(SessionFactoryImpl.class);
        SingleTableEntityPersister entityPersister = (SingleTableEntityPersister) sessionFactory.getMetamodel().entityPersister(getDomainClass());
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        StringBuilder update = new StringBuilder();
        for (Attribute attribute : entityType.getAttributes()) {
            if (columns.length() > 0) {
                columns.append(",");
                values.append(",");
            }

            String column = entityPersister.getPropertyColumnNames(attribute.getName())[0];
            columns.append(column);
            Field field = ReflectUtil.getField(attribute.getName(), getDomainClass());
            field.setAccessible(Boolean.TRUE);
            fields.add(field);
            values.append("?");
            if (CollectionUtils.isEmpty(onlyInsertFields) || onlyInsertFields.contains(attribute.getName()) || onlyInsertFields.contains(column)) {
                continue;
            }
            if (update.length() > 0) {
                update.append(",");
            }
            update.append(String.format("%s=values(%s)", column, column));

        }
        String sql = String.format(tpl, entityPersister.getTableName(), columns.toString(), values.toString(), update.toString());
        LOG.info(sql);
        return sql;
    }

    @Override
    @Transactional
    public int deleteByQuery(DBQuery<ID> query) throws Exception {
        if (null == query) {
            return 0;
        }
        if (CollectionUtils.isNotEmpty(query.getIds())) {
            DBQueryItem queryItem = new DBQueryItem();
            queryItem.setOperation(DBOperation.IN);
            queryItem.setValues(query.getIds());
            queryItem.setField("id");
            List<DBQueryItem> queryItems = Lists.newArrayList(queryItem);
            query = new DBQuery();
            query.setAndQuery(queryItems);
        }
        return deleteByQuery(getSpecification(query.getAndQuery(), query.getOrQuery(), Boolean.FALSE));
    }

    @Override
    public PageResponse<T> findByQuery(DBQuery<ID> query) throws Exception {
        if (null == query) {
            return new PageResponse<>();
        }
        if (query.getCurrentPage() <= 0) {
            query.setCurrentPage(1);
        }
        if (CollectionUtils.isNotEmpty(query.getIds())) {
            PageResponse<T> pageResponse = new PageResponse<T>();
            if(MapUtils.isEmpty(query.getIncludeFields())){
                pageResponse.setList(findAllById(query.getIds()));
            }else {
                DBQueryItem queryItem = new DBQueryItem();
                queryItem.setOperation(DBOperation.IN);
                queryItem.setValues(query.getIds());
                queryItem.setField("id");
                List<DBQueryItem> queryItems = Lists.newArrayList(queryItem);
                query = new DBQuery();
                query.setAndQuery(queryItems);
                pageResponse.setList(DBUtil.tuple2Object(findTuple(query.getIncludeFields(),getSpecification(query.getAndQuery(),null,Boolean.FALSE),DBUtil.getSort(query.getSorts())),getDomainClass()));
            }

            pageResponse.setTotalCountInt(pageResponse.getList().size());
            pageResponse.setTotalPage(1);
            pageResponse.setCurrentPage(1);
            pageResponse.setPageSize(pageResponse.getList().size());
            return pageResponse;
        }
        convertQueryItem(query.getAndQuery());
        convertQueryItem(query.getOrQuery());
        LOG.info("query [{}]", query);
        PageResponse<T> pageResponse = null;
        Specification<T> specification = getSpecification(query.getAndQuery(), query.getOrQuery(), query.isFetch());
        if (query.getPageSize() <= 0) {
            Long total = count(specification);
            pageResponse = new PageResponse<>();
            pageResponse.setTotalCount(total);
            pageResponse.setCurrentPage(1);
            pageResponse.setPageSize(query.getPageSize());
            return pageResponse;
        }
        if (query.isPage()) {
            Pageable pageable = DBUtil.getPageable(query.getCurrentPage(), query.getPageSize(), query.getSorts());
            if(MapUtils.isEmpty(query.getIncludeFields())){
                Page<T> result = findAll(specification,pageable);
                pageResponse = PageUtil.page2pageResponse(result);
            }else {
                Page<Tuple> tupleResult = findTuple(query.getIncludeFields(),specification,pageable);
                pageResponse = DBUtil.tuple2pageResponse(tupleResult,getDomainClass());
            }
       } else {
            pageResponse = new PageResponse<>();
            Sort sort = DBUtil.getSort(query.getSorts());
            if(MapUtils.isEmpty(query.getIncludeFields())){
                pageResponse.setList(findAll(specification, sort));
            }else {
                pageResponse.setList(DBUtil.tuple2Object(findTuple(query.getIncludeFields(),specification,sort),getDomainClass()));
            }
            pageResponse.setTotalCount(Long.MAX_VALUE);
            pageResponse.setCurrentPage(1);
            pageResponse.setPageSize(query.getPageSize());
        }

        return pageResponse;
    }


    @Override
    public PageResponse<Map<String,Object>> findByMapQuery(DBQuery<ID> query) throws Exception {
        if (null == query) {
            return new PageResponse<>();
        }
        if (query.getCurrentPage() <= 0) {
            query.setCurrentPage(1);
        }
        PageResponse<Map<String,Object>> pageResponse = new PageResponse<Map<String,Object>>();
        if (CollectionUtils.isNotEmpty(query.getIds())) {
            DBQueryItem queryItem = new DBQueryItem();
            queryItem.setOperation(DBOperation.IN);
            queryItem.setValues(query.getIds());
            queryItem.setField("id");
            List<DBQueryItem> queryItems = Lists.newArrayList(queryItem);
            query = new DBQuery();
            query.setAndQuery(queryItems);

            List<Map<String,Object>> result = findMap(query.getIncludeFields(),getSpecification(query.getAndQuery(),query.getOrQuery(),query.isFetch()),DBUtil.getSort(query.getSorts()));
            pageResponse.setList(result);
            pageResponse.setTotalCountInt(result.size());
            pageResponse.setTotalPage(1);
            pageResponse.setCurrentPage(1);
            pageResponse.setPageSize(result.size());
            return pageResponse;
        }
        convertQueryItem(query.getAndQuery());
        convertQueryItem(query.getOrQuery());
        LOG.info("query [{}]", query);
        if (query.getPageSize() <= 0) {
            Long total = count(getSpecification(query.getAndQuery(), query.getOrQuery(), query.isFetch()));
            pageResponse.setTotalCount(total);
            pageResponse.setCurrentPage(1);
            pageResponse.setPageSize(query.getPageSize());
        } else if (query.isPage()) {
            Pageable pageable = DBUtil.getPageable(query.getCurrentPage(), query.getPageSize(),  query.getSorts());
            Page<Tuple> result = findTuple(query.getIncludeFields(), getSpecification(query.getAndQuery(), query.getOrQuery(), query.isFetch()), pageable);
            if(null != result) {
                pageResponse.setList(DBUtil.tuple2Map(result.getContent()));
                pageResponse.setCurrentPage(result.getNumber() + 1);
                pageResponse.setPageSize(result.getSize());
                pageResponse.setTotalPage(result.getTotalPages());
                pageResponse.setTotalCountInt((int) result.getTotalElements());
            }
        } else {
            pageResponse.setList(findMap(query.getIncludeFields(), getSpecification(query.getAndQuery(), query.getOrQuery(), query.isFetch()), DBUtil.getSort(query.getSorts())));
            pageResponse.setTotalCount(Long.MAX_VALUE);
            pageResponse.setCurrentPage(1);
            pageResponse.setPageSize(query.getPageSize());
        }

        return pageResponse;
    }


    @Override
    public List<Map<String,Object>> findMap(Map<String,String> includeFields, Specification<T> spec, Sort sort) {
        List<Tuple> tuples = getTupleQuery(includeFields, spec, sort).getResultList();
        return DBUtil.tuple2Map(tuples);
    }


    protected Page<Tuple> findTuple(Map<String,String> includeFields, Specification<T> spec, Pageable pageable) {
        TypedQuery<Tuple> query = getTupleQuery(includeFields, spec, pageable);
        return (Page) (pageable.isUnpaged() ? new PageImpl(query.getResultList()) : readPageTuple(query, pageable, spec));
    }


    protected List<Tuple> findTuple(Map<String,String> includeFields, Specification<T> spec, Sort sort) {
        return getTupleQuery(includeFields,spec,sort).getResultList();
    }

    protected TypedQuery<Tuple> getTupleQuery(Map<String,String> includeFields, Specification<T> spec, Pageable pageable) {
        Sort sort = pageable.isPaged() ? pageable.getSort() : Sort.unsorted();
        return getTupleQuery(includeFields, spec, sort);
    }

    protected TypedQuery<Tuple> getTupleQuery(Map<String,String> includeFields, Specification<T> spec, Sort sort) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = builder.createTupleQuery();
        Root<T> root = query.from(getDomainClass());
        Predicate predicate = spec.toPredicate(root, query, builder);
        if (predicate != null) {
            query.where(predicate);
        }
        List<Selection<Object>> selections = Lists.newArrayList();
        for(Map.Entry<String,String> includeFieldsEntry : includeFields.entrySet()){
            String queryField = includeFieldsEntry.getKey();
            String aliasField = includeFieldsEntry.getValue();
            if(StringUtils.isBlank(aliasField)){
                aliasField = queryField;
            }
            selections.add(DBUtil.getPath(queryField, root).alias(aliasField));
        }
        CompoundSelection<Tuple> selection = builder.tuple(selections.toArray(new Selection[selections.size()]));
        query.select(selection);
        if (sort.isSorted()) {
            query.orderBy(QueryUtils.toOrders(sort, root, builder));
        }
        return em.createQuery(query);
    }

    protected Page<Tuple> readPageTuple(TypedQuery<Tuple> query, Pageable pageable, @Nullable Specification<T> spec) {
        if (pageable.isPaged()) {
            query.setFirstResult((int)pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        return PageableExecutionUtils.getPage(query.getResultList(), pageable, () -> {
            return executeCountTupleQuery(getCountQuery(spec, getDomainClass()));
        });
    }

    protected long executeCountTupleQuery(TypedQuery<Long> query) {
        Assert.notNull(query, "TypedQuery must not be null!");
        List<Long> totals = query.getResultList();
        long total = 0L;

        Long element;
        for(Iterator var4 = totals.iterator(); var4.hasNext(); total += element == null ? 0L : element) {
            element = (Long)var4.next();
        }

        return total;
    }

    protected void convertQueryItem(List<DBQueryItem> queryItems) {
        if (CollectionUtils.isEmpty(queryItems)) {
            return;
        }
        queryItems.stream().filter(Objects::nonNull)
                .filter(q -> (DBOperation.NLIKE.equals(q.getOperation()) || DBOperation.LIKE.equals(q.getOperation())) && CollectionUtils.isNotEmpty(q.getValues()))
                .forEach(q -> {
                    List convert = Lists.newArrayList();
                    for (Object v : q.getValues()) {
                        convert.add(EscapeCharacter.DEFAULT.escape(String.valueOf(v)));
                    }
                    q.setValues(convert);
                });
    }


    protected <T> Specification<T> getSpecification(List<DBQueryItem> andQuery, List<DBQueryItem> orQuery, boolean isFetch) {
        Specification<T> specification = new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                if (isFetch) {
                    List<String> fetchFields = DBUtil.getFetchFields(getDomainClass());
                    //总数统计不能使用fetch
                    if (CollectionUtils.isNotEmpty(fetchFields) && !criteriaQuery.getResultType().getSimpleName().equalsIgnoreCase(Long.class.getSimpleName())) {
                        for (String fetchfield : fetchFields) {
                            root.fetch(fetchfield, JoinType.LEFT);
                        }
                    }
                }

                List<Predicate> predicates = Lists.newArrayList();
                Predicate andPredicate = DBUtil.getPredicate(andQuery, root, criteriaBuilder, DBOperation.AND);
                if (null != andPredicate) {
                    predicates.add(andPredicate);
                }
                Predicate orPredicate = DBUtil.getPredicate(orQuery, root, criteriaBuilder, DBOperation.OR);
                if (null != orPredicate) {
                    predicates.add(orPredicate);
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return specification;
    }
}
