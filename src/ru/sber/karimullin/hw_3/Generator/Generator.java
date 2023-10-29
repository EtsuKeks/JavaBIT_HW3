package ru.sber.karimullin.hw_3.Generator;

import java.util.ArrayList;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public interface Generator<T> {
    String generate(T obj);
}

import ru.sber.karimullin.hw_3.Generator.*;
        import java.util.HashMap;
        import java.util.Collection;
        import java.util.Iterator;

public class GeneratorToJSONFromHashMap<T extends java.util.HashMap> implements Generator<T> {
    @Override
    public String generate(T elem0) {
        StringBuilder output = new StringBuilder();
        output.append("{\n");
        output.append("    \"" + "HashMap" + "\": ");
        output.append("{");
        boolean isWhileWorkedMap0 = false;
        Map<?, ?> map0 = (Map<?, ?>) elem0;
        Iterator<?> iteratorMap0 = map0.entrySet().iterator();
        while(iteratorMap0.hasNext()) {
            Map.Entry<?, ?> entryMap0 = (Map.Entry<?, ?>) iteratorMap0.next();
            output.append("\"");
            java.lang.Integer elem1 = (java.lang.Integer) entryMap0.getKey();
            output.append(elem1);
            output.append("\"");
            output.append(":");
            java.lang.String elem1 = (java.lang.String) entryMap0.getValue();
            output.append("\"" + elem1 + "\"");
            output.append(",");
            isWhileWorkedMap0 = true;
        }
        if (isWhileWorkedMap0) {
            output.deleteCharAt(output.length() - 1);
        }
        output.append("}");
        output.append("\n");
        output.append("}");
        return output.toString();
    }
}
