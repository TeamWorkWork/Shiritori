package jp.ac.it_college.std.shiritori;


import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class OpponentListFragment extends ListFragment
        implements View.OnClickListener, PeerListListener, OnReceiveListener {

    private List<WifiP2pDevice> peers = new ArrayList<>();
    private ProgressDialog progressDialog;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //EventManagerの設定
        ((MainActivity) getActivity()).getEventManager().addOnReceiveListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container_detail, new OpponentDetailFragment())
                    .commit();
        }

        //各ボタンのOnClickListenerを設定
        getView().findViewById(R.id.btn_wifi_setting).setOnClickListener(this);
        getView().findViewById(R.id.btn_discover).setOnClickListener(this);

        //アダプターの設定
        setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.row_devices, peers));

        //WiFiP2pManagerとChannelの設定
        this.manager = ((MainActivity) getActivity()).getManager();
        this.channel = ((MainActivity) getActivity()).getChannel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_opponent_list, container, false);
    }

    private static String getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";

        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        OpponentDetailFragment fragment =
                (OpponentDetailFragment) getFragmentManager().findFragmentById(R.id.container_detail);
        WifiP2pDevice device = (WifiP2pDevice) getListAdapter().getItem(position);

        fragment.showDetails(device);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_wifi_setting:
                //WiFi設定ボタンが押された時の処理
                onClickWifiSetting();
                break;
            case R.id.btn_discover:
                //検索ボタンが押された時の処理
                onDiscover();
        }
    }

    public void onInitiateDiscovery() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(getActivity(),
                getResources().getString(R.string.dialog_title_opponent_search),
                getResources().getString(R.string.dialog_message_opponent_search),
                true, true, new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {

                    }
                });
    }

    private void onDiscover() {
        searchPeer();
    }

    private void onClickWifiSetting() {
        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }

    public void clearPeers() {
        peers.clear();
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    private void searchPeer() {
        boolean isWifiP2pEnabled = ((MainActivity) getActivity()).isWifiP2pEnabled();
        if (!isWifiP2pEnabled) {
            //検索ボタン押下時、WiFiがOffの場合アラートダイアログを表示する
            new AlertDialog.Builder(getActivity())
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

        onInitiateDiscovery();

        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peersList) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        peers.clear();
        peers.addAll(peersList.getDeviceList());
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onStateChanged(Intent intent) {
    }

    @Override
    public void onPeersChanged(Intent intent) {
        if (manager != null) {
            manager.requestPeers(channel, this);
        }
    }

    @Override
    public void onConnectionChanged(Intent intent) {
        if (manager == null) {
            return;
        }

        NetworkInfo networkInfo = (NetworkInfo) intent
                .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

        if (!networkInfo.isConnected()) {
            //Disconnected
            clearPeers();
        }
    }

    @Override
    public void onDeviceChanged(Intent intent) {

    }

}
