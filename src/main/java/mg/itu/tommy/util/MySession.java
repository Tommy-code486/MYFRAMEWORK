package mg.itu.tommy.util;

import java.util.HashMap;
import java.util.Map;

public class MySession {
    private Map<String, Object> data = new HashMap<>();

    public Map<String, Object> getData() { return data; }
    public Object get(String cle) { return data.get(cle); }
    public void set(String cle, Object valeur) { data.put(cle, valeur); }
    public void remove(String cle) { data.remove(cle); }
}
