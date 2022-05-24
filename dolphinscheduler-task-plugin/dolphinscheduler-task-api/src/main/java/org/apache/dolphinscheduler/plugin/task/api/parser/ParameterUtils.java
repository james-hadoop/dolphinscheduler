/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.plugin.task.api.parser;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_DATETIME;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_FORMAT_TIME;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_SHECDULE_TIME;

import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.spi.enums.CommandType;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.spi.utils.DateUtils;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.sql.PreparedStatement;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * parameter parse utils
 */
public class ParameterUtils {

    private static final Logger logger = LoggerFactory.getLogger(ParameterUtils.class);

    private static final String DATE_PARSE_PATTERN = "\\$\\[([^\\$\\]]+)]";

    private static final String DATE_START_PATTERN = "^[0-9]";

    private ParameterUtils() {
        throw new UnsupportedOperationException("Construct ParameterUtils");
    }

    // TODO
    /**
     * create by james on 2022-05-24.
     *
     * 将 $[yyyyMMdd-1] 转换成 20220523
     */
    /**
     * convert parameters place holders
     *
     * @param parameterString parameter
     * @param parameterMap    parameter map
     * @return convert parameters place holders
     */
    public static String convertParameterPlaceholders(String parameterString, Map<String, String> parameterMap) {
        if (StringUtils.isEmpty(parameterString)) {
            return parameterString;
        }
        Date cronTime;
        if (parameterMap != null && !parameterMap.isEmpty()) {
            // replace variable ${} form,refers to the replacement of system variables and custom variables
            parameterString = PlaceholderUtils.replacePlaceholders(parameterString, parameterMap, true);
        }
        if (parameterMap != null && null != parameterMap.get(PARAMETER_DATETIME)) {
            //Get current time, schedule execute time
            String cronTimeStr = parameterMap.get(PARAMETER_DATETIME);
            cronTime = DateUtils.parse(cronTimeStr, PARAMETER_FORMAT_TIME);
        } else {
            cronTime = new Date();
        }
        // replace time $[...] form, eg. $[yyyyMMdd]
        if (cronTime != null) {
            return dateTemplateParse(parameterString, cronTime);
        }
        return parameterString;
    }

    /**
     * new
     * convert parameters place holders
     *
     * @param parameterString parameter
     * @param parameterMap    parameter map
     * @return convert parameters place holders
     */
    public static String convertParameterPlaceholders2(String parameterString, Map<String, String> parameterMap) {
        if (StringUtils.isEmpty(parameterString)) {
            return parameterString;
        }
        //Get current time, schedule execute time
        String cronTimeStr = parameterMap.get(PARAMETER_SHECDULE_TIME);
        Date cronTime = null;

        if (StringUtils.isNotEmpty(cronTimeStr)) {
            cronTime = DateUtils.parse(cronTimeStr, PARAMETER_FORMAT_TIME);

        } else {
            cronTime = new Date();
        }

        // replace variable ${} form,refers to the replacement of system variables and custom variables
        if (!parameterMap.isEmpty()) {
            parameterString = PlaceholderUtils.replacePlaceholders(parameterString, parameterMap, true);
        }

        // replace time $[...] form, eg. $[yyyyMMdd]
        if (cronTime != null) {
            return dateTemplateParse(parameterString, cronTime);
        }
        return parameterString;
    }

    /**
     * set in parameter
     *
     * @param index    index
     * @param stmt     preparedstatement
     * @param dataType data type
     * @param value    value
     * @throws Exception errors
     */
    public static void setInParameter(int index, PreparedStatement stmt, DataType dataType, String value) throws Exception {
        if (dataType.equals(DataType.VARCHAR)) {
            stmt.setString(index, value);
        } else if (dataType.equals(DataType.INTEGER)) {
            stmt.setInt(index, Integer.parseInt(value));
        } else if (dataType.equals(DataType.LONG)) {
            stmt.setLong(index, Long.parseLong(value));
        } else if (dataType.equals(DataType.FLOAT)) {
            stmt.setFloat(index, Float.parseFloat(value));
        } else if (dataType.equals(DataType.DOUBLE)) {
            stmt.setDouble(index, Double.parseDouble(value));
        } else if (dataType.equals(DataType.DATE)) {
            stmt.setDate(index, java.sql.Date.valueOf(value));
        } else if (dataType.equals(DataType.TIME)) {
            stmt.setString(index, value);
        } else if (dataType.equals(DataType.TIMESTAMP)) {
            stmt.setTimestamp(index, java.sql.Timestamp.valueOf(value));
        } else if (dataType.equals(DataType.BOOLEAN)) {
            stmt.setBoolean(index, Boolean.parseBoolean(value));
        }
    }

    /**
     * curing user define parameters
     *
     * @param globalParamMap  global param map
     * @param globalParamList global param list
     * @param commandType     command type
     * @param scheduleTime    schedule time
     * @return curing user define parameters
     */
    public static String curingGlobalParams(Map<String, String> globalParamMap, List<Property> globalParamList,
                                            CommandType commandType, Date scheduleTime) {

        if (globalParamList == null || globalParamList.isEmpty()) {
            return null;
        }

        Map<String, String> globalMap = new HashMap<>();
        if (globalParamMap != null) {
            globalMap.putAll(globalParamMap);
        }
        Map<String, String> allParamMap = new HashMap<>();
        //If it is a complement, a complement time needs to be passed in, according to the task type
        Map<String, String> timeParams = BusinessTimeUtils
            .getBusinessTime(commandType, scheduleTime);

        if (timeParams != null) {
            allParamMap.putAll(timeParams);
        }

        allParamMap.putAll(globalMap);

        Set<Map.Entry<String, String>> entries = allParamMap.entrySet();

        Map<String, String> resolveMap = new HashMap<>();
        for (Map.Entry<String, String> entry : entries) {
            String val = entry.getValue();
            if (val.startsWith("$")) {
                String str = ParameterUtils.convertParameterPlaceholders(val, allParamMap);
                resolveMap.put(entry.getKey(), str);
            }
        }
        globalMap.putAll(resolveMap);

        for (Property property : globalParamList) {
            String val = globalMap.get(property.getProp());
            if (val != null) {
                property.setValue(val);
            }
        }
        return JSONUtils.toJsonString(globalParamList);
    }

    /**
     * $[yyyyMMdd] replace schedule time
     */
    public static String replaceScheduleTime(String text, Date scheduleTime) {
        Map<String, Property> paramsMap = new HashMap<>();
        //if getScheduleTime null ,is current date
        if (null == scheduleTime) {
            scheduleTime = new Date();
        }

        String dateTime = DateUtils.format(scheduleTime, PARAMETER_FORMAT_TIME);
        Property p = new Property();
        p.setValue(dateTime);
        p.setProp(PARAMETER_SHECDULE_TIME);
        paramsMap.put(PARAMETER_SHECDULE_TIME, p);
        text = ParameterUtils.convertParameterPlaceholders2(text, convert(paramsMap));

        return text;
    }

    /**
     * format convert
     *
     * @param paramsMap params map
     * @return Map of converted
     * see org.apache.dolphinscheduler.server.utils.ParamUtils.convert
     */
    public static Map<String, String> convert(Map<String, Property> paramsMap) {
        Map<String, String> map = new HashMap<>();
        Iterator<Map.Entry<String, Property>> iter = paramsMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Property> en = iter.next();
            map.put(en.getKey(), en.getValue().getValue());
        }
        return map;
    }

    // TODO
    /**
     * create by james on 2022-05-24.
     *
     *
     */
    private static String dateTemplateParse(String templateStr, Date date) {
        logger.warn(String.format("\t===>>> dateTemplateParse()"));
        logger.warn(String.format("\t\ttemplateStr=%s, date=%s", templateStr, date.toString()));


        if (templateStr == null) {
            return null;
        }
        Pattern pattern = Pattern.compile(DATE_PARSE_PATTERN);

        StringBuffer newValue = new StringBuffer(templateStr.length());

        Matcher matcher = pattern.matcher(templateStr);

        while (matcher.find()) {
            String key = matcher.group(1);
            if (Pattern.matches(DATE_START_PATTERN, key)) {
                continue;
            }
            String value = TimePlaceholderUtils.getPlaceHolderTime(key, date);
            assert value != null;
            matcher.appendReplacement(newValue, value);
        }

        matcher.appendTail(newValue);

        logger.warn(String.format("\t\tnewValue=%s", newValue));
        return newValue.toString();
    }

}
