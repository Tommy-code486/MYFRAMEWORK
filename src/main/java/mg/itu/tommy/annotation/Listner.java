package mg.itu.rivaldo.controller;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mg.itu.rivaldo.annotation.Url;


@WebListener
public class Listner implements ServletContextListener {
    public void contextInitialized(ServletContextEvent sce) {
        try {
            ServletContext context = sce.getServletContext();
            String packageName = context.getInitParameter("packageNames");
            Util util = new Util();
            Map<UrlType, Mapping> mappingUrls = new HashMap<>();
            List<String> controllerClassNames =
                    util.getListClassNamesWithAnnotation(
                            context,
                            packageName,
                            Url.class,
                            mappingUrls
                    );

            context.setAttribute("mappingUrls", mappingUrls);
            context.setAttribute("controllerClassNames", controllerClassNames);

            System.out.println("Framework initialisé.");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
    
