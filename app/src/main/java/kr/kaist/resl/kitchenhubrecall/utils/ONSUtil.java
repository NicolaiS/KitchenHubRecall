package kr.kaist.resl.kitchenhubrecall.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import org.apache.http.conn.util.InetAddressUtils;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.NAPTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import kr.kaist.resl.kitchenhubrecall.Content_URIs;
import kr.kaist.resl.kitchenhubrecall.enums.EnumOnsService;
import models.Product;

/**
 * Created by nicolais on 5/1/15.
 * <p/>
 * ONS related operations
 */
public class ONSUtil {

    /**
     * Get all Recall services of products
     *
     * @param context  context
     * @param products product
     * @return Map of products and their Recall services
     */
    public static Map<Product, List<NAPTRRecord>> getProductRecordMap(Context context, List<Product> products) {
        Map<Product, String> fqdnMap = new HashMap<Product, String>();
        Set<String> fqdnSet = new HashSet<String>();
        for (Product p : products) {
            try {
                String fqdn = TransformUtil.getFQDN(p);
                fqdnMap.put(p, fqdn);
                fqdnSet.add(fqdn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Map<String, List<NAPTRRecord>> recordMap = new HashMap<String, List<NAPTRRecord>>();
        for (String fqdn : fqdnSet) {
            try {
                List<NAPTRRecord> records = ONSUtil.requestRecords(context, fqdn, EnumOnsService.RECALL);
                recordMap.put(fqdn, records);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Map<Product, List<NAPTRRecord>> productRecordMap = new HashMap<Product, List<NAPTRRecord>>();
        for (Product p : products) {
            List<NAPTRRecord> records = recordMap.get(fqdnMap.get(p));
            productRecordMap.put(p, records);
        }
        return productRecordMap;
    }

    /**
     * Connect to ONS server and request records
     *
     * @param context context
     * @param fqdn    FQDN
     * @param service Service identifier
     * @return List of services
     * @throws Exception
     */
    public static List<NAPTRRecord> requestRecords(Context context, String fqdn, EnumOnsService service) throws Exception {
        List<NAPTRRecord> results = new ArrayList<NAPTRRecord>();

        Log.d(ONSUtil.class.getName(), "- Lookup: " + fqdn + " service " + service.getService());

        Lookup l = new Lookup(fqdn, Type.NAPTR);
        l.setResolver(new SimpleResolver(getONSaddr(context)));
        l.setCache(null);
        Record[] records = l.run();

        if (records != null) {
            Log.d(ONSUtil.class.getName(), "-- " + records.length + " records received");
            for (Record record : records) {
                NAPTRRecord naptrRecord = (NAPTRRecord) record;

                if (!service.getService().equals(naptrRecord.getService())) {
                    Log.d(ONSUtil.class.getName(), "--- Deny: wrong service " + naptrRecord.getService());
                    continue;
                }

                Log.d(ONSUtil.class.getName(), "--- Accept: " + naptrRecord.getOrder() + " " + naptrRecord.getPreference() + " " + naptrRecord.getFlags() + " " + naptrRecord.getService() + " " + naptrRecord.getRegexp());
                results.add(naptrRecord);
            }
        } else {
            Log.d(ONSUtil.class.getName(), "-- No records received");
        }

        Log.d(ONSUtil.class.getName(), "- Sorting results");
        Collections.sort(results, new SortRecords());
        Log.d(ONSUtil.class.getName(), "- Result:");
        for (NAPTRRecord r : results) {
            Log.d(ONSUtil.class.getName(), "-- " + r.getOrder() + " " + r.getPreference() + " " + r.getFlags() + " " + r.getService() + " " + r.getRegexp());
        }

        Log.d(ONSUtil.class.getName(), "- Returning records");

        return results;
    }

    /**
     * Sort records by priority
     * <p/>
     * 1. Order
     * 2. Preference
     * 3. Random
     */
    public static class SortRecords implements Comparator<NAPTRRecord> {
        @Override
        public int compare(NAPTRRecord r1, NAPTRRecord r2) {
            int result = Double.compare(r1.getOrder(), r2.getOrder());
            if (result != 0) return result;

            result = Double.compare(r1.getPreference(), (r2.getPreference()));
            if (result != 0) return result;

            return (new Random().nextInt(3) - 1);
        }
    }

    /**
     * Get address of ONS server
     *
     * @param context context
     * @return Address of ONS server. null if none is found.
     * @throws Exception
     */
    private static String getONSaddr(Context context) throws Exception {
        ContentResolver resolver = context.getContentResolver();
        Cursor c = resolver.query(Content_URIs.CONTENT_URI_ONS_ADDR, null, null, null, null);

        String onsAddr = null;
        if (c.moveToFirst()) {
            String str = c.getString(0);
            if (str != null && InetAddressUtils.isIPv4Address(str)) onsAddr = str;
        }
        c.close();
        if (onsAddr == null) throw new Exception("Invalid ONS address: " + onsAddr);

        return onsAddr;
    }
}
