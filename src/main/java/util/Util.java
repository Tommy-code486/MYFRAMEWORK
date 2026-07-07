package mg.itu.rivaldo.controller;
import jakarta.servlet.ServletContext;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.lang.reflect.Method;
import mg.itu.rivaldo.annotation.UrlMethod;
import java.util.Map;
import java.net.URL;



public class Util {

    public List<String> getListClassNamesWithAnnotation( ServletContext context,String packageName,Class annotationClass, Map<UrlType, Mapping> mappingUrls) {
        List<String> result = new ArrayList<>();
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String packagePath = packageName.replace('.', '/');
            Enumeration<URL> resources = classLoader.getResources(packagePath);

            while (resources.hasMoreElements()) {
                    URL resource = resources.nextElement();

                if (resource.getProtocol().equals("file")) {
                    File directory = new File(resource.toURI());
                    scanDirectory(directory, packageName, annotationClass, result, mappingUrls);
                }
            }
        } catch (java.net.URISyntaxException | java.io.IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void scanDirectory(File directory,String packageName,Class annotationClass,List<String> result, Map<UrlType, Mapping> mappingUrls) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file,packageName + "." + file.getName(),annotationClass,result,mappingUrls);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().replace(".class", "");
                try {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(annotationClass)) {
                        result.add(clazz.getName());
                        for (Method method : clazz.getDeclaredMethods()) {
                            if (method.isAnnotationPresent(UrlMethod.class)) {
                                UrlMethod annotation = method.getAnnotation(UrlMethod.class);
                                UrlType key = new UrlType(annotation.value(),annotation.type());
                                if(mappingUrls.containsKey(key)) {
                                    throw new RuntimeException("Duplicate mapping for URL: " + annotation.value() + " and type: " + annotation.type());
                                }
                                mappingUrls.put(key, new Mapping(clazz, method));
                            }
                        }
                    }

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}