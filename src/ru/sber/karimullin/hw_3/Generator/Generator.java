package ru.sber.karimullin.hw_3.Generator;

import java.util.ArrayList;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public interface Generator<T> {
    String generate(T obj);
}




//
//import ru.sber.karimullin.hw_3.Generator.*;
//        import java.util.Collection;
//        import java.util.Iterator;
//        import java.util.Map;
//
//public class GeneratorToJSONFromPerson<T extends Person> implements Generator<T> {
//    @Override
//    public String generate(T elem0_1) {
//        StringBuilder output = new StringBuilder();
//        output.append("{\n");
//        java.util.HashSet elem1_2 = (java.util.HashSet) elem0_1.getSet();
//        output.append("    \"set\": ");
//        output.append("[");
//        boolean isWhileWorked1_2 = false;
//        Collection<?> collection1_2 = (Collection<?>) elem1_2;
//        Iterator<?> iterator1_2 = collection1_2.iterator();
//        while(iterator1_2.hasNext()) {
//            java.lang.Integer elem2_2 = (java.lang.Integer) iterator1_2.next();
//            output.append(elem2_2);
//            output.append(",");
//            isWhileWorked1_2 = true;
//        }
//        if (isWhileWorked1_2) {
//            output.deleteCharAt(output.length() - 1);
//        }
//        output.append("]");
//        output.append("\n");
//        java.lang.String elem1_3 = (java.lang.String) elem0_1.getName();
//        output.append("    \"name\": ");
//        output.append("\"" + elem1_3 + "\"");
//        output.append("\n");
//        java.util.HashMap elem1_4 = (java.util.HashMap) elem0_1.getMap();
//        output.append("    \"map\": ");
//        output.append("{");
//        boolean isWhileWorked1_4 = false;
//        Map<?, ?> map1_4 = (Map<?, ?>) elem1_4;
//        Iterator<?> iterator1_4 = map1_4.entrySet().iterator();
//        while(iterator1_4.hasNext()) {
//            Map.Entry<?, ?> entry1_4 = (Map.Entry<?, ?>) iterator1_4.next();
//            output.append("\"");
//            java.lang.String elem2_4Key = (java.lang.String) entry1_4.getKey();
//            output.append("\"" + elem2_4Key + "\"");
//            output.append("\"");
//            output.append(":");
//            java.util.ArrayList elem2_4Val = (java.util.ArrayList) entry1_4.getValue();
//            output.append("[");
//            boolean isWhileWorked2_4Val = false;
//            Collection<?> collection2_4Val = (Collection<?>) elem2_4Val;
//            Iterator<?> iterator2_4Val = collection2_4Val.iterator();
//            while(iterator2_4Val.hasNext()) {
//                java.lang.Integer elem3_4Val = (java.lang.Integer) iterator2_4Val.next();
//                output.append(elem3_4Val);
//                output.append(",");
//                isWhileWorked2_4Val = true;
//            }
//            if (isWhileWorked2_4Val) {
//                output.deleteCharAt(output.length() - 1);
//            }
//            output.append("]");
//            output.append(",");
//            isWhileWorked1_4 = true;
//        }
//        if (isWhileWorked1_4) {
//            output.deleteCharAt(output.length() - 1);
//        }
//        output.append("}");
//        output.append("\n");
//        output.append("}");
//        return output.toString();
//    }
//}