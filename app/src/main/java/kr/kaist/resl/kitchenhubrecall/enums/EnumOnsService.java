package kr.kaist.resl.kitchenhubrecall.enums;

/**
 * Created by nicolais on 5/23/15.
 * <p/>
 * Enumerator of service identifiers
 */
public enum EnumOnsService {

    PRODUCT_INFORMATION("http://www.gs1.org/ons/product_information"), RECALL("http://www.gs1.org/ons/recall");

    private String service;

    EnumOnsService(String service) {
        this.service = service;
    }

    public String getService() {
        return service;
    }
}