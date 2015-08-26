package jp.ac.it_college.std.shiritori;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;

public class MainActivity extends Activity
        implements OnReceiveListener, DeviceActionListener {

    private IntentFilter intentFilter;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiP2pDevice device;
    private BroadcastReceiver receiver;
    private boolean isWifiP2pEnabled = false;
    private EventManager eventManager;

    public static final String WIFI_INFO = "WIFI_INFO";
    public static final String WIFI_GROUP = "WIFI_GROUP";
    public static final int SERVER_PORT = 4545;
    public static final int MESSAGE_READ = 0x400 + 1;
    public static final int MY_HANDLE = 0x400 + 2;
    public static final String GAME_READY = "GAME_READY_OK";
    public static final String GAME_START = "GAME_START";
    public static final String GAME_RESULT = "GAME_RESULT";
    public static final String GAME_OVER = "GAME_OVER";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container_root, new TitleFragment())
                    .commit();
        }

        //ブロードキャストされた情報を受け取る
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        eventManager = new EventManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(getEventManager());
        registerReceiver(receiver, intentFilter);
        //リスナー登録
        eventManager.addOnReceiveListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        disconnect();
        unregisterReceiver(receiver);
        //リスナー削除
        eventManager.removeOnReceiveListener(this);
    }

    public WifiP2pDevice getDevice() {
        return device;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public WifiP2pManager getManager() {
        return manager;
    }

    public WifiP2pManager.Channel getChannel() {
        return channel;
    }

    public boolean isWifiP2pEnabled() {
        return isWifiP2pEnabled;
    }

    /*
    Implemented OnReceiveListener
     */
    @Override
    public void onStateChanged(Intent intent) {
        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
            this.isWifiP2pEnabled = true;
        } else {
            this.isWifiP2pEnabled = false;
        }
    }

    @Override
    public void onPeersChanged(Intent intent) {

    }

    @Override
    public void onConnectionChanged(Intent intent) {

    }

    @Override
    public void onDeviceChanged(Intent intent) {
        this.device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
    }

    /*
    Implemented DeviceActionListener
     */
    @Override
    public void cancelDisconnect() {

    }

    @Override
    public void connect(WifiP2pConfig config) {

    }

    @Override
    public void disconnect() {
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }

    @Override
    public void showDetails(WifiP2pDevice device) {

    }
}
