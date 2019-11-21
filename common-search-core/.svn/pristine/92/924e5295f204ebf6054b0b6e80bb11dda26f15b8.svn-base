package com.gemantic.search.support.query;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class QueryItem implements Serializable{

    private static final long serialVersionUID = -5882052908290434004L;


    @ApiModelProperty(value = "字段名称:multiMatch以逗号分隔多个字段")
    private String field;


    @ApiModelProperty(value = "查询方式：term-termQuery,match-matchQuery,matchPhrase-matchPhraseQuery,multiMatch-multiMatchQuery,prefix-prefixQuery,fuzzy-fuzzyQuery,wildcard-wildcardQuery,regexp-regexpQuery",allowableValues = "term,match,matchPhrase,multiMatch,prefix,fuzzy,wildcard,regexp")
    private String queryType = "term";

    @ApiModelProperty(value = "multiMatch查询对应的type:best_fields,most_fields,cross_fields,phrase,phrase_prefix",allowableValues = "best_fields,most_fields,cross_fields,phrase,phrase_prefix")
    private String multiMatchType = "best_fields";

    @ApiModelProperty(value = "values之间的关系：OR-或;AND-且",allowableValues = "OR,AND")
    private String operator = "OR";


    @ApiModelProperty(value = "field之间的关系：OR-或;AND-且",allowableValues = "OR,AND")
    private String fieldOperator = "OR";


    @ApiModelProperty(value = "values之间OR关系的最小匹配")
    private String minimumNumberShouldMatch = "1";

    @ApiModelProperty(value = "field之间OR关系的最小匹配")
    private String fieldMinimumNumberShouldMatch = "1";

    @ApiModelProperty(value = "字段值等值查询")
    private List<String> values;


    @ApiModelProperty(value = "最小值是否包含本值,true-gte;false-gt",allowableValues = "true,false")
    private boolean minValueIncludeLower = Boolean.TRUE;

    @ApiModelProperty(value = "最小值")
    private Object minValue;

    @ApiModelProperty(value = "最大值")
    private Object maxValue;


    @ApiModelProperty(value = "最大值是否包含本值,true-lte;false-lt",allowableValues = "true,false")
    private boolean maxValueIncludeLower = Boolean.TRUE;

    @ApiModelProperty(value = "boost设置")
    private float boost = 1.0f;

    @ApiModelProperty(value = "matchPhraseQuery使用的slop参数")
    private Integer slop = 0;



}
