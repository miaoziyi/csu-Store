package org.csu.csumall.utils;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

    //str->LocalDateTime
    //LocalDateTime->str
    public static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";


    public static LocalDateTime strToDate(String dateTimeStr, String formatStr){
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(formatStr);
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, dateTimeFormatter);
        return localDateTime;
    }

    public static String dateToStr(LocalDateTime localDateTime, String formatStr){
        if(localDateTime == null){
            return StringUtils.EMPTY;
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(formatStr);
        String localDateTimeStr = localDateTime.format(dateTimeFormatter);
        return localDateTimeStr;
    }

    public static LocalDateTime strToDate(String dateTimeStr){
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(STANDARD_FORMAT);
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, dateTimeFormatter);
        return localDateTime;
    }

    public static String dateToStr(LocalDateTime localDateTime){
        if(localDateTime == null){
            return StringUtils.EMPTY;
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(STANDARD_FORMAT);
        String localDateTimeStr = localDateTime.format(dateTimeFormatter);
        return localDateTimeStr;
    }


//    public static void main(String[] args) {
//        System.out.println(DateTimeUtil.dateToStr(LocalDateTime.now(),"yyyy-MM-dd HH:mm:ss"));
//        System.out.println(DateTimeUtil.strToDate("2020-06-09 11:11:11","yyyy-MM-dd HH:mm:ss"));
//    }

}
