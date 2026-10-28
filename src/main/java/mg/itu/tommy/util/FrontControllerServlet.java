package mg.itu.tommy.util;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import mg.itu.tommy.annotation.RestAPI;
import jakarta.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import mg.itu.tommy.annotation.Auth;

@MultipartConfig
@WebServlet(name = "FrontControllerServlet", urlPatterns = {"/"})
public class FrontControllerServlet extends HttpServlet {

    private String prefixe;
    private String suffixe;
    private List<String> controllerClassNames;
    private Map<UrlType, Mapping> mappingUrls = new HashMap<>();
    private String dossierUpload;
    private String authKey;
    private String roleKey;

    public static final String SESSION_MAP_KEY = "instanceSession";


    // ===================== INITIALISATION =====================

    @Override
    @SuppressWarnings("unchecked")
    
    public void init() throws ServletException {
        try {
            prefixe = getInitParameter("prefixe");
            suffixe = getInitParameter("suffixe");
            ServletContext context = getServletContext();

            authKey = context.getInitParameter("authentification");
            roleKey = context.getInitParameter("role");

            mappingUrls = (Map<UrlType, Mapping>) context.getAttribute("mappingUrls");
            controllerClassNames = (List<String>) context.getAttribute("controllerClassNames");

            if (context.getAttribute(SESSION_MAP_KEY) == null) {
               context.setAttribute(SESSION_MAP_KEY, new java.util.concurrent.ConcurrentHashMap<HttpSession, Session>());
            }

            chargerDossierUpload();
        } catch (Exception e) {
            throw new ServletException("Erreur initialisation", e);
        }
    }

    private int verificationAccess(Mapping mapping, HttpServletRequest request) {
        Auth auth = mapping.getMethod().getAnnotation(Auth.class);
        if (auth == null) {
            auth = mapping.getControllerClass().getAnnotation(Auth.class);
        }
        if (auth == null) return 0;

        HttpSession httpSession = request.getSession(false);
        if(httpSession == null || authKey == null || httpSession.getAttribute(authKey) == null) {
            return HttpServletResponse.SC_UNAUTHORIZED;
        }

        String[] roleAutorises = auth.value();
        if(roleAutorises.length == 0) {
            return 0;
        }
        Object roletUtilisateur = (roleKey == null) ? null : httpSession.getAttribute(roleKey);
        if(roletUtilisateur != null) {
            for(String role : roleAutorises) {
                if(role.equals(roletUtilisateur.toString())) {
                    return 0;
                }
            }
        }
        return HttpServletResponse.SC_FORBIDDEN;
    }

    private void chargerDossierUpload() throws IOException {
        Properties props = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("upload.properties")) {
            if (in != null) {
                props.load(in);
                dossierUpload = props.getProperty("upload.dir");
            }
        }
    }

    // ===================== CONVERSION =====================

    private Object convertir(String valeur, Class<?> type) {
        if (type == String.class) return valeur;
        if (type == int.class || type == Integer.class) return Integer.parseInt(valeur);
        if (type == long.class || type == Long.class) return Long.parseLong(valeur);
        if (type == double.class || type == Double.class) return Double.parseDouble(valeur);
        if (type == float.class || type == Float.class) return Float.parseFloat(valeur);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(valeur);
        return null;
    }

    private boolean estTypeSimple(Class<?> type) {
        return type.isPrimitive() || type == String.class
                || Number.class.isAssignableFrom(type) || type == Boolean.class;
    }

    // ===================== CONSTRUCTION DES ARGUMENTS =====================

    private Object[] construireArguments(Mapping mapping, HttpServletRequest request) throws Exception {
        Class<?>[] typeParametres = mapping.getMethod().getParameterTypes();
        java.lang.reflect.Parameter[] parametres = mapping.getMethod().getParameters();
        Object[] argument = new Object[typeParametres.length];

        for (int i = 0; i < parametres.length; i++) {
            String nomArgument = parametres[i].getName();
            Class<?> type = typeParametres[i];

            if (type == Session.class) {
                argument[i] = recupererSession(request);
                continue;
            }

            if (estTypeSimple(type)) {
                argument[i] = construireArgumentSimple(nomArgument, type, request);
            } else {
                argument[i] = construireObjet(type, request);
            }
        }
        return argument;
    }

    @SuppressWarnings("unchecked")
    private Session recupererSession(HttpServletRequest request) {
        ServletContext context = getServletContext();
        Map<HttpSession, Session> instanceSession =
                (Map<HttpSession, Session>) context.getAttribute(SESSION_MAP_KEY);

        HttpSession httpSession = request.getSession();
        Session sessionTrouver = null;

        for (Map.Entry<HttpSession, Session> entry : instanceSession.entrySet()) {
            if (entry.getKey() == httpSession) {
                sessionTrouver = entry.getValue();
                break;
            }
        }

        if (sessionTrouver == null) {
            sessionTrouver = new Session(httpSession);
            instanceSession.put(httpSession, sessionTrouver);
        }
        return sessionTrouver;
    }

    private Object construireArgumentSimple(String nomArgument, Class<?> type, HttpServletRequest request) {
        for (String nomRequest : request.getParameterMap().keySet()) {
            if (nomArgument.equals(nomRequest)) {
                return convertir(request.getParameter(nomRequest), type);
            }
        }
        return null;
    }

    private Object construireObjet(Class<?> type, HttpServletRequest request) throws Exception {
        Object obj = type.getDeclaredConstructor().newInstance();
        for (Field champ : type.getDeclaredFields()) {

            if (champ.getType() == Mapfile.class) {
                remplirMapfile(champ, type, obj, request);
                continue;
            }

            String valeur = request.getParameter(champ.getName());
            if (valeur != null) {
                String nomChamp = champ.getName();
                String nomSetter = "set" + Character.toUpperCase(nomChamp.charAt(0)) + nomChamp.substring(1);
                Method setter = type.getMethod(nomSetter, champ.getType());
                setter.invoke(obj, convertir(valeur, champ.getType()));
            }
        }
        return obj;
    }

    // ===================== UPLOAD DE FICHIER =====================

    private void remplirMapfile(Field champ, Class<?> type, Object obj, HttpServletRequest request) throws Exception {
        String contentType = request.getContentType();
        if (contentType != null && contentType.startsWith("multipart/")) {

            Part part = request.getPart(champ.getName());

            if (part != null && part.getSize() > 0) {
                String nomFichier = part.getSubmittedFileName();
                byte[] octets = part.getInputStream().readAllBytes();

                Mapfile mapfile = new Mapfile();
                mapfile.setNom(nomFichier);
                mapfile.setExtension(nomFichier.contains(".")
                        ? nomFichier.substring(nomFichier.lastIndexOf('.') + 1) : "");
                mapfile.setBytes(octets);

                sauvegarderFichier(nomFichier, octets);

                String nom = champ.getName();
                String nomSetter = "set" + Character.toUpperCase(nom.charAt(0)) + nom.substring(1);
                type.getMethod(nomSetter, Mapfile.class).invoke(obj, mapfile);
            }
        }
    }

    private void sauvegarderFichier(String nomFichier, byte[] octets) throws IOException {
        if (dossierUpload != null) {
            File dossier = new File(dossierUpload);
            dossier.mkdirs();
            File destination = new File(dossier, UUID.randomUUID().toString() + "_" + nomFichier);
            Files.write(destination.toPath(), octets);
        }
    }

    
    // ===================== REPONSES =====================
    private void envoyerReponseRestAPI(Object retour, HttpServletResponse response, PrintWriter out)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (retour instanceof String) {
            out.println(retour);
        } else {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(retour);
            out.println(json);
        }
    }

    private void forwarderVersVue(ModelAndVue modelAndVue, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        for (Map.Entry<String, Object> entry : modelAndVue.getData().entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }

        String chemin = prefixe + modelAndVue.getVue() + suffixe;
        request.getRequestDispatcher(chemin).forward(request, response);
    }

    private void afficherMappingsDisponibles(String path, PrintWriter out) {
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

    // ===================== TRAITEMENT PRINCIPAL =====================

    private String extraireChemin(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String path = uri.substring(request.getContextPath().length());
        return path.substring(1);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain");
        String path = extraireChemin(request);
        PrintWriter out = response.getWriter();
        try {
            System.out.println("URL    : " + path);
            Mapping mapping = mappingUrls.get(new UrlType("/" + path, request.getMethod()));

            if (mapping != null) {

                int access = verificationAccess(mapping, request);
                if (access == HttpServletResponse.SC_UNAUTHORIZED) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("text/html");
                    out.println("Vous devez vous connecter pour accéder à cette ressource.");
                    out.println("<a href=\"" + request.getContextPath() + "/login\">Se connecter</a>");
                    return;
                }
                if (access == HttpServletResponse.SC_FORBIDDEN) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("text/html");
                    out.println("Accès interdit");
                    return;
                }
                Object controllerInstance = mapping.getControllerClass().getDeclaredConstructor().newInstance();
                Object[] argument = construireArguments(mapping, request);

                // Verification API
                Object retour = mapping.getMethod().invoke(controllerInstance, argument);
                boolean isRestAPI = mapping.getMethod().isAnnotationPresent(RestAPI.class);

                if (isRestAPI) {
                    envoyerReponseRestAPI(retour, response, out);
                    return;
                }

                if (retour instanceof ModelAndVue) {
                    forwarderVersVue((ModelAndVue) retour, request, response);
                    return;
                } else {
                    out.println("Retour de la méthode : " + retour);
                }

                out.println("URL:" + path + "  Class :" + mapping.getControllerClass().getSimpleName()
                        + "  -> " + mapping.getMethod().getName());

            } else {
                afficherMappingsDisponibles(path, out);
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