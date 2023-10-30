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

@SuppressWarnings({"unchecked", "StringConcatenationInsideStringBufferAppend"})
public class Factory {
    // Если obj == null, то такой вызов невалиден, вроде null не парсится в json
    public static <T> Generator<T> factoryToJSON(@NotNull T obj)
            throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException,
                   IllegalAccessException, ClassNotFoundException {
        Class<?> clazz = obj.getClass();
        Counter.clear();

        // Если obj - массив, то мы не сможем его заэкстэндить в объявлении генерируемого класса, поэтому кидаем
        // исключение (но массивы могут быть внутри кастомных классов)
        if (clazz.isArray()) {
            throw new IllegalArgumentException();
        }

        StringBuilder toCompile = new StringBuilder();
        toCompile.append("import ru.sber.karimullin.hw_3.Generator.*;\n");
        // Если класс принтабельный, то он джавовый. Если нет, то подключаем либу с кастомным классом
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
        // Если класс принтабельный, то мы здесь же выведем его оглавление в json, потом сами переведем строку на новую
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

        // Тут вылезает unchecked exception, это нормально поскольку класс не существовал на этапе компиляции и его
        // никак иначе не привести, как supresswarning-ом
        return (Generator<T>) compiledClass.getConstructor().newInstance();
    }

    // Проверка на принтабельность -- джавовый класс с адекватным toString() / коллекция / мапа
    private static boolean clazzIsPrintable(Class<?> clazz) {
        return clazz == String.class || clazz == Integer.class || clazz == Double.class || clazz == Float.class ||
                clazz == Long.class || clazz == Short.class || clazz == Byte.class || clazz == Date.class ||
                clazz == File.class || Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz);
    }

    // Проверка на джавовость с адекватным toString()
    private static boolean clazzIsPrimitive(Class<?> clazz) {
        return clazz == String.class || clazz == Integer.class || clazz == Double.class || clazz == Float.class ||
                clazz == Long.class || clazz == Short.class || clazz == Byte.class || clazz == Date.class ||
                clazz == File.class;
    }

    // Рекурсивно углубляемся во внутренность объектов, которые выводим.
    // obj -- параметр, по которому коллекции находят класс, в который далее надо углубиться.
    // recursionLevelWithSigns -- параметр, нужный чтобы красиво печатать вложенные custom классы
    // recursionLevel -- параметр, нужный чтобы разграничивать индексы параметров внутри одной ветки рекурсии (иначе
    // код не скомпилируется)
    // isOuter -- параметр, по которому мы понимаем что выбирается следующая рекурсивная ветка внутри одного кастомного
    // класса
    // isFromMap -- параметр, по которому мы понимаем что вызов пришел из обработчика мапы, по нему мы понимаем
    // приписывать нам "Key" / "Val" к индексу переменной или нет. Опять же, проблема которую мы так фиксим --
    // возможность компиляции
    // isKey -- параметр, по которому мы понимаем, что же в итоге ставить -- "Key" или "Val"
    private static <T> void recursiveJSON(Class<?> clazz, T obj, StringBuilder toCompile, int recursionLevelWithSigns,
                                      int recursionLevel, boolean isOuter, boolean isFromMap, boolean isKey)
            throws InvocationTargetException, IllegalAccessException {
        // Проверку на clazz == null тут не ставим -- в таком случае просто clazz не primitive. Если obj == null, мы
        // заходим в тело, так как все необходимое для его работы у нас есть -- требуется только clazz; obj требуется
        // обработчикам мапы и коллекций для выяснения вложенных классов
        if (clazzIsPrimitive(clazz)) {
            processPrimitive(clazz, toCompile, recursionLevel, isOuter, isFromMap, isKey);
            return;
        }

        if (clazz == null || obj == null) {
            toCompile.append("output.append(\"null\");\n");
            return;
        }

        if (Collection.class.isAssignableFrom(clazz)) {
            processCollection(obj, toCompile, recursionLevelWithSigns, recursionLevel, isOuter, isFromMap, isKey);
            return;
        }

        if (clazz.isArray()) {
            processArray(clazz, obj, toCompile, recursionLevelWithSigns, recursionLevel, isOuter, isFromMap, isKey);
            return;
        }

        if (Map.class.isAssignableFrom(clazz)) {
            processMap(obj, toCompile, recursionLevelWithSigns, recursionLevel, isOuter, isFromMap, isKey);
            return;
        }

        processCustom(clazz, obj, toCompile, recursionLevelWithSigns, recursionLevel, isOuter, isFromMap, isKey);
    }

    // signal здесь и далее -- стринговый параметр, позволяющий разграничить пространство индексов, чтобы все
    // скомпилировалось успешно
    // Стрингу надо обрамлять в двойные ковычки в джейсоне, отсюда if
    private static void processPrimitive(Class<?> clazz, StringBuilder toCompile, int recursionLevel,
                                             boolean isOuter, boolean isFromMap, boolean isKey) {
        if (isOuter) Counter.counterPlus();
        String signal = Counter.getCounter(recursionLevel, isFromMap, isKey);
        if (clazz == String.class) {
            toCompile.append("output.append(\"\\\"\" + elem" + signal + " + \"\\\"\");\n");
            return;
        }
        toCompile.append("output.append(elem" + signal + ");\n");
    }

    private static <T> void processCollection(T obj, StringBuilder toCompile, int recursionLevelWithSigns,
                                              int recursionLevel, boolean isOuter, boolean isFromMap, boolean isKey)
            throws InvocationTargetException, IllegalAccessException {
        if (isOuter) Counter.counterPlus();
        // Сначала ищем класс, в который углубимся далее по рекурсии. Если коллекция пустая, мы считаем что
        // genericClazz == null, и печатаем null, даже если на деле там и имелся какой-то тип. Такой подход разумен,
        // поскольку в рантайме Class<?> не сохраняют информацию о типах. Но не все, у List например можно достать
        // параметризацию в рантайме, но вот у HashSet<> например с этим проблемы.
        // Флаг gotIt здесь нужен чтобы единожды зайти в цикл, и не обходить всю коллекцию
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

        // signal и signal_plus1 нужны здесь, чтобы склеивать предыдущую итерацию рекурсии и текущую
        String signal = Counter.getCounter(recursionLevel, isFromMap, isKey);
        String signal_plus1 = Counter.getCounter(recursionLevel + 1, isFromMap, isKey);

        // Заводим флаг вхождения в цикл, по которому если что потом сотрем лишнюю запятую, далее приводим переданный
        // elem.._.. к collection, откуда достаем итератор, по которому итерируемся в цикле, приводя явно элементы под
        // итератором к установленному типу. Далее вставляем рекурсивно код, печатающий обработку вложенных классов
        toCompile.append("output.append(\"[\");\n");
        toCompile.append("boolean isWhileWorked" + signal + " = false;\n");
        toCompile.append("Collection<?> collection" + signal + " = (Collection<?>) elem" + signal + ";\n");
        toCompile.append("Iterator<?> iterator" + signal + " = collection" + signal + ".iterator();\n");
        toCompile.append("while(iterator" + signal + ".hasNext()) {\n");
        toCompile.append(genericClazz.toString().split(" ")[1] + " elem" + signal_plus1 +
                " = (" + genericClazz.toString().split(" ")[1] + ") iterator" + signal + ".next();\n");
        recursiveJSON(genericClazz, collection.iterator().next(), toCompile, recursionLevelWithSigns,
                recursionLevel + 1, false, isFromMap, isKey);
        toCompile.append("output.append(\",\");\n");
        toCompile.append("isWhileWorked" + signal + " = true;\n");
        toCompile.append("}\n");
        toCompile.append("if (isWhileWorked" + signal + ") {\n");
        toCompile.append("output.deleteCharAt(output.length() - 1);\n");
        toCompile.append("}\n");
        toCompile.append("output.append(\"]\");\n");
    }

    private static <T> void processArray(Class<?> clazz, T obj, StringBuilder toCompile, int recursionLevelWithSigns,
                                         int recursionLevel, boolean isOuter, boolean isFromMap, boolean isKey)
            throws InvocationTargetException, IllegalAccessException {
        if (isOuter) Counter.counterPlus();
        Class<?> genericClazz = clazz.getComponentType();

        // Если переданный массив пуст, мы говорим что не будем парсить все следующие объекты. Что, конечно, не совсем
        // правильно, ведь тип genericClazz через массив уже установлен, но тогда учитывание специфики массива
        // (что нам необязательно иметь объекты внутри массива чтобы доподлинно сказать что в нем лежит) сильно бы
        // отразилось на всех прочих обработчиках. Пришлось бы вводить флаги прихода из массива, передавать весь трейс
        // классов которые были установленны таким образом. Поэтому будем считать так
        Object[] tempArray = (Object[]) obj;
        if (tempArray.length == 0) {
            toCompile.append("output.append(\"null\");\n");
            return;
        }

        // arrayType надо спарсить в строку, учитывая [L в начале и ; на конце
        String signal = Counter.getCounter(recursionLevel, isFromMap, isKey);
        String signal_plus1 = Counter.getCounter(recursionLevel + 1, isFromMap, isKey);
        String arrayType = clazz.toString().split(" ")[1];
        arrayType = arrayType.substring(2, arrayType.length() - 1);

        // Здесь подход к итерации другой. Мы кастим пришедший элемент к массиву, и в цикле for кастим элемент массива
        // к установленному типу, после чего вызываем рекурсивно код, обрабатывающий установленный тип.
        toCompile.append("output.append(\"[\");\n");
        toCompile.append("boolean isWhileWorked" + signal + " = false;\n");
        toCompile.append(arrayType + "[] array" + signal + " = (" + arrayType + "[]) elem" + signal + ";\n");
        toCompile.append("for (int i = 0; i < array" + signal + ".length; ++i) {\n");
        toCompile.append(genericClazz.toString().split(" ")[1] + " elem" + signal_plus1 +
                " = (" + genericClazz.toString().split(" ")[1] + ") array" + signal + "[i];\n");
        recursiveJSON(genericClazz, tempArray[0], toCompile, recursionLevelWithSigns,
                recursionLevel + 1, false, isFromMap, isKey);
        toCompile.append("output.append(\",\");\n");
        toCompile.append("isWhileWorked" + signal + " = true;\n");
        toCompile.append("}\n");
        toCompile.append("if (isWhileWorked" + signal + ") {\n");
        toCompile.append("output.deleteCharAt(output.length() - 1);\n");
        toCompile.append("}\n");
        toCompile.append("output.append(\"]\");\n");
    }

    // Аналогично пункту про Collection
    private static <T> void processMap(T obj, StringBuilder toCompile, int recursionLevelWithSigns,
                                              int recursionLevel, boolean isOuter, boolean isFromMap, boolean isKey)
            throws InvocationTargetException, IllegalAccessException {
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
        recursiveJSON(genericClazz1, map.entrySet().iterator().next().getKey(), toCompile, recursionLevelWithSigns,
                recursionLevel + 1, false, true, true);
        toCompile.append("output.append(\"\\\"\");\n");

        toCompile.append("output.append(\":\");\n");

        toCompile.append(genericClazz2.toString().split(" ")[1] + " elem" + signal_plus1 + "Val" +
                " = (" + genericClazz2.toString().split(" ")[1] + ") entry" + signal + ".getValue();\n");
        recursiveJSON(genericClazz2, map.entrySet().iterator().next().getValue(), toCompile, recursionLevelWithSigns,
                recursionLevel + 1, false, true, false);

        toCompile.append("output.append(\",\");\n");
        toCompile.append("isWhileWorked" + signal + " = true;\n");
        toCompile.append("}\n");
        toCompile.append("if (isWhileWorked" + signal + ") {\n");
        toCompile.append("output.deleteCharAt(output.length() - 1);\n");
        toCompile.append("}\n");
        toCompile.append("output.append(\"}\");\n");
    }

    // Если флаг isOuter не установлен, значит мы пришли из другого кастомного класса, или из коллекции в которой
    // лежали кастомные классы. Тогда чтобы красиво вывелось надо поставить перевод строки, иначе углубить индекс
    // чтобы отличаться от других кастомных классов в индексном пространстве
    private static <T> void processCustom(Class<?> clazz, T obj, StringBuilder toCompile, int recursionLevelWithSigns,
                                       int recursionLevel, boolean isOuter, boolean isFromMap, boolean isKey)
            throws InvocationTargetException, IllegalAccessException {
        Method[] methods = clazz.getDeclaredMethods();
        if (isOuter) {
            Counter.counterPlus();
        } else {
            toCompile.append("output.append(\"\\n\");\n");
        }
        // Старый индекс всем полям кастомного класса нужен одинаковый, выносим его из цикла
        String signal = Counter.getCounter(recursionLevel, isFromMap, isKey);
        for (Method method : methods) {
            if (method.getName().startsWith("get") && method.getParameterCount() == 0 &&
                    !method.getReturnType().equals(void.class)) {
                Counter.counterPlus();
                String signal_plus1 = Counter.getCounter(recursionLevel + 1, isFromMap, isKey);
                method.setAccessible(true);
                String fieldName = method.getName().substring(3);
                Class<?> anotherClazz = method.getReturnType();
                String anotherClazzType = anotherClazz.toString().split(" ")[1];
                if (anotherClazz.isArray()) {
                    anotherClazzType = anotherClazzType.substring(2, anotherClazzType.length() - 1) + "[]";
                }
                toCompile.append(anotherClazzType + " elem" + signal_plus1 + " = (" +
                        anotherClazzType + ") elem" + signal + ".get" + fieldName + "();\n");

                toCompile.append("output.append(\"" + " ".repeat(recursionLevelWithSigns)+ "\\\"" +
                        fieldName.toLowerCase() + "\\\": \");\n");

                recursiveJSON(anotherClazz, method.invoke(obj), toCompile,
                        recursionLevelWithSigns + 4 + fieldName.length(),
                        recursionLevel + 1, false, isFromMap, isKey);

                toCompile.append("output.append(\"\\n\");\n");
            }
        }
    }

    // Нам нужно разграничить индексы переменных в сгенерированном коде по веткам рекурсии, по вызову в Custom блоке,
    // и по Key / Map парам. Класс статический, значит надо сбрасывать счетчик, разграничивающий вызовы в Custom блоке
    // при новом вызове factoryToJSON().
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
        public static void clear() {
            counter = 0;
        }
    }
}
