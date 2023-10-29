package ru.sber.karimullin.hw_3.Factory;

import ru.sber.karimullin.hw_3.Generator.*;
import ru.sber.karimullin.hw_3.RuntimeCompiler.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class Factory {
    public static <T> Generator<T> factoryToJSON(@NotNull T obj) throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        StringBuilder toCompile = new StringBuilder();
        toCompile.append("import ru.sber.karimullin.hw_3.Generator.*;\n");
        toCompile.append("import " + obj.getClass().toString().split(" ")[1] + ";\n");
        toCompile.append("import java.lang.reflect.*;\n");
        toCompile.append("import java.util.Collection;\n");
        toCompile.append("\n");
        toCompile.append("public class GeneratorToJSONFrom" + obj.getClass().getSimpleName() +
                "<T extends " + obj.getClass().toString().split(" ")[1] + "> implements Generator<T> {\n");
        toCompile.append("@Override\n");
        toCompile.append("public String generate(T obj) {\n");
        toCompile.append("StringBuilder output = new StringBuilder();\n");
        toCompile.append("output.append(\"{\\n\");\n");
        if (objIsPrintable(obj.getClass())) {
            toCompile.append("output.append(\"    \\\"" + obj.getClass().getSimpleName() + "\\\": " + ifObjIsPrintable(obj) +
                    "\\n\");\n");
        } else {
            recursiveJSON(obj, toCompile, 0);
        }
        toCompile.append("output.append(\"}\");\n");
        toCompile.append("return output.toString();\n");
        toCompile.append("}\n");
        toCompile.append("}\n");

        RuntimeCompiler.compile("GeneratorToJSONFrom" + obj.getClass().getSimpleName(), toCompile.toString());

        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{RuntimeCompiler.getFile().toURI().toURL()});
        Class<?> compiledClass = Class.forName("GeneratorToJSONFrom" + obj.getClass().getSimpleName(),
                true, classLoader);
        return (Generator<T>) compiledClass.getConstructor().newInstance();
    }

    private static <T> boolean objIsPrintable(Class<?> clazz) {
        return isPrimitiveType(clazz) || clazz.isArray() || Collection.class.isAssignableFrom(clazz) ||
                Map.class.isAssignableFrom(clazz);
    }

    private static boolean isPrimitiveType(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz == String.class ||
                clazz == Integer.class ||
                clazz == Double.class ||
                clazz == Float.class ||
                clazz == Long.class ||
                clazz == Short.class ||
                clazz == Byte.class ||
                clazz == Date.class ||
                clazz == File.class;
    }

    private static <T> String ifObjIsPrintable(T obj) {
        if (obj == null) {
            return "null";
        }
        Class<?> clazz = obj.getClass();

        if (isPrimitiveType(clazz)) {
            if (obj instanceof String) {
                return "\"" + obj + "\"";
            }
            return obj.toString();
        }

        if (clazz.isArray() || Collection.class.isAssignableFrom(clazz)) {
            StringBuilder temp = new StringBuilder("[");
            Collection<?> elements = (Collection<?>) obj;

            for (Object element : elements) {
                temp.append(ifObjIsPrintable(element)).append(",");
            }

            if (!temp.isEmpty()) {
                temp.deleteCharAt(temp.length() - 1);
            }

            temp.append("]");
            return temp.toString();
        }

        if (Map.class.isAssignableFrom(clazz)) {
            StringBuilder temp = new StringBuilder("{");
            Map<?, ?> entries = (Map<?, ?>) obj;

            for (Map.Entry<?, ?> entry : entries.entrySet()) {
                temp.append("\"").append(entry.getKey()).append("\":").append(ifObjIsPrintable(entry.getValue())).
                        append(",");
            }

            if (!temp.isEmpty()) {
                temp.deleteCharAt(temp.length() - 1);
            }

            temp.append("}");
            return temp.toString();
        }
        return "";
    }

    private static <T> void recursiveJSON(T obj, StringBuilder toCompile, int recursionLevel) {

    }
//
//    public static <T> Generator<T> factoryToXML(T obj) {
//    }
}
