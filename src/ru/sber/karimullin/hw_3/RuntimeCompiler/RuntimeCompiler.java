package ru.sber.karimullin.hw_3.RuntimeCompiler;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;

public class RuntimeCompiler {
    // Заводим инстанс своего ручного компилятора
    private static final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    // Заводим инстанс своего ручного файлового менеджера, который укажет компилятору куда класть .class файлы
    private static final StandardJavaFileManager fileManager = compiler.getStandardFileManager(
            null, null, null);

    // Создаем абстракцию своей ручной временной директории, после чего создаем саму директорию в статик инициалайзере,
    // если она еще не была создана. В случае, если уже была создана - не очищаем, а переписывваем имеющиеся файлы.
    private static final File outputDir = new File("temp");

    static {
        if (!outputDir.isDirectory()) {
            outputDir.mkdirs();
        }

        try {
            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singletonList(outputDir));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File getFile() {
        return outputDir.getAbsoluteFile();
    }

    // Метод порубит старую версию .class файла с тем же названием, что мы требуем от него теперь
    public static void compile(String name, String toCompile) throws IOException {
        // Код взят из доки https://docs.oracle.com/en/java/javase/17/docs/api/java.compiler/javax/tools/
        // JavaCompiler.html#getTask(java.io.Writer,javax.tools.JavaFileManager,javax.tools.DiagnosticListener,
        // java.lang.Iterable,java.lang.Iterable,java.lang.Iterable)

        String[] elems = toCompile.split("\\s+");

        // Создаем абстракцию .java файла с записанным внутри кодом toCompile, его надо будет скормить компилятору
        JavaFileObject fileObject = new JavaSourceFromString(name, toCompile);

        // Заводим абстракцию того что компилятор должен скомпилировать, после чего передаем это на компиляцию вместе
        // с файловым менеджером, который скажет компилятору потом куда класть результат компиляции
        Iterable<? extends JavaFileObject> compilationUnits = Collections.singletonList(fileObject);
        compiler.getTask(null, fileManager, null, null, null, compilationUnits).call();
        // Тут у нас уже есть .class файл в temp директории
    }

    // Тут все прям как в указанной доке. Какой то утилитарный класс, представляет из себя абстракцию
    // .java файла с записанным внутри кодом javaCode, его надо будет скормить компилятору.
    static class JavaSourceFromString extends SimpleJavaFileObject {
        final String code;
        JavaSourceFromString(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }
}
