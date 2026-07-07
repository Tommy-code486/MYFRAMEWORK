package mg.itu.tommy.mapping;
import mg.itu.tommy.annotation.MethodHttp;


public class Mapping {

    private String className;
    private String methodName;
    private MethodHttp methodHttp;

    public Mapping() {
    }

    public Mapping(String className, String methodName, MethodHttp methodHttp) {
        this.className = className;
        this.methodName = methodName;
        this.methodHttp = methodHttp;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public MethodHttp getMethodHttp() {
        return methodHttp;
    }

    public void setMethodHttp(MethodHttp methodHttp) {
        this.methodHttp = methodHttp;
    }

    @Override
    public String toString() {
        return "Mapping{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", methodHttp=" + methodHttp +
                '}';
    }
}