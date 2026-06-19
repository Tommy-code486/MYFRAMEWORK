package util;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import mg.itu.tommy.annotation.Controller;

public class FrontController extends HttpServlet {

    public FrontController() {
        super();
    }
    private List<String> listControllers = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        try {
            List<String> classesAll = Utilitaire.ScanneClass("controller");
            for (String className : classesAll) {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(Controller.class)) {
                    this.listControllers.add(className);
                }
            }
            System.out.println("Controllers trouvés : " + this.listControllers.size());
        } catch (Exception e) {
            throw new ServletException("Erreur lors de l'initialisation du FrontController", e);
        }
    }  
   
    protected void processRequest(HttpServletRequest request , HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        String url = request.getRequestURI().toString();

        response.getWriter().println("URL demandée : " + url);
        response.getWriter().println("Controllers disponibles : ");
        for (String controller : this.listControllers) {
            response.getWriter().println(controller);
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
