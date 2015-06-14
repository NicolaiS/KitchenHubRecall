package kr.kaist.resl.kitchenhubrecall.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import constants.database.KHSchema;
import constants.database.KHSchemaAttribute;
import constants.database.KHSchemaProduct;
import constants.database.KHSchemaProductInfoMeta;
import constants.database.KHSchemaRecall;
import kr.kaist.resl.kitchenhubrecall.Content_URIs;
import kr.kaist.resl.kitchenhubrecall.models.RecallNotification;
import kr.kaist.resl.kitchenhubrecall.models.Tuple;
import models.Product;

/**
 * Created by nicolais on 5/1/15.
 * <p/>
 * Util to connect to Shared Storage module
 */
public class DBUtil {

    /**
     * Get all products marked "present"
     *
     * @param context context
     * @return list of present products
     */
    public static List<Product> getPresentProducts(Context context) {
        List<Product> results = new ArrayList<Product>();

        String selection = KHSchemaProduct.CN_PRESENT + " = ?";
        String[] selectionArgs = new String[]{Integer.toString(1)};

        ContentResolver resolver = context.getContentResolver();
        Cursor c = resolver.query(Content_URIs.CONTENT_URI_PRODUCT, KHSchemaProduct.PROJECTION_ALL, selection, selectionArgs, null);
        if (c.moveToFirst()) {
            do {
                Integer intPresent = c.getInt(7);
                Boolean present = intPresent > 0;
                Product i = new Product(c.getInt(0), c.getInt(1), c.getInt(2), c.getString(3), c.getString(4), c.getString(5), c.getInt(6), present, c.getLong(8));
                results.add(i);
            } while (c.moveToNext());
        }

        c.close();

        return results;
    }

    /**
     * Generate batch URN from Product
     * Batch number is gathered from attributes
     *
     * @param context context
     * @param p       product
     * @return batch number
     */
    public static String getBatchNo(Context context, Product p) {
        ContentResolver resolver = context.getContentResolver();
        String urn = UrnUtil.getUniqueUrn(p);

        String selection = KHSchemaProductInfoMeta.CN_URN + " = ?";
        String[] selectionArgs = new String[]{urn};
        Cursor c = resolver.query(Content_URIs.CONTENT_URI_PRODUCT_INFO_META, new String[]{KHSchema.CN_ID}, selection, selectionArgs, null);
        if (!c.moveToFirst()) {
            c.close();
            return null;
        }

        Integer pimId = c.getInt(0);
        c.close();

        selection = KHSchemaAttribute.CN_PIM_ID + " = ? AND " + KHSchemaAttribute.CN_ATTR_KEY + " = ?";
        selectionArgs = new String[]{Integer.toString(pimId), "attr_batch_no"};
        c = resolver.query(Content_URIs.CONTENT_URI_ATTRIBUTE, new String[]{KHSchemaAttribute.CN_ATTR_VALUE}, selection, selectionArgs, null);
        if (!c.moveToFirst()) {
            c.close();
            return null;
        }

        String result = Integer.toString(c.getInt(0));
        c.close();

        return result;
    }

    /**
     * Get previous versions of recalls
     *
     * @param context context
     * @param params  recalls/version number tuples. Version numbers are loaded to this object
     */
    public static void loadPreviousVersion(Context context, List<Tuple> params) {
        ContentResolver resolver = context.getContentResolver();

        Map<String, Integer> versionMap = new HashMap<String, Integer>();

        Cursor c = resolver.query(Content_URIs.CONTENT_URI_RECALL, new String[]{KHSchemaRecall.CN_URN, KHSchemaRecall.CN_VERSION}, null, null, null);
        if (c.moveToFirst()) {
            do {
                versionMap.put(c.getString(0), c.getInt(1));
            } while (c.moveToNext());
        }
        c.close();

        for (Tuple t : params) {
            if (versionMap.containsKey(t.getURN())) t.setVersion(versionMap.get(t.getURN()));
        }
    }

    /**
     * Insert new recall notification. Deletes old with name Recall Serial
     *
     * @param context context
     * @param rn      recall notification
     * @return Private key of inserted recall notification.
     */
    public static Integer insertRecallNotification(Context context, RecallNotification rn) {
        Log.d(DBUtil.class.getName(), "-- Inserting recall notification");
        ContentResolver resolver = context.getContentResolver();
        resolver.delete(Content_URIs.CONTENT_URI_RECALL, KHSchemaRecall.CN_RECALL_SERIAL + " = ?", new String[]{rn.getRecallSerial()});

        Log.d(DBUtil.class.getName(), "--- " + rn.getUrn());
        Log.d(DBUtil.class.getName(), "--- " + rn.getRecallSerial());
        Log.d(DBUtil.class.getName(), "--- " + rn.getIssueDate().toString());
        Log.d(DBUtil.class.getName(), "--- " + rn.getDescription());
        Log.d(DBUtil.class.getName(), "--- " + rn.getDanger());
        Log.d(DBUtil.class.getName(), "--- " + rn.getInstructions());
        Log.d(DBUtil.class.getName(), "--- " + rn.getVersion());

        ContentValues values = new ContentValues();
        values.put(KHSchemaRecall.CN_URN, rn.getUrn());
        values.put(KHSchemaRecall.CN_RECALL_SERIAL, rn.getRecallSerial());
        values.put(KHSchemaRecall.CN_ISSUE_DATE, rn.getIssueDate().getTime());
        values.put(KHSchemaRecall.CN_DESCRIPTION, rn.getDescription());
        values.put(KHSchemaRecall.CN_DANGER, rn.getDanger());
        values.put(KHSchemaRecall.CN_INSTRUCTIONS, rn.getInstructions());
        values.put(KHSchemaRecall.CN_ACCEPTED, false);
        values.put(KHSchemaRecall.CN_VERSION, rn.getVersion());
        Uri uri = resolver.insert(Content_URIs.CONTENT_URI_RECALL, values);

        return Integer.valueOf(uri.getLastPathSegment());
    }
}