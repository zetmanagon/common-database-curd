package com.gemantic.doc.utils;

import com.gemantic.springcloud.utils.StringUtil;
import com.google.common.collect.Lists;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DocUtils {
    public static final String FIELD_NULL_DEFAULT = "NONE";

    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String getId(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            String field = StringUtil.trim(fields[i]);
            if (StringUtils.isBlank(field)) {
                field = FIELD_NULL_DEFAULT;
            }
            if (i > 0) {
                sb.append("_");
            }
            sb.append(field);
        }
        return DigestUtils.md5Hex(sb.toString());
    }


    public static String getLikeRegex(String value) {
        String[] searchList = Lists.newArrayList("(", "[", "\\", "^", "$", "*", "+", "?", ".", "|", "[", "]", "{", "}", ")").toArray(new String[]{});
        String[] replaceList = Lists.newArrayList("\\(", "\\[", "\\\\", "\\^", "\\$", "\\*", "\\+", "\\?", "\\.", "\\|", "\\[", "\\]", "\\{", "\\}", "\\)").toArray(new String[]{});
        value = StringUtils.replaceEach(value, searchList, replaceList);
        return ".*?" + (value) + ".*";
    }


    public static Object getAggregationResultValue(Object value) {
        if (null == value) {
            return value;
        }
        if (value instanceof ArrayList) {
            ArrayList list = (ArrayList) value;
            if (CollectionUtils.isEmpty(list)) {
                return null;
            }
            return getAggregationResultValue(list.get(0));
        } else {
            return value;
        }
    }


    public static void fillAggregationResultValue(List<Object> values, Object value) {
        if (null == value) {
            return;
        }
        if (value instanceof ArrayList) {
            ArrayList list = (ArrayList) value;
            for (Object v : list) {
                fillAggregationResultValue(values, v);
            }
        } else {
            values.add(value);
        }
    }


}
