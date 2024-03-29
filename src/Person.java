import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Person {
    String name;
    HashSet<Integer> set;
    HashMap<String, ArrayList<Integer>> map;
    HashSet<MiniClass> miniSet;
    MiniClass miniClass;

    public Person(String name, HashSet<Integer> set, HashMap<String, ArrayList<Integer>> map, HashSet<MiniClass> miniSet, MiniClass miniClass) {
        this.name = name;
        this.set = set;
        this.map = map;
        this.miniSet = miniSet;
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

    public HashSet<MiniClass> getMiniSet() {
        return miniSet;
    }
    public MiniClass getMiniClass() {
        return miniClass;
    }
}
