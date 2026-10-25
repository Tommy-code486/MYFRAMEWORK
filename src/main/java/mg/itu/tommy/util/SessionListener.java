package mg.itu.tommy.util;

import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import java.util.Map;

@WebListener
public class SessionListener implements HttpSessionListener {

    @Override
    @SuppressWarnings("unchecked")
    public void sessionDestroyed(HttpSessionEvent se) {
        ServletContext context = se.getSession().getServletContext();
        Map<HttpSession, Session> instanceSession =
                (Map<HttpSession, Session>) context.getAttribute(FrontControllerServlet.SESSION_MAP_KEY);
        if (instanceSession != null) {
            instanceSession.remove(se.getSession());
        }
    }
}