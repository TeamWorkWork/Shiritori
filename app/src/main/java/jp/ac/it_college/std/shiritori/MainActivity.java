package jp.ac.it_college.std.shiritori;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;

public class MainActivity extends Activity implements OnReceiveListener{

    private IntentFilter intentFilter;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container_bottom, new StartFragment())
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    /*
    Implemented OnReceiveListener
     */

    /* WiFi Directの有効/無効状態が通知される。機能制限やユーザへの通知に利用 */
    @Override
    public void onStateChanged(Intent intent) {

    }

    /* デバイス情報の変更通知（通信可能なデバイスの発見・ロストなど） */
    @Override
    public void onPeersChanged(Intent intent) {

    }

    /* IPアドレスなどコネクション情報。通信状態の変更通知 */
    @Override
    public void onConnectionChanged(Intent intent) {

    }

    /* 自分自身のデバイス状態の変更通知(相手デバイスではないことに注意) */
    @Override
    public void onDeviceChanged(Intent intent) {

    }
}
