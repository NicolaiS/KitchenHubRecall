package kr.kaist.resl.kitchenhubrecall;

import android.net.Uri;

import constants.ProviderConstants;
import constants.database.KHSchemaAttribute;
import constants.database.KHSchemaProduct;
import constants.database.KHSchemaProductInfoMeta;
import constants.database.KHSchemaRecall;

/**
 * Created by nicolais on 4/23/15.
 * <p/>
 * URIs to access Shared Storage module
 */
public class Content_URIs {

    private static final Uri CONTENT_URI = Uri.parse("content://" + ProviderConstants.DB_AUTHORITY);

    public static final Uri CONTENT_URI_PRODUCT = Uri.withAppendedPath(CONTENT_URI, KHSchemaProduct.TABLE_NAME);
    public static final Uri CONTENT_URI_PRODUCT_INFO_META = Uri.withAppendedPath(CONTENT_URI, KHSchemaProductInfoMeta.TABLE_NAME);
    public static final Uri CONTENT_URI_ATTRIBUTE = Uri.withAppendedPath(CONTENT_URI, KHSchemaAttribute.TABLE_NAME);
    public static final Uri CONTENT_URI_RECALL = Uri.withAppendedPath(CONTENT_URI, KHSchemaRecall.TABLE_NAME);

    public static final Uri CONTENT_URI_ONS_ADDR = Uri.withAppendedPath(CONTENT_URI, ProviderConstants.ONS_ADDR);

}
