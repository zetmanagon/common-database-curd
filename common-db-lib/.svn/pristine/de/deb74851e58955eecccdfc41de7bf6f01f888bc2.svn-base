package com.gemantic.db.repository;

import com.gemantic.db.support.DBQuery;
import com.gemantic.db.support.DBUpdate;
import com.gemantic.springcloud.model.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import javax.annotation.Nullable;
import javax.persistence.Tuple;
import java.util.List;
import java.util.Map;

@NoRepositoryBean
public interface BaseRepository<T,ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T>  {


    int deleteByQuery(Specification var) throws Exception;

    int updateByQuery(Specification var, Map<String,Object> values) throws Exception;

    int updateByQuery(DBUpdate update) throws Exception;

    void bulkInsert(List<T> data)throws Exception;

    void bulkSave(List<T> data,List<String> onlyInsertFields) throws Exception;

    int deleteByQuery(DBQuery<ID> query) throws Exception;

    PageResponse<T> findByQuery(DBQuery<ID> dbQuery) throws Exception;

    PageResponse<Map<String,Object>> findByMapQuery(DBQuery<ID> dbQuery) throws Exception;

    List<Map<String,Object>> findMap(Map<String,String> includeFields, Specification<T> spec, Sort sort) throws Exception;



}
