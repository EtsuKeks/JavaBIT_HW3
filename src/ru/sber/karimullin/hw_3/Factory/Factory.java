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

// Мы не будем парсить null-значения, вроде он не парсится в json
@SuppressWarnings({"unchecked", "StringConcatenationInsideStringBufferAppend"})
public class Factory {
    public static <T> Generator<T> factoryToJSON(@NotNull T obj)
            throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException,
                   IllegalAccessException, ClassNotFoundException {
        Class<?> clazz = obj.getClass();

        // См. класс Counter ниже. Класс Counter статический, значит помнит нумерацию с последней компиляции. Чтобы
        // склеить с elem0_0, подающимся на вход генерации, надо его обновить
        Counter.clear();

        // Если obj -- массив, то мы тоже выходим из метода с ошибкой, мы не сможем его заэкстендить в объявлении
        // класса, а значит, не сможем потом пользоваться им нормально, ведь он будет принимать любые классы, что не
        // типобезопасно
        if (clazz.isArray()) {
            throw new IllegalArgumentException();
        }

        StringBuilder toCompile = new StringBuilder();
        toCompile.append("import ru.sber.karimullin.hw_3.Generator.*;\n");

        // Если класс принтабельный (то есть не кастомный), то он джавовый (мы считаем, что кастомные классы не могут
        // имплементить коллекции или мапы - в противном случае и Deque с его getFirst, getLast -- тоже кастомный), а
        // значит его можно получить из стандартной либы. Иначе надо подключить пакет
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
        toCompile.append("public String generate(T elem0_0) {\n");
        toCompile.append("StringBuilder output = new StringBuilder();\n");
        toCompile.append("output.append(\"{\\n\");\n");

        // Если класс не кастомный, то мы научимся парсить в json и его. Для этого мы должны сами распечатать его имя,
        // и передать все нужные флаги (см. ниже)
        if (clazzIsPrintable(clazz)) {
            toCompile.append("output.append(\"    \\\"\" + \"" + clazz.getSimpleName() + "\" + \"\\\": \");\n");
            recursiveJSON(clazz, null, obj, toCompile, 8 + clazz.getSimpleName().length(), 0,
                    true, false, false, false);
            toCompile.append("output.append(\"\\n\");\n");
        } else {
            recursiveJSON(clazz, null, obj, toCompile, 4, 0, true,
                    false, false, true);
        }

        toCompile.append("output.append(\"}\\n\");\n");
        toCompile.append("return output.toString();\n");
        toCompile.append("}\n");
        toCompile.append("}\n");

        RuntimeCompiler.compile("GeneratorToJSONFrom" + clazz.getSimpleName(), toCompile.toString());

        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{RuntimeCompiler.getFile().toURI().toURL()});
        Class<?> compiledClass = Class.forName("GeneratorToJSONFrom" + clazz.getSimpleName(),
                true, classLoader);

        // Тут вылетает unchecked cast warning, который можно только отключить вручную, потому что на этапе компиляции
        // класса который мы кастим не было
        return (Generator<T>) compiledClass.getConstructor().newInstance();
    }

    // Проверяем что класс принтабельный == некастомный
    private static boolean clazzIsPrintable(Class<?> clazz) {
        return clazz == String.class || clazz == Integer.class || clazz == Double.class || clazz == Float.class ||
                clazz == Long.class || clazz == Short.class || clazz == Byte.class || clazz == Date.class ||
                clazz == File.class || Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz);
    }

    // Проверяем что класс имеет адекватный toString()
    private static boolean clazzIsPrimitive(Class<?> clazz) {
        return clazz == String.class || clazz == Integer.class || clazz == Double.class || clazz == Float.class ||
                clazz == Long.class || clazz == Short.class || clazz == Byte.class || clazz == Date.class ||
                clazz == File.class;
    }

    // Наша рекурсия будет работать в двух режимах - кастомном и не кастомном, в зависимости от того какой флаг
    // кастомности ей был передан изначально. Дело в том, что если мы передадим например джавовую коллекцию, то generic
    // типы у нее сотрутся, а между тем ее все равно можно запринтить, например, перебирая элементы поданной коллекции
    // и оттуда, при возможности, доставая класс принтящегося объекта. Поэтому мы будем таскать за собой type, и
    // доставать оттуда классы при необходимости, либо доставать их из obj, в зависимости от поданного флага.
    // Clazz - актуальный класс принтящегося объекта
    // Type - то что вернуло getActualParameters на объекте выше по рекурсии
    // obj - объект который мы тащим за собой если не выставлен флаг isCustom
    // recursionLevelWithSigns - для отступов в случае вложенных кастомных классов
    // recursionLevel - для корректного разделения пространства индексов по веткам рекурсии (чтобы скомпилировалось,
    // см. ниже класс Counter)
    // isOuter - флаг, нужный чтобы корректно переносить строки в случае вложенных кастомных классов
    // isFromMap - флаг, нужный чтобы корректно разделять индексное пространство в случае, если мы пришли из мапы
    // (добавляя Val / Key к концу индекса)
    // isKey - чтобы выбирать между Key / Val
    // isCustom - флаг кастомности поданного класса
    private static <T> void recursiveJSON(Class<?> clazz, ParameterizedType type, T obj, StringBuilder toCompile,
                                          int recursionLevelWithSigns, int recursionLevel, boolean isOuter,
                                          boolean isFromMap, boolean isKey, boolean isCustom)
            throws InvocationTargetException, IllegalAccessException {
        if (clazzIsPrimitive(clazz)) {
            processPrimitive(clazz, toCompile, recursionLevel, isFromMap, isKey);
            return;
        }

        // Проверяем на null только в случае некастомности (если флаг isCustom выставлен - необходимости в obj нет,
        // даже если он null, а clazz никогда не null). В случае если isCustom не выставлен проверяем на obj==null
        // только после примитивной секции - clazz никогда не будет null и в этом случае, т.к. мы вручную печатаем
        // null-обработку в следующих секциях, а obj==null неважен для примитивной секции
        if ((clazz == null || obj == null) && !isCustom) {
            toCompile.append("output.append(\"null\");\n");
            return;
        }

        if (Collection.class.isAssignableFrom(clazz)) {
            processCollection(type, obj, toCompile, recursionLevelWithSigns, recursionLevel, isFromMap, isKey, isCustom);
            return;
        }

        if (clazz.isArray()) {
            processArray(clazz, obj, toCompile, recursionLevelWithSigns, recursionLevel, isFromMap, isKey, isCustom);
            return;
        }

        if (Map.class.isAssignableFrom(clazz)) {
            processMap(type, obj, toCompile, recursionLevelWithSigns, recursionLevel, isFromMap, isKey, isCustom);
            return;
        }

        processCustom(clazz, obj, toCompile, recursionLevelWithSigns, recursionLevel, isOuter, isFromMap, isKey,
                isCustom);
    }

    private static <T> void processCustom(Class<?> clazz, T obj, StringBuilder toCompile, int recursionLevelWithSigns,
                                          int recursionLevel, boolean isOuter, boolean isFromMap, boolean isKey,
                                          boolean isCustom)
            throws InvocationTargetException, IllegalAccessException {

        // Смотрим на все публичные методы clazz не null если выставлен isCustom, потому что он никогда не null в
        // таком случае (всегда все достаем из type), а obj==null отсекли в проверках recursiveJSON().
        Method[] methods = clazz.getMethods();

        // Про signal см. в классе Counter, заводить signal и signal_plus1 нужно для корректной склейки глубин рекурсии
        // по индексам
        String signal = Counter.getCounter(recursionLevel, isFromMap, isKey);

        boolean isFirst = true;

        for (Method method : methods) {

            // метод get() - не геттер, getClass() - тоже, остальное считаем геттерами если сигнатура подходящая
            if (method.getName().startsWith("get") && method.getParameterCount() == 0 && method.getName().length() > 3
                    && !method.getReturnType().equals(void.class) && !method.getName().equals("getClass")) {

                // Нужно чтобы корректно разделить индексное пространство между разными ответвлениями рекурсии на
                // уровне одного кастомного класса (вложенного или нет)
                Counter.counterPlus();

                // signal_plus1 нужен каждому ответвлению свой, в отличие от signal, который един для всех ответвлений
                String signal_plus1 = Counter.getCounter(recursionLevel + 1, isFromMap, isKey);
                String fieldName = method.getName().substring(3);
                Class<?> anotherClazz = method.getReturnType();
                String anotherClazzType = anotherClazz.toString().split(" ")[1];

                // Отрезаем у массива "[L" и ";" на конце
                if (anotherClazz.isArray()) {
                    anotherClazzType = anotherClazzType.substring(2, anotherClazzType.length() - 1) + "[]";
                }

                // Бросаем филд в свою переменную в сгенерированном коде
                toCompile.append(anotherClazzType + " elem" + signal_plus1 + " = (" +
                        anotherClazzType + ") elem" + signal + ".get" + fieldName + "();\n");

                // Переезжать строки будут только в конце не первых и не последних кастомов
                if (isFirst) {
                    toCompile.append("output.append(\"" + "\\\"" + fieldName.toLowerCase() + "\\\": \");\n");
                } else {
                    // Добавляем установленное параметром рекурсии отступление для красивого вывода
                    toCompile.append("output.append(\"" + " ".repeat(recursionLevelWithSigns) + "\\\"" +
                            fieldName.toLowerCase() + "\\\": \");\n");
                }

                // Снимаем флаг
                isFirst = false;

                // Если isCustom выставлен, то:
                if (isCustom) {

                    // Если ретерн тайп параметризованный, то это точно либо коллекция, либо мапа, и не более. Тогда
                    // бросаем класс, который достали из возвращаемого типа геттера, сводим ретерн тайп к ParameterizedType
                    // безопасно, obj бросаем нулевой (все равно он не нужен), isOuter снимаем, он пригодился только один
                    // раз, isFromMap и isKey оставляем те же, как и isCustom.
                    if (method.getGenericReturnType() instanceof ParameterizedType) {
                        recursiveJSON(anotherClazz, (ParameterizedType) method.getGenericReturnType(), null,
                                toCompile, recursionLevelWithSigns + 4 + fieldName.length(),
                                recursionLevel + 1, false, isFromMap, isKey, true);
                    } else {

                        // Если непараметризованный, то он и не нужен, бросаем null. Действительно, мы попадем на следующей
                        // глубине либо в примитивный, либо в массив (который и сам все знает про хранимые типы, это же массив),
                        // либо в следующий кастомный класс. Ни в одном из случаев не нужен parameterizedType. В остальном аналогично
                        recursiveJSON(anotherClazz, null, null,
                                toCompile, recursionLevelWithSigns + 4 + fieldName.length(),
                                recursionLevel + 1, false, isFromMap, isKey, true);
                    }
                } else {

                    // Иначе наоборот, тип не нужен, зато нужно то, что возвращает геттер. Exception-а вызов геттера
                    // не вызовет, ведь мы убедились выше, что при не выставленном isCustom здесь obj!=null.
                    recursiveJSON(anotherClazz, null, method.invoke(obj),
                            toCompile, recursionLevelWithSigns + 4 + fieldName.length(),
                            recursionLevel + 1, false, isFromMap, isKey, false);
                }

                toCompile.append("output.append(\"\\n\");\n");
            }
        }

        // Если не выбрались к наружнему кастому, удаляем перенос строки, чтобы закрывающие скобки не съезжали. У всех
        // это наслоиться, кроме вызывающего в самом начале рекурсию персона.
        if (methods.length != 0 && !isOuter) {
            toCompile.append("output.delete(output.length() - 1, output.length());\n");
        }
    }

    // Все уже дано, если логика вызовов цела, просто печатаем. Если дана была стринга, добавляем ей ""
    private static void processPrimitive(Class<?> clazz, StringBuilder toCompile, int recursionLevel,
                                         boolean isFromMap, boolean isKey) {
        String signal = Counter.getCounter(recursionLevel, isFromMap, isKey);
        if (clazz == String.class) {
            toCompile.append("output.append(\"\\\"\" + elem" + signal + " + \"\\\"\");\n");
            return;
        }
        toCompile.append("output.append(elem" + signal + ");\n");
    }

    private static <T> void processCollection(ParameterizedType type, T obj, StringBuilder toCompile,
                                              int recursionLevelWithSigns, int recursionLevel, boolean isFromMap,
                                              boolean isKey, boolean isCustom)
            throws InvocationTargetException, IllegalAccessException {
        Class<?> genericClazz = null;
        Collection<?> collection = null;

        // По выстроенной логике выше тут нам либо дали корректный type в случае, если isCustom стоит, либо нам надо
        // самим извлечь из коллекции genericType, а если коллекция пуста, самим отпечатать null.
        // Флаг gotIt нужен, чтобы не проходить всю коллекцию целиком. Достаточно погрузиться в нее один раз.
        if (isCustom) {

            // ParametrizedType не сводится к Class<?>. Нам же нужен только RawType(), пусть он и не хранит всей
            // нужной информации про вложенный далее классы, главное, что ее хранит type, который мы корректно
            // кидаем далее
            if (type.getActualTypeArguments()[0] instanceof ParameterizedType) {
                genericClazz = (Class<?>) ((ParameterizedType) type.getActualTypeArguments()[0]).getRawType();
            } else {
                genericClazz = (Class<?>) type.getActualTypeArguments()[0];
            }
        } else {
            collection = (Collection<?>) obj;
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
        }

        String signal = Counter.getCounter(recursionLevel, isFromMap, isKey);
        String signal_plus1 = Counter.getCounter(recursionLevel + 1, isFromMap, isKey);

        // Флаг isWhileWorked нужен для удаления последней запятой, если присутствует
        toCompile.append("output.append(\"[\");\n");
        toCompile.append("boolean isWhileWorked" + signal + " = false;\n");
        toCompile.append("Collection<?> collection" + signal + " = (Collection<?>) elem" + signal + ";\n");
        toCompile.append("Iterator<?> iterator" + signal + " = collection" + signal + ".iterator();\n");
        toCompile.append("while(iterator" + signal + ".hasNext()) {\n");
        toCompile.append(genericClazz.toString().split(" ")[1] + " elem" + signal_plus1 +
                " = (" + genericClazz.toString().split(" ")[1] + ") iterator" + signal + ".next();\n");

        // Если флаг isCustom выставлен, то:
        if (isCustom) {

            // Если вложенный тип не примитивный, кидаем найденный genericClazz и сведенный непримитивный вложенный тип
            // , остальное аналогично
            if (type.getActualTypeArguments()[0] instanceof ParameterizedType) {
                recursiveJSON(genericClazz, (ParameterizedType) type.getActualTypeArguments()[0], null, toCompile,
                        recursionLevelWithSigns,recursionLevel + 1, false, isFromMap, isKey,
                        true);

                // Если примитивный, то type нам больше и не понадобится, остальное аналогично
            } else {
                recursiveJSON(genericClazz, null, null, toCompile, recursionLevelWithSigns,
                        recursionLevel + 1, false, isFromMap, isKey, true);
            }

            // Иначе, кидаем найденный genericClazz и извлеченный следующий объект
        } else {
            recursiveJSON(genericClazz, null, collection.iterator().next(), toCompile, recursionLevelWithSigns,
                    recursionLevel + 1, false, isFromMap, isKey, false);
        }

        toCompile.append("output.append(\",\");\n");
        toCompile.append("isWhileWorked" + signal + " = true;\n");
        toCompile.append("}\n");
        toCompile.append("if (isWhileWorked" + signal + ") {\n");
        toCompile.append("output.deleteCharAt(output.length() - 1);\n");
        toCompile.append("}\n");
        toCompile.append("output.append(\"]\");\n");
    }

    private static <T> void processArray(Class<?> clazz, T obj, StringBuilder toCompile, int recursionLevelWithSigns,
                                         int recursionLevel, boolean isFromMap, boolean isKey, boolean isCustom)
            throws InvocationTargetException, IllegalAccessException {

        // Массив сразу хранит в себе вложенный тип.
        Class<?> genericClazz = clazz.getComponentType();
        Object[] tempArray = (Object[]) obj;

        String signal = Counter.getCounter(recursionLevel, isFromMap, isKey);
        String signal_plus1 = Counter.getCounter(recursionLevel + 1, isFromMap, isKey);
        String arrayType = clazz.toString().split(" ")[1];

        // Обрезаем мусор из названия
        arrayType = arrayType.substring(2, arrayType.length() - 1);

        toCompile.append("output.append(\"[\");\n");
        toCompile.append("boolean isWhileWorked" + signal + " = false;\n");
        toCompile.append(arrayType + "[] array" + signal + " = (" + arrayType + "[]) elem" + signal + ";\n");
        toCompile.append("for (int i = 0; i < array" + signal + ".length; ++i) {\n");
        toCompile.append(genericClazz.toString().split(" ")[1] + " elem" + signal_plus1 +
                " = (" + genericClazz.toString().split(" ")[1] + ") array" + signal + "[i];\n");

        // Если isCustom выставлен, то:
        if (isCustom) {

            // Кидаем извлеченный из описания массива тип, type кидаем нулевым, все равно в джаве создать массив
            // дженериков нельзя, а значит он не пригодится, остальное аналогично
            recursiveJSON(genericClazz, null, null, toCompile, recursionLevelWithSigns,
                    recursionLevel + 1, false, isFromMap, isKey, true);
        } else {
            // Иначе, если можем - извлекаем из массива объект, и кидаем дальше для повторного извлечения информации
            // о классе
            if (tempArray.length != 0) {
                recursiveJSON(genericClazz, null, tempArray[0], toCompile, recursionLevelWithSigns,
                        recursionLevel + 1, false, isFromMap, isKey, false);
            } else {

                // Если нет, все равно можем прокинуть null - genericClazz-то уже есть, может, если следующий по
                // вложению примитивный, obj нам и не понадобится
                recursiveJSON(genericClazz, null, null, toCompile, recursionLevelWithSigns,
                        recursionLevel + 1, false, isFromMap, isKey, false);
            }
        }

        toCompile.append("output.append(\",\");\n");
        toCompile.append("isWhileWorked" + signal + " = true;\n");
        toCompile.append("}\n");
        toCompile.append("if (isWhileWorked" + signal + ") {\n");
        toCompile.append("output.deleteCharAt(output.length() - 1);\n");
        toCompile.append("}\n");
        toCompile.append("output.append(\"]\");\n");
    }

    private static <T> void processMap(ParameterizedType type, T obj, StringBuilder toCompile,
                                       int recursionLevelWithSigns, int recursionLevel, boolean isFromMap,
                                       boolean isKey, boolean isCustom)
            throws InvocationTargetException, IllegalAccessException {
        Class<?> genericClazz1 = null;
        Class<?> genericClazz2 = null;
        Map<?, ?> map = null;

        // Аналогично коду из collection
        if (isCustom) {
            if (type.getActualTypeArguments()[0] instanceof ParameterizedType) {
                genericClazz1 = (Class<?>) ((ParameterizedType) type.getActualTypeArguments()[0]).getRawType();
            } else {
                genericClazz1 = (Class<?>) type.getActualTypeArguments()[0];
            }

            if (type.getActualTypeArguments()[1] instanceof ParameterizedType) {
                genericClazz2 = (Class<?>) ((ParameterizedType) type.getActualTypeArguments()[1]).getRawType();
            } else {
                genericClazz2 = (Class<?>) type.getActualTypeArguments()[1];
            }
        } else {
            map = (Map<?, ?>) obj;
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
        }

        String signal = Counter.getCounter(recursionLevel, isFromMap, isKey);
        String signal_plus1 = Counter.getCounter(recursionLevel + 1, isFromMap, isKey);

        toCompile.append("output.append(\"{\");\n");
        toCompile.append("boolean isWhileWorked" + signal + " = false;\n");
        toCompile.append("Map<?, ?> map" + signal + " = (Map<?, ?>) elem" + signal + ";\n");
        toCompile.append("Iterator<?> iterator" + signal + " = map" + signal + ".entrySet().iterator();\n");
        toCompile.append("while(iterator" + signal + ".hasNext()) {\n");
        toCompile.append("Map.Entry<?, ?> entry" + signal + " = (Map.Entry<?, ?>) iterator" + signal + ".next();\n");

        // Для связки индексов со следующим уровнем рекурсии, добавляем вручную здесь Key к индексу
        toCompile.append("output.append(\"\\\"\");\n");
        toCompile.append(genericClazz1.toString().split(" ")[1] + " elem" + signal_plus1 + "Key" +
                " = (" + genericClazz1.toString().split(" ")[1] + ") entry" + signal + ".getKey();\n");

        // Аналогично
        if (isCustom) {
            if (type.getActualTypeArguments()[0] instanceof ParameterizedType) {
                recursiveJSON(genericClazz1, (ParameterizedType) type.getActualTypeArguments()[0], null, toCompile,
                        recursionLevelWithSigns,recursionLevel + 1, false, true,
                        true, true);
            } else {
                recursiveJSON(genericClazz1, null, null, toCompile, recursionLevelWithSigns,
                        recursionLevel + 1, false, true, true, true);
            }
        } else {
            recursiveJSON(genericClazz1, null, map.entrySet().iterator().next().getKey(), toCompile,
                    recursionLevelWithSigns,recursionLevel + 1, false, true, true,
                    false);
        }

        toCompile.append("output.append(\"\\\"\");\n");
        toCompile.append("output.append(\":\");\n");
        toCompile.append(genericClazz2.toString().split(" ")[1] + " elem" + signal_plus1 + "Val" +
                " = (" + genericClazz2.toString().split(" ")[1] + ") entry" + signal + ".getValue();\n");

        // Аналогично
        if (isCustom) {
            if (type.getActualTypeArguments()[1] instanceof ParameterizedType) {
                recursiveJSON(genericClazz2, (ParameterizedType) type.getActualTypeArguments()[1], null, toCompile,
                        recursionLevelWithSigns,recursionLevel + 1, false, true,
                        false, true);
            } else {
                recursiveJSON(genericClazz2, null, null, toCompile, recursionLevelWithSigns,
                        recursionLevel + 1, false, true, false, true);
            }
        } else {
            recursiveJSON(genericClazz2, null, map.entrySet().iterator().next().getValue(), toCompile,
                    recursionLevelWithSigns,recursionLevel + 1, false, true, false,
                    false);
        }

        toCompile.append("output.append(\",\");\n");
        toCompile.append("isWhileWorked" + signal + " = true;\n");
        toCompile.append("}\n");
        toCompile.append("if (isWhileWorked" + signal + ") {\n");
        toCompile.append("output.deleteCharAt(output.length() - 1);\n");
        toCompile.append("}\n");
        toCompile.append("output.append(\"}\");\n");
    }

    // Код не скомпилируется, если переменные будут иметь одинаковые названия. Значит, нам надо разделить индексные
    // пространства по:
    // 1) Глубине рекурсии (recusrionLevel)
    // 2) Разветвлениям, порождающимся в custom классах
    // 3) Val / Key значениям, если мы пришли на следующий уровень рекурсии из мапы.
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
