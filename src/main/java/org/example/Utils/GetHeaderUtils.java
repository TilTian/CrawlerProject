package org.example.Utils;

import org.example.Entity.XiaomiDataEntity;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GetHeaderUtils {
    public static List<String> getHeader() {
        XiaomiDataEntity xiaomiDataEntity = new XiaomiDataEntity();
        List<Field> fields = Arrays.asList(xiaomiDataEntity.getClass().getDeclaredFields());
        return fields.stream().map(Field::getName).collect(Collectors.toList());
    }
}
