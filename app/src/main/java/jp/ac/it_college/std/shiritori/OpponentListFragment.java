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
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class OpponentListFragment extends ListFragment
        implements View.OnClickListener, PeerListListener {

    private List<WifiP2pDevice> peers = new ArrayList<>();
    private ProgressDialog progressDialog;

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
    public void onPeersAvailable(WifiP2pDeviceList peersList) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        peers.clear();
        peers.addAll(peersList.getDeviceList());
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
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
        progressDialog = ProgressDialog.show(getActivity(),
                getResources().getString(R.string.dialog_title),
                getResources().getString(R.string.dialog_message),
                true, true, new DialogInterface.OnCancelListener() {

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
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row_devices, null);
            }
            WifiP2pDevice device = items.get(position);
            if (device != null) {
                TextView top = (TextView) v.findViewById(R.id.device_name);
                TextView bottom = (TextView) v.findViewById(R.id.device_details);
                if (top != null) {
                    top.setText(device.deviceName);
                }
                if (bottom != null) {
                    bottom.setText(getDeviceStatus(device.status));
                }
            }

            return v;
        }
    }
}
