package jp.ac.it_college.std.shiritori;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.provider.Settings;

public class MainActivity extends Activity
        implements OnReceiveListener, DeviceActionListener{

    private IntentFilter intentFilter;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver;
    private boolean isWifiP2pEnabled = false;

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

    private void resetData() {
        OpponentListFragment fragmentList = (OpponentListFragment) getFragmentManager()
                .findFragmentById(R.id.container_root);
        OpponentDetailFragment fragmentDetails = (OpponentDetailFragment) getFragmentManager()
                .findFragmentById(R.id.container_detail);

        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
        if (fragmentDetails != null) {
            fragmentDetails.resetViews();
        }
    }

    /*
    Implemented OnReceiveListener
     */

    /* WiFi Directの有効/無効状態が通知される。機能制限やユーザへの通知に利用 */
    @Override
    public void onStateChanged(Intent intent) {
        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
            this.isWifiP2pEnabled = true;
        } else {
            this.isWifiP2pEnabled = false;
        }
    }

    /* デバイス情報の変更通知（通信可能なデバイスの発見・ロストなど） */
    @Override
    public void onPeersChanged(Intent intent) {
        if (manager != null) {
            PeerListListener listener = (PeerListListener) getFragmentManager()
                    .findFragmentById(R.id.container_root);
            manager.requestPeers(channel, listener);
        }
    }

    /* IPアドレスなどコネクション情報。通信状態の変更通知 */
    @Override
    public void onConnectionChanged(Intent intent) {
        if (manager == null) {
            return;
        }

        NetworkInfo networkInfo = (NetworkInfo) intent
                .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

        if (networkInfo.isConnected()) {
            OpponentDetailFragment fragment = (OpponentDetailFragment) getFragmentManager()
                    .findFragmentById(R.id.container_detail);
            manager.requestConnectionInfo(channel, fragment);
        } else {
            resetData();
        }
    }

    /* 自分自身のデバイス状態の変更通知(相手デバイスではないことに注意) */
    @Override
    public void onDeviceChanged(Intent intent) {

    }


    /*
    Implemented DeviceActionListener
     */

    //TODO 接続を途中で中段した場合の切断処理を実装
    @Override
    public void cancelDisconnect() {

    }

    @Override
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }

    @Override
    public void disconnect() {
        OpponentDetailFragment fragmentDetails = (OpponentDetailFragment) getFragmentManager()
                .findFragmentById(R.id.container_detail);
        fragmentDetails.resetViews();

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
    public void searchPeer() {
        if (!isWifiP2pEnabled) {
            //検索ボタン押下時、WiFiがOffの場合アラートダイアログを表示する
            new AlertDialog.Builder(this)
                    .setTitle(R.string.alert_wifi_title)
                    .setMessage(R.string.alert_wifi_message)
                    .setPositiveButton(R.string.alert_btn_wifi_setting, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    })
                    .setNegativeButton(R.string.alert_btn_cancel, null)
                    .show();

            return;
        }

        OpponentListFragment fragment = (OpponentListFragment)getFragmentManager()
                .findFragmentById(R.id.container_root);
        fragment.onInitiateDiscovery();

        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }
}
