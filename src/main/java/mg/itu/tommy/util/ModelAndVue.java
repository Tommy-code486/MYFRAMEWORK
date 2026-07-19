
package mg.itu.tommy.util;
import java.util.HashMap;
import java.util.Map;

public class ModelAndVue {
    String vue;
    Map<String, Object> data;

    public ModelAndVue() {
        this.data = new HashMap<>();
    }

    public ModelAndVue(String vue, Map<String, Object> data) {
        this.vue = vue;
        this.data = data;
    }
    public String getVue() {
        return vue;
    }
    public void setVue(String vue) {
        this.vue = vue;
    }
    public Map<String, Object> getData() {
        return data;
    }
    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public void setAttribute(String key, Object value) {
        this.data.put(key, value);
    }

}
