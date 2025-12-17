package com.m1raynee.telegram.utils;

import java.lang.reflect.Field;

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
        update = (Update) getPrivateField(targetObject, "update");
        storage = (StateMemoryStorage) getPrivateField(targetObject, "storage");
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
}
