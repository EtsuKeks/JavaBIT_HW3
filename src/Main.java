import ru.sber.karimullin.hw_3.Factory.*;
import ru.sber.karimullin.hw_3.Generator.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        HashSet<Integer> set = new HashSet<>();
        set.add(52);
        set.add(52);
        set.add(52);
        set.add(25);
        Generator<HashSet<Integer>> generator = Factory.factoryToJSON(set);
        System.out.println(generator.generate(set));
    }
}

class Person {
    public String getName() {
        return name;
    }

    public HashSet<Integer> getSet() {
        return set;
    }

    public ArrayList<ArrayDeque<Long>> getList() {
        return list;
    }

    HashSet<Integer> set;
    String name;
    ArrayList<ArrayDeque<Long>> list;
}
