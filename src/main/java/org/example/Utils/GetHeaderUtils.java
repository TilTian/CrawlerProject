package org.example.Utils;

import org.example.Entity.XiaomiDataEntity;
import org.example.Entity.XiaomiMainCommentDataEntity;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GetHeaderUtils {
    public static List<String> getXiaomiDataHeader() {
        XiaomiDataEntity xiaomiDataEntity = new XiaomiDataEntity();
        List<Field> fields = Arrays.asList(xiaomiDataEntity.getClass().getDeclaredFields());
        return fields.stream().map(Field::getName).collect(Collectors.toList());
    }

    public static List<String> getXiaomiMainCommentDataHeader() {
        XiaomiMainCommentDataEntity xiaomiMainCommentDataEntity = new XiaomiMainCommentDataEntity();
        List<Field> fields = Arrays.asList(xiaomiMainCommentDataEntity.getClass().getDeclaredFields());
        return fields.stream().map(Field :: getName).collect(Collectors.toList());
    }
}
