package util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;



public class Utilitaire {

    public static List<String> ScanneClass(String packageName) throws Exception {
        List<String> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(path);

        if (resource == null) {
            return classes;
        }
        File directory = new File(resource.getFile());
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    if (file.isFile() && fileName.endsWith(".class")) {
                        String className = packageName + "." + fileName.substring(0, fileName.length() - 6);
                        classes.add(className);
                    }
                }
            }
        }
        return classes;
    }
}
