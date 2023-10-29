package ru.sber.karimullin.hw_3.Factory;

import ru.sber.karimullin.hw_3.Generator.*;
import ru.sber.karimullin.hw_3.RuntimeCompiler.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unchecked")
public class Factory {
    public static <T> Generator<T> factoryToJSON(@NotNull T obj) throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Class<?> clazz = obj.getClass();
        StringBuilder toCompile = new StringBuilder();
        toCompile.append("import ru.sber.karimullin.hw_3.Generator.*;\n");
        if (clazzIsPrintable(clazz)) {
            toCompile.append("import " + clazz.toString().split(" ")[1] + ";\n");
        } else if (!clazzIsPrintable(clazz) && !clazz.getPackageName().isEmpty()) {
            toCompile.append("import " + clazz.getPackageName() + "." + clazz.toString().split(" ")[1] + ";\n");
        }
        toCompile.append("import java.util.Collection;\n");
        toCompile.append("import java.util.Iterator;\n");
        toCompile.append("import java.util.Map;\n");
        toCompile.append("\n");
        toCompile.append("public class GeneratorToJSONFrom" + clazz.getSimpleName() + "<T extends " +
                clazz.toString().split(" ")[1] + "> implements Generator<T> {\n");
        toCompile.append("@Override\n");
        toCompile.append("public String generate(T elem0_1) {\n");
        toCompile.append("StringBuilder output = new StringBuilder();\n");
        toCompile.append("output.append(\"{\\n\");\n");
        if (clazzIsPrintable(clazz)) {
            toCompile.append("output.append(\"    \\\"\" + \"" + clazz.getSimpleName() + "\" + \"\\\": \");\n");
            recursiveJSON(clazz, obj, toCompile, 8 + clazz.getSimpleName().length(), 0,
                    true, false, false);
            toCompile.append("output.append(\"\\n\");\n");
        } else {
            recursiveJSON(clazz, obj, toCompile, 4, 0,
                    true, false, false);
        }
        toCompile.append("output.append(\"}\");\n");
        toCompile.append("return output.toString();\n");
        toCompile.append("}\n");
        toCompile.append("}\n");

        RuntimeCompiler.compile("GeneratorToJSONFrom" + clazz.getSimpleName(), toCompile.toString());

        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{RuntimeCompiler.getFile().toURI().toURL()});
        Class<?> compiledClass = Class.forName("GeneratorToJSONFrom" + clazz.getSimpleName(),
                true, classLoader);
        return (Generator<T>) compiledClass.getConstructor().newInstance();
    }

    private static boolean clazzIsPrintable(Class<?> clazz) {
        return clazz == String.class || clazz == Integer.class || clazz == Double.class || clazz == Float.class ||
                clazz == Long.class || clazz == Short.class || clazz == Byte.class || clazz == Date.class ||
                clazz == File.class || Collection.class.isAssignableFrom(clazz) ||
                Map.class.isAssignableFrom(clazz);
    }

    private static boolean clazzIsPrimitive(Class<?> clazz) {
        return clazz == String.class || clazz == Integer.class || clazz == Double.class || clazz == Float.class ||
                clazz == Long.class || clazz == Short.class || clazz == Byte.class || clazz == Date.class ||
                clazz == File.class;
    }

    private static <T> void recursiveJSON(Class<?> clazz, T obj, StringBuilder toCompile, int recursionLevelWithSigns,
                                      int recursionLevel, boolean isOuter, boolean isFromMap, boolean isKey) throws InvocationTargetException, IllegalAccessException {
        if (clazzIsPrimitive(clazz)) {
            processPrimitive(clazz, obj, toCompile, recursionLevelWithSigns, recursionLevel, isOuter, isFromMap, isKey);
            return;
        }

        if (clazz == null || obj == null) {
            toCompile.append("output.append(\"null\");\n");
            return;
        }

        if (Collection.class.isAssignableFrom(clazz)) {
            processCollection(clazz, obj, toCompile, recursionLevelWithSigns, recursionLevel, isOuter, isFromMap, isKey);
            return;
        }

        if (Map.class.isAssignableFrom(clazz)) {
            processMap(clazz, obj, toCompile, recursionLevelWithSigns, recursionLevel, isOuter, isFromMap, isKey);
            return;
        }

        processCustom(clazz, obj, toCompile, recursionLevelWithSigns, recursionLevel, isOuter, isFromMap, isKey);
    }

    private static <T> void processPrimitive(Class<?> clazz, T obj, StringBuilder toCompile, int recursionLevelWithSigns,
                                             int recursionLevel, boolean isOuter, boolean isFromMap, boolean isKey) {
        if (isOuter) Counter.counterPlus();
        String signal = Counter.getCounter(recursionLevel, isFromMap, isKey);
        if (clazz == String.class) {
            toCompile.append("output.append(\"\\\"\" + elem" + signal + " + \"\\\"\");\n");
            return;
        }
        toCompile.append("output.append(elem" + signal + ");\n");
    }

    private static <T> void processCollection(Class<?> clazz, T obj, StringBuilder toCompile, int recursionLevelWithSigns,
                                              int recursionLevel, boolean isOuter, boolean isFromMap, boolean isKey) throws InvocationTargetException, IllegalAccessException {
        if (isOuter) Counter.counterPlus();
        Class<?> genericClazz = null;
        Collection<?> collection = (Collection<?>) obj;
        Iterator<?> iterator = collection.iterator();
        boolean gotIt = false;
        while (iterator.hasNext() && !gotIt) {
            genericClazz = collection.iterator().next().getClass();
            gotIt = true;
        }

        if (genericClazz == null) {
            toCompile.append("output.append(\"null\");\n");
            return;
        }

        String signal = Counter.getCounter(recursionLevel, isFromMap, isKey);
        String signal_plus1 = Counter.getCounter(recursionLevel + 1, isFromMap, isKey);

        toCompile.append("output.append(\"[\");\n");
        toCompile.append("boolean isWhileWorked" + signal + " = false;\n");
        toCompile.append("Collection<?> collection" + signal + " = (Collection<?>) elem" + signal + ";\n");
        toCompile.append("Iterator<?> iterator" + signal + " = collection" + signal + ".iterator();\n");
        toCompile.append("while(iterator" + signal + ".hasNext()) {\n");
        toCompile.append(genericClazz.toString().split(" ")[1] + " elem" + signal_plus1 +
                " = (" + genericClazz.toString().split(" ")[1] + ") iterator" + signal + ".next();\n");
        recursiveJSON(genericClazz, collection.iterator().next(), toCompile, recursionLevelWithSigns, recursionLevel + 1, false, isFromMap, isKey);
        toCompile.append("output.append(\",\");\n");
        toCompile.append("isWhileWorked" + signal + " = true;\n");
        toCompile.append("}\n");
        toCompile.append("if (isWhileWorked" + signal + ") {\n");
        toCompile.append("output.deleteCharAt(output.length() - 1);\n");
        toCompile.append("}\n");
        toCompile.append("output.append(\"]\");\n");
    }

    private static <T> void processMap(Class<?> clazz, T obj, StringBuilder toCompile, int recursionLevelWithSigns,
                                              int recursionLevel, boolean isOuter, boolean isFromMap, boolean isKey) throws InvocationTargetException, IllegalAccessException {
        if (isOuter) Counter.counterPlus();
        Class<?> genericClazz1 = null;
        Class<?> genericClazz2 = null;
        Map<?, ?> map = (Map<?, ?>) obj;
        Iterator<?> iterator = map.entrySet().iterator();
        boolean gotIt = false;
        while (iterator.hasNext() && !gotIt) {
            genericClazz1 = map.entrySet().iterator().next().getKey().getClass();
            genericClazz2 = map.entrySet().iterator().next().getValue().getClass();
            gotIt = true;
        }

        if (genericClazz1 == null) {
            toCompile.append("output.append(\"null,null\");\n");
            return;
        }

        String signal = Counter.getCounter(recursionLevel, isFromMap, isKey);
        String signal_plus1 = Counter.getCounter(recursionLevel + 1, isFromMap, isKey);

        toCompile.append("output.append(\"{\");\n");
        toCompile.append("boolean isWhileWorked" + signal + " = false;\n");
        toCompile.append("Map<?, ?> map" + signal + " = (Map<?, ?>) elem" + signal + ";\n");
        toCompile.append("Iterator<?> iterator" + signal + " = map" + signal + ".entrySet().iterator();\n");
        toCompile.append("while(iterator" + signal + ".hasNext()) {\n");
        toCompile.append("Map.Entry<?, ?> entry" + signal + " = (Map.Entry<?, ?>) iterator" + signal + ".next();\n");

        toCompile.append("output.append(\"\\\"\");\n");
        toCompile.append(genericClazz1.toString().split(" ")[1] + " elem" + signal_plus1 + "Key" +
                " = (" + genericClazz1.toString().split(" ")[1] + ") entry" + signal + ".getKey();\n");
        recursiveJSON(genericClazz1, map.entrySet().iterator().next().getKey(), toCompile, recursionLevelWithSigns, recursionLevel + 1, false, true, true);
        toCompile.append("output.append(\"\\\"\");\n");

        toCompile.append("output.append(\":\");\n");

        toCompile.append(genericClazz2.toString().split(" ")[1] + " elem" + signal_plus1 + "Val" +
                " = (" + genericClazz2.toString().split(" ")[1] + ") entry" + signal + ".getValue();\n");
        recursiveJSON(genericClazz2, map.entrySet().iterator().next().getValue(), toCompile, recursionLevelWithSigns, recursionLevel + 1, false, true, false);

        toCompile.append("output.append(\",\");\n");
        toCompile.append("isWhileWorked" + signal + " = true;\n");
        toCompile.append("}\n");
        toCompile.append("if (isWhileWorked" + signal + ") {\n");
        toCompile.append("output.deleteCharAt(output.length() - 1);\n");
        toCompile.append("}\n");
        toCompile.append("output.append(\"}\");\n");
    }

    private static <T> void processCustom(Class<?> clazz, T obj, StringBuilder toCompile, int recursionLevelWithSigns,
                                       int recursionLevel, boolean isOuter, boolean isFromMap, boolean isKey) throws InvocationTargetException, IllegalAccessException {
        Method[] methods = clazz.getDeclaredMethods();
        if (isOuter) {
            Counter.counterPlus();
        } else {
            toCompile.append("output.append(\"\\n\");\n");
        }
        String signal = Counter.getCounter(recursionLevel, isFromMap, isKey);
        for (Method method : methods) {
            if (method.getName().startsWith("get") && method.getParameterCount() == 0 &&
                    !method.getReturnType().equals(void.class)) {
                Counter.counterPlus();
                String signal_plus1 = Counter.getCounter(recursionLevel + 1, isFromMap, isKey);
                method.setAccessible(true);
                String fieldName = method.getName().substring(3);
                Class<?> anotherClazz = method.getReturnType();
                toCompile.append(anotherClazz.toString().split(" ")[1] + " elem" + signal_plus1 + " = (" +
                        anotherClazz.toString().split(" ")[1] + ") elem" + signal + ".get" + fieldName + "();\n");

                toCompile.append("output.append(\"" + " ".repeat(recursionLevelWithSigns)+ "\\\"" +
                        fieldName.toLowerCase() + "\\\": \");\n");

                recursiveJSON(anotherClazz, method.invoke(obj), toCompile,
                        recursionLevelWithSigns + 4 + fieldName.length(),
                        recursionLevel + 1, false, isFromMap, isKey);

                toCompile.append("output.append(\"\\n\");\n");
            }
        }
    }

    static class Counter {
        private static int counter = 0;

        public static void counterPlus() {
            counter++;
        }

        public static String getCounter(int recursionLevel, boolean isFromMap, boolean isKey) {
            if (isFromMap) {
                if (isKey) {
                    return recursionLevel + "_" + counter + "Key";
                } else {
                    return recursionLevel + "_" + counter + "Val";
                }
            }
            return recursionLevel + "_" + counter;

        }
    }
}
