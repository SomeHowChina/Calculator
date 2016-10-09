package bean;

/**
 * Created by songhao on 16/10/9.
 */
public class Tuple <T,K>{
    private T t;
    private K k;

    public Tuple() {
    }

    public Tuple(T t, K k) {
        this.t = t;
        this.k = k;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }

    public K getK() {
        return k;
    }

    public void setK(K k) {
        this.k = k;
    }

}
