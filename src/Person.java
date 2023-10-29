import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Person {
    String name;
    HashSet<Integer> set;
    HashMap<String, ArrayList<Integer>> map;
    MiniClass miniClass;


    public Person(String name, HashSet<Integer> set, HashMap<String, ArrayList<Integer>> map, MiniClass miniClass) {
        this.name = name;
        this.set = set;
        this.map = map;
        this.miniClass = miniClass;
    }

    public HashSet<Integer> getSet() {
        return set;
    }

    public String getName() {
        return name;
    }

    public HashMap<String, ArrayList<Integer>> getMap() {
        return map;
    }

    public MiniClass getMiniClass() {
        return miniClass;
    }
}
