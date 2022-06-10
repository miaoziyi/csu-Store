package org.csu.csumall.utils;



import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.apache.commons.lang3.StringUtils;

//import com.fasterxml.jackson.databind.ObjectMapper;

import org.csu.csumall.entity.User;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson工具类
 *
 * @author suddev
 * @create 2017-11-25 2:18 PM
 **/
public class JSONUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 对象字段全部列入
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);

        // 取消默认转换timestamps形式
//        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        // 忽略空bean转json的错误
//        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS,false);

        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 统一日期格式yyyy-MM-dd HH:mm:ss
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));


        objectMapper.registerModule(javaTimeModule);

        // 忽略在json字符串中存在,但是在java对象中不存在对应属性的情况
//        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,false);

        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    /**
     * Object转json字符串
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> String obj2String(T obj){
        if (obj == null){
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            System.out.println("Parse object to String error");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Object转json字符串并格式化美化
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> String obj2StringPretty(T obj){
        if (obj == null){
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            System.out.println("Parse object to String error");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * string转object
     * @param str json字符串
     * @param clazz 被转对象class
     * @param <T>
     * @return
     */
    public static <T> T string2Obj(String str,Class<T> clazz){
        if (StringUtils.isEmpty(str) || clazz == null){
            return null;
        }
        try {
            return clazz.equals(String.class)? (T) str :objectMapper.readValue(str,clazz);
        } catch (IOException e) {
            System.out.println("Parse String to Object error");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * string转object
     * @param str json字符串
     * @param typeReference 被转对象引用类型
     * @param <T>
     * @return
     */
    public static <T> T string2Obj(String str, TypeReference<T> typeReference){
        if (StringUtils.isEmpty(str) || typeReference == null){
            return null;
        }
        try {
            return (T)(typeReference.getType().equals(String.class)? str :objectMapper.readValue(str,typeReference));
        } catch (IOException e) {
            System.out.println("Parse String to Object error");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * string转object 用于转为集合对象
     * @param str json字符串
     * @param collectionClass 被转集合class
     * @param elementClasses 被转集合中对象类型class
     * @param <T>
     * @return
     */
    public static <T> T string2Obj(String str,Class<?> collectionClass,Class<?>... elementClasses){
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass,elementClasses);
        try {
            return objectMapper.readValue(str,javaType);
        } catch (IOException e) {
            System.out.println("Parse String to Object error");
            e.printStackTrace();
            return null;
        }
    }

//    测试
    public static void main(String[] args) {
        User user = new User();
        user.setId(1);
        user.setCreateTime(LocalDateTime.now());
        String s = JSONUtil.obj2String(user);
        System.out.println(s);
        User user1 = JSONUtil.string2Obj(s, User.class);
        System.out.println(user1);
    }
}
