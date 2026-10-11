// FrontControllerServlet.java - version corrigée
package mg.itu.tommy.util;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.tommy.annotation.RestAPI;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@WebServlet(name = "FrontControllerServlet", urlPatterns = {"/"})
public class FrontControllerServlet extends HttpServlet {

    private String prefixe;
    private String suffixe;
    private List<String> controllerClassNames;
    private Map<UrlType, Mapping> mappingUrls = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public void init() throws ServletException {
        try {
            prefixe = getInitParameter("prefixe");
            suffixe = getInitParameter("suffixe");
            ServletContext context = getServletContext();
            mappingUrls = (Map<UrlType, Mapping>) context.getAttribute("mappingUrls");
            controllerClassNames = (List<String>) context.getAttribute("controllerClassNames");
        } catch (Exception e) {
            throw new ServletException("Erreur initialisation", e);
        }
    }

    private Object convertir(String valeur, Class<?> type) {
        if (type == String.class) return valeur;
        if (type == int.class || type == Integer.class) return Integer.parseInt(valeur);
        if (type == long.class || type == Long.class) return Long.parseLong(valeur);
        if (type == double.class || type == Double.class) return Double.parseDouble(valeur);
        if (type == float.class || type == Float.class) return Float.parseFloat(valeur);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(valeur);
        return null;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain");
        String uri = request.getRequestURI();
        String path = uri.substring(request.getContextPath().length());
        path = path.substring(1);
        PrintWriter out = response.getWriter();
        try {
            System.out.println("URL    : " + path);
            Mapping mapping = mappingUrls.get(new UrlType("/" + path, request.getMethod()));

            if (mapping != null) {
                Object controllerInstance = mapping.getControllerClass().getDeclaredConstructor().newInstance();
                Class<?>[] typeParametres = mapping.getMethod().getParameterTypes();
                Object[] argument = new Object[typeParametres.length];
                java.lang.reflect.Parameter[] parametres = mapping.getMethod().getParameters();

                for (int i = 0; i < parametres.length; i++) {
                    String nomArgument = parametres[i].getName();
                    Class<?> type = typeParametres[i];

                    boolean typeSimple = type.isPrimitive() || type == String.class|| Number.class.isAssignableFrom(type) || type == Boolean.class;
                    
                    if (typeSimple) {
                        for (String nomRequest : request.getParameterMap().keySet()) {
                            if (nomArgument.equals(nomRequest)) {
                                argument[i] = convertir(request.getParameter(nomRequest), type);
                                break;
                            } 
                        }
                    } else {
                        Object obj = type.getDeclaredConstructor().newInstance();
                        for (Field champ : type.getDeclaredFields()) {
                            String valeur = request.getParameter(champ.getName());
                            if (valeur != null) {
                                String nomChamp = champ.getName();
                                String nomSetter = "set" + Character.toUpperCase(nomChamp.charAt(0)) + nomChamp.substring(1);
                                Method setter = type.getMethod(nomSetter, champ.getType());
                                setter.invoke(obj, convertir(valeur, champ.getType()));                           
                            }
                        }
                        argument[i] = obj;
                    }
                    
                }

                // Verification API
                Object retour = mapping.getMethod().invoke(controllerInstance, argument);
                boolean isRestAPI = mapping.getMethod().isAnnotationPresent(RestAPI.class);

                if (isRestAPI) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");

                    if (retour instanceof String) {
                        out.println(retour);
                    } else {
                        ObjectMapper mapper = new ObjectMapper();
                        String json = mapper.writeValueAsString(retour);
                        out.println(json);
                    }
                    return;
                }

                if (retour instanceof ModelAndVue) {
                    ModelAndVue modelAndVue = (ModelAndVue) retour;

                    for (Map.Entry<String, Object> entry : modelAndVue.getData().entrySet()) {
                        request.setAttribute(entry.getKey(), entry.getValue());
                    }

                    String chemin = prefixe + modelAndVue.getVue() + suffixe;
                    request.getRequestDispatcher(chemin).forward(request, response);
                    return;

                } else {
                    out.println("Retour de la méthode : " + retour);
                }

                out.println("URL:" + path + "  Class :" + mapping.getControllerClass().getSimpleName()
                        + "  -> " + mapping.getMethod().getName());

            } else {
                out.println("Aucune correspondance trouvée pour l'URL : " + path);
                out.println("Les methodes disponibles sont :");
                for (Map.Entry<UrlType, Mapping> entry : mappingUrls.entrySet()) {
                    UrlType urlType = entry.getKey();
                    Mapping m = entry.getValue();
                    out.println("URL: " + urlType.getUrl() + "  Class: "
                            + m.getControllerClass().getSimpleName() + "  -> "
                            + m.getMethod().getName() + "  Type: " + urlType.getVerb());
                }
            }
        } catch (Exception e) {
            out.println("Erreur lors du traitement de la requête : " + e.getMessage());
        }

        out.println("---Sprint1---");
        for (String className : controllerClassNames) {
            out.println("Classe trouvée : " + className);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processRequest(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processRequest(req, res);
    }
}