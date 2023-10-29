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
        toCompile.append("import " + clazz.toString().split(" ")[1] + ";\n");
        toCompile.append("import java.util.Collection;\n");
        toCompile.append("import java.util.Iterator;\n");
        toCompile.append("\n");
        toCompile.append("public class GeneratorToJSONFrom" + clazz.getSimpleName() + "<T extends " +
                clazz.toString().split(" ")[1] + "> implements Generator<T> {\n");
        toCompile.append("@Override\n");
        toCompile.append("public String generate(T elem0) {\n");
        toCompile.append("StringBuilder output = new StringBuilder();\n");
        toCompile.append("output.append(\"{\\n\");\n");
        if (clazzIsPrintable(clazz)) {
            toCompile.append("output.append(\"    \\\"\" + \"" + clazz.getSimpleName() + "\" + \"\\\": \");\n");
            recursiveJSON(clazz, obj, toCompile, 8 + clazz.getSimpleName().length(), 0);
            toCompile.append("output.append(\"\\n\");\n");
        } else {
            recursiveJSON(clazz, obj, toCompile, 4, 0);
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

    private static <T> void recursiveJSON(Class<?> clazz, T obj, StringBuilder toCompile, int recursionLevelWithSigns,
                                      int recursionLevel) {
        if (clazz == null) {
            toCompile.append("output.append(\"null\");\n");
        }

        if (clazz == String.class || clazz == Integer.class || clazz == Double.class || clazz == Float.class ||
                clazz == Long.class || clazz == Short.class || clazz == Byte.class || clazz == Date.class ||
                clazz == File.class) {
            if (clazz == String.class) {
                toCompile.append("output.append(\"\\\"\" + elem" + recursionLevel + " + \"\\\"\");\n");
                return;
            }
            toCompile.append("output.append(elem" + recursionLevel + ");\n");
        }

        if (Collection.class.isAssignableFrom(clazz)) {
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

            toCompile.append("output.append(\"[\");\n");
            toCompile.append("boolean isWhileWorkedCollection" + recursionLevel + " = false;\n");
            toCompile.append("Collection<?> collection" + recursionLevel + " = (Collection<?>) elem" + recursionLevel + ";\n");
            toCompile.append("Iterator<?> iteratorCollection" + recursionLevel + " = collection" + recursionLevel + ".iterator();\n");
            toCompile.append("while(iteratorCollection" + recursionLevel + ".hasNext()) {\n");
            toCompile.append(genericClazz.toString().split(" ")[1] + " elem" + (recursionLevel + 1) +
                    " = (" + genericClazz.toString().split(" ")[1] + ") iteratorCollection" + recursionLevel + ".next();\n");
            recursiveJSON(genericClazz, collection.iterator().next(), toCompile, recursionLevelWithSigns, recursionLevel + 1);
            toCompile.append("output.append(\",\");\n");
            toCompile.append("isWhileWorkedCollection" + recursionLevel + " = true;\n");
            toCompile.append("}\n");
            toCompile.append("if (isWhileWorkedCollection" + recursionLevel + ") {\n");
            toCompile.append("output.deleteCharAt(output.length() - 1);\n");
            toCompile.append("}\n");
            toCompile.append("output.append(\"]\");\n");
        }

        if (Map.class.isAssignableFrom(clazz)) {
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

            toCompile.append("output.append(\"{\");\n");
            toCompile.append("boolean isWhileWorkedMap" + recursionLevel + " = false;\n");
            toCompile.append("Map<?, ?> map" + recursionLevel + " = (Map<?, ?>) elem" + recursionLevel + ";\n");
            toCompile.append("Iterator<?> iteratorMap" + recursionLevel + " = map" + recursionLevel + ".entrySet().iterator();\n");
            toCompile.append("while(iteratorMap" + recursionLevel + ".hasNext()) {\n");
            toCompile.append("Map.Entry<?, ?> entryMap" + recursionLevel + " = (Map.Entry<?, ?>) iteratorMap" + recursionLevel + ".next();\n");

            toCompile.append("output.append(\"\\\"\");\n");
            toCompile.append(genericClazz1.toString().split(" ")[1] + " elem" + (recursionLevel + 1) +
                    " = (" + genericClazz1.toString().split(" ")[1] + ") entryMap" + recursionLevel + ".getKey();\n");
            recursiveJSON(genericClazz1, map.entrySet().iterator().next().getKey(), toCompile, recursionLevelWithSigns, recursionLevel + 1);
            toCompile.append("output.append(\"\\\"\");\n");

            toCompile.append("output.append(\":\");\n");

            toCompile.append(genericClazz2.toString().split(" ")[1] + " elem" + (recursionLevel + 1) +
                    " = (" + genericClazz2.toString().split(" ")[1] + ") entryMap" + recursionLevel + ".getValue();\n");
            recursiveJSON(genericClazz2, map.entrySet().iterator().next().getValue(), toCompile, recursionLevelWithSigns, recursionLevel + 1);

            toCompile.append("output.append(\",\");\n");
            toCompile.append("isWhileWorkedMap" + recursionLevel + " = true;\n");
            toCompile.append("}\n");
            toCompile.append("if (isWhileWorkedMap" + recursionLevel + ") {\n");
            toCompile.append("output.deleteCharAt(output.length() - 1);\n");
            toCompile.append("}\n");
            toCompile.append("output.append(\"}\");\n");
        }

//        return "...";
//
//        if (objIsPrintable(obj.getClass())) {
//            toCompile.append(ifObjIsPrintable(obj));
//        }
//
//        Class<?> clazz = obj.getClass();
//
//        Method[] methods = clazz.getDeclaredMethods();
//        for (Method method : methods) {
//            if (method.getName().startsWith("get") && method.getParameterCount() == 0 &&
//                    !method.getReturnType().equals(void.class)) {
//                String fieldName = method.getName().substring(3);
//                toCompile.append("output.append(\"" + " ".repeat(recursionLevelWithSigns)+ "\"" + fieldName + "\": ");
//                Class<?> returnType = method.getReturnType();
//                if (isPrimitiveType(returnType)) {
//                    toCompile.append(ifObjIsPrintable())
//                }
//                toCompile.append()
//                recursiveJSON();
//                toCompile.append("output.append(\");\n");
//            }
//        }
    }

    class Counter {
        private int counter = 0;
        public int getCounter() {
            counter++;
            return counter;
        }
    }
}
