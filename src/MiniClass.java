import java.util.ArrayDeque;

public class MiniClass {
    ArrayDeque<Double> deque;
    String[] strArr;
    public MiniClass(ArrayDeque<Double> deque, String[] strArr) {
        this.deque = deque;
        this.strArr = strArr;
    }

    public ArrayDeque<Double> getDeque() {
        return deque;
    }

    public String[] getStrArr() {
        return strArr;
    }
}
