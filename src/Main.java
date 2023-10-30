import ru.sber.karimullin.hw_3.Factory.*;
import ru.sber.karimullin.hw_3.Generator.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String exmp01 = "DAW";
        String exmp02 = "ADekar";
        Generator<String> generator0 = Factory.factoryToJSON(exmp01);
        System.out.print(generator0.generate(exmp02));

        HashSet<HashSet<Integer>> exmp1 = new HashSet<>();
        HashSet<Integer> temp1 = new HashSet<>();
        temp1.add(325);
        temp1.add(3425);
        exmp1.add(temp1);
        HashSet<Integer> temp2 = new HashSet<>();
        temp2.add(3235);
        temp2.add(2314);
        exmp1.add(temp2);

        HashSet<HashSet<Integer>> exmp2 = new HashSet<>();
        HashSet<Integer> temp3 = new HashSet<>();
        temp3.add(42545);
        temp3.add(34425);
        exmp2.add(temp3);
        HashSet<Integer> temp4 = new HashSet<>();
        temp4.add(31342);
        temp4.add(24545);
        exmp2.add(temp4);

        Generator<HashSet<HashSet<Integer>>> generator1 = Factory.factoryToJSON(exmp1);
        System.out.println(generator1.generate(exmp2));

        HashMap<Integer, HashSet<String>> exmp3 = new HashMap<>();
        HashSet<String> temp5 = new HashSet<>();
        temp5.add("dwa");
        temp5.add("reerw");
        exmp3.put(3, temp5);

        HashMap<Integer, HashSet<String>> exmp4 = new HashMap<>();
        HashSet<String> temp6 = new HashSet<>();
        temp6.add("ealkdsr");
        temp6.add("dwagt");
        exmp4.put(1, temp6);

        Generator<HashMap<Integer, HashSet<String>>> generator2 = Factory.factoryToJSON(exmp3);
        System.out.println(generator2.generate(exmp4));

        HashSet<Integer> temp7 = new HashSet<>();
        temp7.add(5);
        temp7.add(53);
        HashMap<String, ArrayList<Integer>> temp8 = new HashMap<>();
        ArrayList<Integer> list1 = new ArrayList<>();
        list1.add(453);
        list1.add(54);
        list1.add(34298);
        temp8.put("DAWDWD", list1);
        ArrayDeque<Double> deque1 = new ArrayDeque<>();
        deque1.push(4.64564);
        deque1.push(43.539);
        deque1.push(5.232094);
        String[] arr1 = new String[]{"dwa", "ter3", "wadr3"};
        MiniClass temp9 = new MiniClass(deque1, arr1);
        Person exmp5 = new Person("SADW", temp7, temp8, temp9);

        HashSet<Integer> temp10 = new HashSet<>();
        temp10.add(8327);
        temp10.add(8753);
        HashMap<String, ArrayList<Integer>> temp11 = new HashMap<>();
        ArrayList<Integer> list2 = new ArrayList<>();
        list2.add(5978);
        list2.add(430);
        list2.add(8743);
        temp11.put("WDAJWDAm,vfsv", list2);
        ArrayDeque<Double> deque2 = new ArrayDeque<>();
        deque2.push(4.64564);
        deque2.push(43.539);
        deque2.push(5.232094);
        String[] arr2 = new String[]{"dwa", "ter3", "wadr3"};
        MiniClass temp12 = new MiniClass(deque2, arr2);
        Person exmp6 = new Person("darge", temp10, temp11, temp12);

        Generator<Person> generator = Factory.factoryToJSON(exmp5);
        System.out.println(generator.generate(exmp6));
    }
}