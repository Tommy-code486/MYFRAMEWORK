package mg.itu.rivaldo.controller;

import java.util.Objects;

public class UrlType {
    private String url;
    private String verb;

    public UrlType(String url, String verb) {
        this.url = url;
        this.verb = verb;
    }

    public String getUrl() {
        return url;
    }

    public String getVerb() {
        return verb;
    }

    
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }
            
        if (obj == null || getClass() != obj.getClass()){
            return false;
        }
            

        UrlType urlType = (UrlType) obj;

        return Objects.equals(url, urlType.url) && Objects.equals(verb, urlType.verb);
    }

    public int hashCode() {
        return Objects.hash(url, verb);
    }
}