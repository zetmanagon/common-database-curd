package com.gemantic.search.support.elastic;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
@EqualsAndHashCode
public class IndexInfo implements Serializable {

    private String index;

    private String setting;

    private String mapping;

}
