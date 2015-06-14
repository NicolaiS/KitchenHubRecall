package kr.kaist.resl.kitchenhubrecall.service;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.xbill.DNS.NAPTRRecord;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import constants.KHBroadcasts;
import constants.database.KHSchema;
import kr.kaist.resl.kitchenhubrecall.R;
import kr.kaist.resl.kitchenhubrecall.models.RecallNotification;
import kr.kaist.resl.kitchenhubrecall.models.Tuple;
import kr.kaist.resl.kitchenhubrecall.utils.DBUtil;
import kr.kaist.resl.kitchenhubrecall.utils.ONSUtil;
import kr.kaist.resl.kitchenhubrecall.utils.UrnUtil;
import models.Product;

/**
 * Created by nicolais on 4/30/15.
 * <p/>
 * Service to connect to recall services and register
 */
public class RecallService extends Service {

    private static int FOREGROUND_ID = 4203;

    private BroadcastReceiver broadcastReceiver = null;

    private RecallConnection thread = null;

    // Open connections
    private List<WebsocketClientEndpoint> connections = new ArrayList<WebsocketClientEndpoint>();

    private Gson gson = null;

    @Override
    public void onCreate() {
        super.onCreate();

        gson = new GsonBuilder().create();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                startRecallClient();
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(KHBroadcasts.PRODUCT_INFORMATION_UPDATED);
        registerReceiver(broadcastReceiver, filter);

        startRecallClient();
    }

    private void startRecallClient() {
        // Start thread is none is alive
        if (thread == null || !thread.isAlive()) {
            thread = new RecallConnection();
            thread.start();
        }
    }

    @Override
    public void onDestroy() {
        if (broadcastReceiver != null) unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Keep service alive
        startForeground(FOREGROUND_ID, buildForegroundNotification());
        return START_STICKY;
    }

    class RecallConnection extends Thread {
        @Override
        public void run() {
            Log.d(getClass().getName(), "- Reconnecting recall clients");

            Log.d(getClass().getName(), "- Closing old connections");
            try {
                closeConnections();
            } catch (InterruptedException e) {
                Log.d(getClass().getName(), "- Error closing connections: " + e.getMessage());
            }

            List<Product> products = DBUtil.getPresentProducts(RecallService.this);
            Log.d(getClass().getName(), "- Retrieved " + products.size() + " existing products.");
            Map<Product, List<NAPTRRecord>> productRecordMap = ONSUtil.getProductRecordMap(RecallService.this, products);

            // Batch up products if they have identical Recall services
            Map<String, Set<String>> urlMap = new HashMap<String, Set<String>>();
            for (Product p : products) {
                List<NAPTRRecord> records = productRecordMap.get(p);
                if (records == null || records.isEmpty()) continue;

                String url = records.get(0).getRegexp().split("!")[2];

                String itemNo = UrnUtil.getItemUrn(p);
                String batchNo = UrnUtil.getBatchUrn(RecallService.this, p);

                if (itemNo != null || batchNo != null) {
                    if (!urlMap.containsKey(url)) urlMap.put(url, new HashSet<String>());
                    Set<String> urnSet = urlMap.get(url);

                    if (itemNo != null) urnSet.add(itemNo);
                    if (batchNo != null) urnSet.add(batchNo);
                }
            }

            for (Map.Entry<String, Set<String>> e : urlMap.entrySet()) {
                connectAndRegister(e.getKey(), e.getValue());
            }

            Log.d(getClass().getName(), "- Connections established");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification buildForegroundNotification() {
        NotificationCompat.Builder b = new NotificationCompat.Builder(this);

        b.setOngoing(true);
        b.setSmallIcon(R.drawable.ic_adb_white_24dp)
                .setContentTitle("Recall")
                .setContentText("Running...");

        return (b.build());
    }

    /**
     * Close all connections to Recall services
     *
     * @throws InterruptedException
     */
    private void closeConnections() throws InterruptedException {
        for (WebsocketClientEndpoint wsc : connections) {
            wsc.closeSession();
        }
        connections.clear();
    }

    /**
     * Connect to Recall service
     *
     * @param url    URL of Recall service
     * @param urnSet URNs to be registered
     */
    private void connectAndRegister(String url, final Set<String> urnSet) {
        Log.d(getClass().getName(), "- Registering following URNs to: " + url);
        for (String urn : urnSet) {
            Log.d(getClass().getName(), "-- " + urn);
        }

        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint(uri);
        // Save recall notification if received and send out broadcast
        clientEndPoint.addMessageHandler(new WebsocketClientEndpoint.MessageHandler() {
            public void handleMessage(String msg) {
                Log.d(getClass().getName(), "- Received recall notification");

                RecallNotification rn = gson.fromJson(msg, RecallNotification.class);

                Integer id = DBUtil.insertRecallNotification(RecallService.this, rn);

                Log.d(getClass().getName(), "-- Sending broadcast: " + KHBroadcasts.RECALL_UPDATED + " with id " + id);
                Intent i = new Intent(KHBroadcasts.RECALL_UPDATED);
                i.putExtra(KHSchema.CN_ID, id);
                sendBroadcast(i);
            }
        });

        // Prepare URNs and version numbers
        List<Tuple> params = new ArrayList<Tuple>(urnSet.size());
        for (String urn : urnSet) {
            params.add(new Tuple(urn, -1));
        }
        DBUtil.loadPreviousVersion(RecallService.this, params);

        // Register products
        clientEndPoint.sendMessage(gson.toJson(params));

        connections.add(clientEndPoint);
    }
}
