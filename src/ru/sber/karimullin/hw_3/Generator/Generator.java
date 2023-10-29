package ru.sber.karimullin.hw_3.Generator;

import java.util.ArrayList;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public interface Generator<T> {
    String generate(T obj);
}

