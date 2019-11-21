package com.gemantic.doc.model;

import java.io.Serializable;

public class BaseCollection implements Serializable{
    protected String id;

    protected Long createAt;
    protected Long updateAt;

    public BaseCollection(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Long createAt) {
        this.createAt = createAt;
    }

    public Long getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Long updateAt) {
        this.updateAt = updateAt;
    }
}
