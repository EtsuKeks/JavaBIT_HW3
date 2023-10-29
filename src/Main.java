import ru.sber.karimullin.hw_3.Factory.*;
import ru.sber.karimullin.hw_3.Generator.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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

//        HashMap<Integer, HashSet<String>> integer = new HashMap<>();
//        HashSet<String> temp1 = new HashSet<>();
//        temp1.add("dwa");
//        temp1.add("reerw");
//        integer.put(3, temp1);
//
//        HashMap<Integer, HashSet<String>> integer2 = new HashMap<>();
//        HashSet<String> temp2 = new HashSet<>();
//        temp2.add("ealkdsr");
//        temp2.add("dwagt");
//        integer2.put(1, temp2);

        HashSet<Integer> temp1 = new HashSet<>();
        temp1.add(5);
        temp1.add(53);
        HashMap<String, ArrayList<Integer>> temp2 = new HashMap<>();
        ArrayList<Integer> list1 = new ArrayList<>();
        list1.add(453);
        list1.add(54);
        list1.add(34298);
        temp2.put("DAWDWD", list1);

        ArrayDeque<Double> deque1 = new ArrayDeque<>();
        deque1.push(4.64564);
        deque1.push(43.539);
        deque1.push(5.232094);

        MiniClass temp5 = new MiniClass(deque1);

        Person integer = new Person("SADW", temp1, temp2, temp5);

        HashSet<Integer> temp3 = new HashSet<>();
        temp3.add(8327);
        temp3.add(8753);
        HashMap<String, ArrayList<Integer>> temp4 = new HashMap<>();
        ArrayList<Integer> list2 = new ArrayList<>();
        list2.add(5978);
        list2.add(430);
        list2.add(8743);
        temp4.put("WDAJWDAm,vfsv", list2);

        ArrayDeque<Double> deque2 = new ArrayDeque<>();
        deque2.push(4.64564);
        deque2.push(43.539);
        deque2.push(5.232094);

        MiniClass temp6 = new MiniClass(deque2);

        Person integer2 = new Person("darge", temp3, temp4, temp6);

        Generator<Person> generator = Factory.factoryToJSON(integer);
        System.out.println(generator.generate(integer2));
    }
}