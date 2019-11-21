package com.gemantic.doc.repository;

import com.gemantic.doc.model.BaseCollection;
import com.gemantic.doc.support.CollectionGroupResult;
import com.gemantic.doc.support.CollectionQuery;
import com.gemantic.doc.support.CollectionUpdate;
import com.gemantic.springcloud.model.PageResponse;

import java.util.List;
import java.util.Map;

public interface BaseRepository<T extends BaseCollection> {

    void saveList(List<T> data)  throws Exception;

    void bulkInsert(List<T> data) throws Exception;

    void bulkUpdate(List<T> data) throws Exception;


    void update(CollectionUpdate updateObj)  throws Exception;


    T getOne(String id)  throws Exception;

    PageResponse<T> searchPage(CollectionQuery query) throws Exception;

    List<CollectionGroupResult> group(CollectionQuery query) throws Exception;


    List<T> search(CollectionQuery query) throws Exception;

    void delete(CollectionQuery query) throws Exception;

    Class<T> getTClass();
}
