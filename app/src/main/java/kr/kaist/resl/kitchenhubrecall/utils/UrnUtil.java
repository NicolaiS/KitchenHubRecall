package kr.kaist.resl.kitchenhubrecall.utils;

import android.content.Context;

import models.Product;

/**
 * Created by nicolais on 5/24/15.
 * <p/>
 * Util for URN generation
 */
public class UrnUtil {

    private static final String preSGTIN = "urn:epc:class:sgtin:";
    private static final String preLGTIN = "urn:epc:class:lgtin:";

    /**
     * Generate company URN from Product
     *
     * @param p product
     * @return Company URN
     */
    public static String getCompanyUrn(Product p) {
        return preSGTIN + p.getCompanyPrefix() + ".*.*";
    }

    /**
     * Generate item URN from Product
     *
     * @param p product
     * @return Item URN
     */
    public static String getItemUrn(Product p) {
        return preSGTIN + p.getCompanyPrefix() + "." + p.getItemRefNo() + ".*";
    }

    /**
     * Generate unique URN from Product
     *
     * @param p product
     * @return Unique URN
     */
    public static String getUniqueUrn(Product p) {
        return preSGTIN + p.getCompanyPrefix() + "." + p.getItemRefNo() + "." + p.getSerial();
    }

    /**
     * Generate batch URN from Product
     * Batch number is gathered from attributes
     *
     * @param context context
     * @param p       product
     * @return batch URN
     */
    public static String getBatchUrn(Context context, Product p) {
        String batchNumber = DBUtil.getBatchNo(context, p);
        return preLGTIN + p.getCompanyPrefix() + "." + p.getItemRefNo() + "." + batchNumber;
    }

}
