package com.eastrobot.kbs.media.common.util.ali;

import com.alibaba.fastjson.JSONObject;

import java.util.Optional;

public class FastJson {

    public static <T> Optional<T> opt(JSONObject json, String key, Class<T> clazz) {
        switch (clazz.getSimpleName()) {
            case "Integer":
                return (Optional<T>) Optional.ofNullable(json.getInteger(key));
            case "String":
                return (Optional<T>) Optional.ofNullable(json.getString(key));
            case "Double":
                return (Optional<T>) Optional.ofNullable(json.getDouble(key));
            case "Byte":
                return (Optional<T>) Optional.ofNullable(json.getByte(key));
            case "Boolean":
                return (Optional<T>) Optional.ofNullable(json.getBoolean(key));
            case "Date":
                return (Optional<T>) Optional.ofNullable(json.getDate(key));
            case "JSONObject":
                return (Optional<T>) Optional.ofNullable(json.getJSONObject(key));
            case "JSONArray":
                return (Optional<T>) Optional.ofNullable(json.getJSONArray(key));

        }
        return Optional.empty();
    }

}
