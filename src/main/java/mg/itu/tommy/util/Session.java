package mg.itu.tommy.util;

import jakarta.servlet.http.HttpSession;

import java.util.Enumeration;
import java.util.HashMap;

public class Session extends HashMap<String, Object> {

    private HttpSession httpSession;

    public Session(HttpSession httpSession) {
        this.httpSession = httpSession;

        Enumeration<String> noms = httpSession.getAttributeNames();
        while (noms.hasMoreElements()) {
            String nom = noms.nextElement();
            super.put(nom, httpSession.getAttribute(nom)); // super : httpSession est déjà affecté, mais on évite de réécrire dans la session
        }
    }

    @Override
    public Object put(String cle, Object valeur) {
        httpSession.setAttribute(cle, valeur);
        return super.put(cle, valeur);
    }

    @Override
    public Object remove(Object cle) {
        httpSession.removeAttribute((String) cle);
        return super.remove(cle);
    }
}