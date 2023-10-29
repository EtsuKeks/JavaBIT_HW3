import ru.sber.karimullin.hw_3.Factory.*;
import ru.sber.karimullin.hw_3.Generator.*;
import ru.sber.karimullin.hw_3.RuntimeCompiler.RuntimeCompiler;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
//        HashSet<HashSet<Integer>> integer = new HashSet<>();
//        HashSet<Integer> temp = new HashSet<>();
//        temp.add(325);
//        temp.add(3425);
//        integer.add(temp);
//        HashSet<Integer> temp2 = new HashSet<>();
//        temp2.add(3235);
//        temp2.add(2314);
//        integer.add(temp2);
//
//        HashSet<HashSet<Integer>> integer2 = new HashSet<>();
//        HashSet<Integer> temp3 = new HashSet<>();
//        temp3.add(42545);
//        temp3.add(34425);
//        integer2.add(temp3);
//        HashSet<Integer> temp4 = new HashSet<>();
//        temp4.add(31342);
//        temp4.add(24545);
//        integer2.add(temp4);

        HashMap<Integer, String> integer = new HashMap<>();
        integer.put(4, "as");
        integer.put(5, "adw");

        HashMap<Integer, String> integer2 = new HashMap<>();
        integer2.put(423, "dwadaw");
        integer2.put(32, "dwadawf");

        Generator<HashMap<Integer, String>> generator = Factory.factoryToJSON(integer);
        System.out.println(generator.generate(integer2));
    }
}

class Person {
    Person(String name) {
        this.name = name;
    }
    String name;
}
