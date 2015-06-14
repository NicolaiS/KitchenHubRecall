package kr.kaist.resl.kitchenhubrecall.models;

/**
 * Tuple of URN and version number
 */

public class Tuple {

    private String u = null;
    private Integer v = null;

    public Tuple(String u, Integer v) {
        super();
        this.u = u;
        this.v = v;
    }

    public String getURN() {
        return u;
    }

    public void setURN(String u) {
        this.u = u;
    }

    public Integer getVersion() {
        return v;
    }

    public void setVersion(Integer v) {
        this.v = v;
    }
}
