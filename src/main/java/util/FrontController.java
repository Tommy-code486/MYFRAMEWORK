package util;

import java.io.IOException;
import java.lang.reflect.Method;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import mg.itu.tommy.annotation.Controller;
import mg.itu.tommy.annotation.Url;


public class FrontController extends HttpServlet {

    public FrontController() {
        super();
    }
    private List<String> listControllers = new ArrayList<>();
    private List<Method> listUrls = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        try {
            List<String> classesAll = Utilitaire.ScanneClass("controller");
            for (String className : classesAll) {
                Class<?> clazz = Class.forName(className);
                Method[] methods = clazz.getDeclaredMethods();
                if (clazz.isAnnotationPresent(Controller.class)) {
                    for (Method method : methods) {
                        if(method.isAnnotationPresent(Url.class)) {
                            Url urlAnnotation = method.getAnnotation(Url.class);
                            String urlValue = urlAnnotation.value();
                            this.listUrls.add(method);
                        }
                    }
                    this.listControllers.add(className);
                }
            }
            System.out.println("Controllers trouvés : " + this.listControllers.size());
            System.out.println("URLs trouvées : " + this.listUrls.size());
        } catch (Exception e) {
            throw new ServletException("Erreur lors de l'initialisation du FrontController", e);
        }
    }  
   
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/plain");
    java.io.PrintWriter out = response.getWriter();

    // 1. Extraction de l'URL
    String contextPath = request.getContextPath();
    String requestURI = request.getRequestURI();
    String urlDemandee = requestURI.substring(contextPath.length());

    out.println("URL demandée : " + urlDemandee);
    out.println("---------------------------------------");

    Method methodeTrouvee = null;

    // 2. Recherche de la méthode correspondante dans la liste
    for (Method method : this.listUrls) {
        Url urlAnnotation = method.getAnnotation(Url.class);
        if (urlAnnotation != null && urlAnnotation.value().equals(urlDemandee)) {
            methodeTrouvee = method;
            break;
        }
    }

    // 3. Affichage du résultat selon que la méthode existe ou non
    if (methodeTrouvee != null) {
        // La méthode a été trouvée
        String controleurAssocie = methodeTrouvee.getDeclaringClass().getName();
        
        out.println("[RÉSULTAT] : Méthode trouvée !");
        out.println("Contrôleur : " + controleurAssocie);
        out.println("Méthode    : " + methodeTrouvee.getName());
    } else {
        out.println("[RÉSULTAT] : ");
        out.println("L'URL \"" + urlDemandee + "\" n'a pas de méthode associée.");
        out.println("URLs disponibles :");
        for (Method method : this.listUrls) {
            Url urlAnnotation = method.getAnnotation(Url.class);
            if (urlAnnotation != null) {
                String urlValue = urlAnnotation.value();
                String controleurAssocie = method.getDeclaringClass().getName();
                out.println("- " + urlValue + " (Contrôleur : " + controleurAssocie + ", Méthode : " + method.getName() + ")");
            }
        }
    }
}

    @Override
    protected void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException , IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost (HttpServletRequest request, HttpServletResponse response) throws ServletException , IOException {
        processRequest(request, response);
    }    
}
