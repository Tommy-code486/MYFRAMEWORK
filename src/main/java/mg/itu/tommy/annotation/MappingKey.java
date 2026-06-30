package mg.itu.tommy.mapping;

import java.util.Objects;

import mg.itu.tommy.annotation.MethodHttp;

public class MappingKey {

    private String url;
    private MethodHttp methodHttp;

    public MappingKey() {
    }

    public MappingKey(String url, MethodHttp methodHttp) {
        this.url = url;
        this.methodHttp = methodHttp;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public MethodHttp getMethodHttp() {
        return methodHttp;
    }

    public void setMethodHttp(MethodHttp methodHttp) {
        this.methodHttp = methodHttp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MappingKey that = (MappingKey) o;
        return Objects.equals(url, that.url) && methodHttp == that.methodHttp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, methodHttp);
    }

    @Override
    public String toString() {
        return "MappingKey{" +
                "url='" + url + '\'' +
                ", methodHttp=" + methodHttp +
                '}';
    }
}