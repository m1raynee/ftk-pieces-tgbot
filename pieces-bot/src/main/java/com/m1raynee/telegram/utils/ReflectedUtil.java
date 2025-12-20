package com.m1raynee.telegram.utils;

import java.lang.reflect.Field;
import java.util.Map;

import io.github.natanimn.telebof.states.StateMemoryStorage;
import io.github.natanimn.telebof.types.updates.Update;
import lombok.Getter;

@Getter
public class ReflectedUtil<T> {
    private Update update;
    private StateMemoryStorage storage;
    private T targetObject;

    public ReflectedUtil(T targetObject) {
        this.targetObject = targetObject;
        update = (Update) getMaybePrivateField("update");
        storage = (StateMemoryStorage) getMaybePrivateField("storage");
    }

    public static Object getPrivateField(Object targetObject, String fieldName) {
        if (targetObject == null) {
            throw new IllegalArgumentException("Целевой объект (targetObject) не может быть null.");
        }

        Class<?> targetClass = targetObject.getClass();
        Field privateField = null;

        try {
            // 1. Поиск поля в классе
            privateField = targetClass.getDeclaredField(fieldName);

            // 2. Установка доступности (обход проверки доступа)
            privateField.setAccessible(true);

            // 3. Получение значения поля из объекта
            return privateField.get(targetObject);

        } catch (NoSuchFieldException e) {
            // Оборачиваем Checked Exception в Unchecked, чтобы упростить использование
            throw new RuntimeException("Поле '" + fieldName + "' не найдено в классе " + targetClass.getName(), e);
        } catch (IllegalAccessException e) {
            // Это маловероятно после setAccessible(true), но лучше обработать
            throw new RuntimeException("Не удалось получить доступ к полю '" + fieldName + "'", e);
        } finally {
            if (privateField != null) {
                privateField.setAccessible(false);
            }
        }
    }

    public static Object getMaybePrivateField(Object targetObject, String fieldName) {
        try {
            return getPrivateField(targetObject, fieldName);
        } catch (RuntimeException e) {
            return null;
        }
    }

    public Object getPrivateField(String fieldName) {
        return getPrivateField(targetObject, fieldName);
    }

    public Object getMaybePrivateField(String fieldName) {
        return getMaybePrivateField(targetObject, fieldName);
    }

    public String getStateName(long id) {
        return storage.getName(id);
    }

    @SuppressWarnings("unchecked")
    public void transplantData(Long id, Map<String, Object> newData) {
        var ref = new ReflectedUtil<>(storage);
        var userStorage = (Map<Long, Map<String, Object>>) ref.getMaybePrivateField("userStorage");
        if (userStorage != null) {
            userStorage.put(id, newData);
        }
    }
}
