package ru.sber.karimullin.hw_3.Factory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import ru.sber.karimullin.hw_3.Generator.*;
import ru.sber.karimullin.hw_3.RuntimeCompiler.RuntimeCompiler;

public class Factory {
    public static <T> Generator<T> factoryToJSON(T obj) throws IOException {
        StringBuilder toCompile = new StringBuilder();
        toCompile.append("import ru.sber.karimullin.hw_3.Generator.*;\n");
        toCompile.append("import java.lang.reflect.*;\n");
        toCompile.append("import java.util.Collection;\n");
        toCompile.append("\n");
        toCompile.append("public class GeneretorToJSON<T extends " + obj.getClass() + "> implements Generator<T> {\n");
        toCompile.append("    @Override\n");
        toCompile.append("    public String generate(T obj) {\n");
        toCompile.append("        StringBuilder output = new StringBuilder();\n");
        toCompile.append("        output.append(\"{\\n\");\n");
        toCompile.append("        recursiveJSON(obj, output, 0);\n");
        toCompile.append("        output.append(\"}\");\n");
        toCompile.append("        return output.toString();\n");
        toCompile.append("    }\n");
        toCompile.append("    private void recursiveJSON(T obj, StringBuilder output, int recursionLevel) {\n");
        toCompile.append("        recursionLevel++;\n");
        toCompile.append("        String str = \"    \".repeat(recursionLevel);\n");
        toCompile.append("        if (obj == null) {\n");
        toCompile.append("            output.append(str + \"null\\n\");\n");
        toCompile.append("            return;\n");
        toCompile.append("        }\n");
        toCompile.append("        Class clazz = obj.getClass();\n");
        toCompile.append("        if (clazz.isPrimitive() || clazz == String.class || clazz == Integer.class ||\n");
        toCompile.append("        Double.class || Float.class || Long.class || Short.class || Byte.class || \n");
        toCompile.append("        Date.class || File.class) {\n");
        toCompile.append("            if (obj instanseof String) {\n");
        toCompile.append("                output.append(str + \"\\\"\" + clazz.getShortName() + \":\\\" \" + \"\\\"\" + obj + \"\\\"\");\n");
        toCompile.append("                return;\n");
        toCompile.append("            }\n");
        toCompile.append("            output.append(str + \"\\\"\" + clazz.getShortName() + \":\\\" \" + obj.toString());\n");
        toCompile.append("            return;\n");
        toCompile.append("        }\n");
        toCompile.append("        if (clazz.isArray() || Collection.class.isAssignableFrom(clazz)) {\n");
        toCompile.append("            Collection<?> elements = (Collection<?>) obj \n");
        toCompile.append("            for (Object element : elements) {\n");
        toCompile.append("                output.append(str + \"\\\"\" + clazz.getShortName() + \":\\\"\");\n");
        toCompile.append("                recursiveJSON(element, output, recursionLevel);\n");
        toCompile.append("            \n");
        toCompile.append("            \n");
        toCompile.append("            \n");
        toCompile.append("            \n");
        toCompile.append("        \n");
        toCompile.append("        \n");
        toCompile.append("        \n");
        toCompile.append("        \n");
        toCompile.append("        \n");
        toCompile.append("        \n");
        toCompile.append("        Method[] methods = clazz.getDeclaredMethods();\n");
        toCompile.append("        for (Method method : methods) {\n");
        toCompile.append("            if (method.getName().startsWith(\"get\") && method.getParameterCount() == 0) {\n");
        toCompile.append("                String fieldName = method.getName().substring(3);\n");
        toCompile.append("                \n");
        toCompile.append("                \n");
        toCompile.append("                \n");
        toCompile.append("            \n");
        toCompile.append("        \n");
        toCompile.append("        \n");
        toCompile.append("        \n");
        toCompile.append("        \n");
        toCompile.append("    \n");
        toCompile.append("    }\n");
        toCompile.append("}\n");

        RuntimeCompiler.compile(toCompile.toString());


    }

    public static <T> Generator<T> factoryToXML(T obj) {

    }

    private static <T> void recursiveJSON(T obj, Class<?> clazz, StringBuilder toCompile) {


    }

    private static <T> void recursiveToFieldsJSON(T obj, StringBuilder toCompile) {

    }
}

class JsonObjectParser {
    public static String parseObjectToJson(Object object) {
        if (object == null) {
            return "null";
        }

        Class<?> clazz = object.getClass();
        if (isPrimitiveType(clazz)) {
            return primitiveToJson(object);
        }

        if (clazz.isArray() || Collection.class.isAssignableFrom(clazz)) {
            return collectionToJson(object);
        }

        if (Map.class.isAssignableFrom(clazz)) {
            return mapToJson(object);
        }

        StringBuilder json = new StringBuilder("{");
        Method[] methods = clazz.getMethods();

        for (Method method : methods) {
            if (isGetter(method)) {
                String fieldName = method.getName().substring(3);
                try {
                    Object fieldValue = method.invoke(object);
                    String fieldValueJson = parseObjectToJson(fieldValue);
                    json.append("\"").append(fieldName).append("\":").append(fieldValueJson).append(",");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (json.charAt(json.length() - 1) == ',') {
            json.deleteCharAt(json.length() - 1);
        }

        json.append("}");
        return json.toString();
    }

    private static boolean isPrimitiveType(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz == String.class ||
                clazz == Integer.class ||
                clazz == Double.class ||
                clazz == Boolean.class;
    }

    private static String primitiveToJson(Object value) {
        if (value instanceof String) {
            return "\"" + value + "\"";
        }
        return value.toString();
    }

    private static String collectionToJson(Object collection) {
        if (collection == null) {
            return "null";
        }

        StringBuilder json = new StringBuilder("[");
        Collection<?> elements = (Collection<?>) collection;

        for (Object element : elements) {
            json.append(parseObjectToJson(element)).append(",");
        }

        if (json.charAt(json.length() - 1) == ',') {
            json.deleteCharAt(json.length() - 1);
        }

        json.append("]");
        return json.toString();
    }

    private static String mapToJson(Object map) {
        if (map == null) {
            return "null";
        }

        StringBuilder json = new StringBuilder("{");
        Map<?, ?> entries = (Map<?, ?>) map;

        for (Map.Entry<?, ?> entry : entries.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\":").append(parseObjectToJson(entry.getValue())).append(",");
        }

        if (json.charAt(json.length() - 1) == ',') {
            json.deleteCharAt(json.length() - 1);
        }

        json.append("}");
        return json.toString();
    }

    private static boolean isGetter(Method method) {
        return method.getName().startsWith("get") &&
                method.getParameterCount() == 0 &&
                !method.getReturnType().equals(void.class);
    }
//
//    public static void main(String[] args) {
//        // Example usage:
//        SampleObject sample = new SampleObject("John", 30, true);
//        String json = parseObjectToJson(sample);
//        System.out.println(json);
//    }
}
