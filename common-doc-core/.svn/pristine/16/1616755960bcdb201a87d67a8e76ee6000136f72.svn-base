package com.gemantic.doc.support;

import com.gemantic.doc.constant.SearchType;
import io.swagger.annotations.ApiModel;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("查询item")
public class CollectionQueryItem implements Serializable{
    private static final long serialVersionUID = -7256737008862482045L;

    private String field;

    private SearchType searchType = SearchType.FULL_VALUE;

    private List values;

    //起始时间
    private Number valueStartAt;

    //结束时间
    private Number valueEndAt;

    //嵌套子节点
    private List<CollectionQueryItem> andChildren;

    private List<CollectionQueryItem> orChildren;

    private List<CollectionQueryItem> notChildren;

}
