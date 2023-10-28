package ru.sber.karimullin.hw_3.Generator;

import java.util.ArrayList;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public interface Generator<T> {
    String generate(T obj);
}

class GeneratorToJSON<T extends Person> implements Generator<T> {
    @Override
    public String generate(T obj) {
        StringBuilder output = new StringBuilder();
        output.append("{");
        if (obj == null) {
            output.append("    null");
            output.append("}");
            return output.toString();
        }
        Class cl = obj.getClass();
        if (cl == String.class) return (String) obj;

        if (cl.isArray())
        {
            String r = cl.getComponentType() + "[]{";
            for (int i = 0; i < Array.getLength(obj); i++)
            {
                if (i > 0) r += ",";
                Object val = Array.get(obj, i);
                if (cl.getComponentType().isPrimitive()) r += val;
                else r += generate(val);
            }
            return r + "}";
        }
        String r = cl.getName();
        do
        {
            r += "[";
            Field[] fields = cl.getDeclaredFields();
            AccessibleObject.setAccessible(fields, true);
            for (Field f : fields)
            {
                if (!Modifier.isStatic(f.getModifiers()))
                {
                    if (!r.endsWith("[")) r += ",";
                    r += f.getName() + "=";
                    Class t = f.getType();
                    Object val = f.get(obj);
                    if (t.isPrimitive()) r += val;
                    else r += toString(val);
    }
            }
        }
        r += "]";
        cl = cl.getSuperclass();
    }
while (cl != null);
return r;
}
}
}

class Person {
    ArrayList<Integer> list = new ArrayList<>();
    int[] arr = new int[10];
    String name;

}