package jp.ac.it_college.std.shiritori;


import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class OpponentListFragment extends ListFragment
        implements View.OnClickListener, PeerListListener {

    private List<WifiP2pDevice> peers = new ArrayList<>();
    private ProgressDialog progressDialog;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //各ボタンのOnClickListenerを設定
        getView().findViewById(R.id.btn_wifi_setting).setOnClickListener(this);
        getView().findViewById(R.id.btn_discover).setOnClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_opponent_list, container, false);
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peersList) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        peers.clear();
        peers.addAll(peersList.getDeviceList());

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_wifi_setting:
                //WiFi設定ボタンが押された時の処理
                showWifiSetting();
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
        progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel", "finding peers", true,
                true, new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {

                    }
                });
    }

    private void onDiscover() {
        ((DeviceActionListener) getActivity()).searchPeer();
    }

    private void showWifiSetting() {
        startActivity(new Intent(Settings.ACTION_WIFI_IP_SETTINGS));
    }

    private class WiFiPeerListAdapter extends ArrayAdapter {

        private List<WifiP2pDevice> items;

        public WiFiPeerListAdapter(Context context, int resource, List objects) {
            super(context, resource, objects);
            items = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return super.getView(position, convertView, parent);
        }
    }
}
