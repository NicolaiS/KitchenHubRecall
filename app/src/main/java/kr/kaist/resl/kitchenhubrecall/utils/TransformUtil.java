package kr.kaist.resl.kitchenhubrecall.utils;

import android.util.Log;

import models.Product;

/**
 * Created by nicolais on 5/1/15.
 * <p/>
 * Transformation utli
 */
public class TransformUtil {

    /**
     * Generate FQDN from Product
     *
     * @param product product
     * @return FQDN of product
     * @throws Exception
     */
    public static String getFQDN(Product product) throws Exception {
        String result = "";

        String gtin = product.getIndicator() + product.getCompanyPrefix() + product.getItemRefNo();

        result += gtin.charAt(0);
        for (int i = gtin.length() - 1; i > 0; i--) {
            result += "." + gtin.charAt(i);
        }

        String end = ".gtin.gs1.id.onsepc.com";
        result += end;

        Log.d(TransformUtil.class.getName(), "- Generating FQDN");
        Log.d(TransformUtil.class.getName(), "-- GTIN: " + gtin);
        Log.d(TransformUtil.class.getName(), "-- FQDN: " + result);

        return result;
    }

}
