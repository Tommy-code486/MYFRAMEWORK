package util;

import java.io.IOException;
import java.lang.reflect.Method;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mg.itu.tommy.annotation.Controller;
import mg.itu.tommy.annotation.MethodHttp;
import mg.itu.tommy.annotation.Url;
import mg.itu.tommy.mapping.Mapping;
import mg.itu.tommy.mapping.MappingKey;


public class FrontController extends HttpServlet {

    public FrontController() {
        super();
    }

    private List<String> listControllers = new ArrayList<>();
    // Clé = MappingKey(url, typeHttp). Deux MappingKey sont égales si url et type sont identiques (cf. equals())
    private Map<MappingKey, Mapping> mapUrls = new HashMap<>();

    @Override
    public void init() throws ServletException {
        try {
            List<String> classesAll = Utilitaire.ScanneClass("controller");
            for (String className : classesAll) {
                Class<?> clazz = Class.forName(className);
                Method[] methods = clazz.getDeclaredMethods();
                if (clazz.isAnnotationPresent(Controller.class)) {
                    for (Method method : methods) {
                        if (method.isAnnotationPresent(Url.class)) {
                            Url urlAnnotation = method.getAnnotation(Url.class);
                            String urlValue = urlAnnotation.value();
                            MethodHttp methodHttp = urlAnnotation.method();

                            Mapping mapping = new Mapping(className, method.getName(), methodHttp);
                            MappingKey cle = new MappingKey(urlValue, methodHttp);
                            this.mapUrls.put(cle, mapping);
                        }
                    }
                    this.listControllers.add(className);
                }
            }
            System.out.println("Controllers trouvés : " + this.listControllers.size());
            System.out.println("URLs trouvées : " + this.mapUrls.size());
        } catch (Exception e) {
            throw new ServletException("Erreur lors de l'initialisation du FrontController", e);
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        java.io.PrintWriter out = response.getWriter();

        // 1. Extraction de l'URL et du type de requête (GET/POST)
        String contextPath = request.getContextPath();
        String requestURI = request.getRequestURI();
        String urlDemandee = requestURI.substring(contextPath.length());
        MethodHttp typeHttpDemande = MethodHttp.valueOf(request.getMethod().toUpperCase());

        out.println("URL demandée   : " + urlDemandee);
        out.println("Type demandé   : " + typeHttpDemande);
        out.println("---------------------------------------");

        // 2. Recherche dans la map grâce à equals()/hashCode() de MappingKey
        MappingKey cle = new MappingKey(urlDemandee, typeHttpDemande);
        Mapping mappingTrouve = this.mapUrls.get(cle);

        // 3. Affichage du résultat selon que la méthode existe ou non
        if (mappingTrouve != null) {
            out.println("[RÉSULTAT] : Méthode trouvée !");
            out.println("Contrôleur : " + mappingTrouve.getClassName());
            out.println("Méthode    : " + mappingTrouve.getMethodName());
            out.println("Type       : " + mappingTrouve.getMethodHttp());
        } else {
            out.println("[RÉSULTAT] : ");
            out.println("L'URL \"" + urlDemandee + "\" en " + typeHttpDemande + " n'a pas de méthode associée.");
            out.println("URLs disponibles :");
            for (Map.Entry<MappingKey, Mapping> entry : this.mapUrls.entrySet()) {
                MappingKey key = entry.getKey();
                Mapping mapping = entry.getValue();
                out.println("- " + key.getUrl() + " [" + key.getMethodHttp() + "] (Contrôleur : "
                        + mapping.getClassName() + ", Méthode : " + mapping.getMethodName() + ")");
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
}