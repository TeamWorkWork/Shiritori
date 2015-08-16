package jp.ac.it_college.std.shiritori;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class OpponentDetailFragment extends Fragment
        implements View.OnClickListener, ConnectionInfoListener, OnReceiveListener, DeviceActionListener{

    private WifiP2pDevice device;
    private ProgressDialog  progressDialog;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EventManagerのセット
        ((MainActivity) getActivity()).getEventManager().addOnReceiveListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //各ボタンのクリックリスナーをセット
        getView().findViewById(R.id.btn_connect).setOnClickListener(this);
        getView().findViewById(R.id.btn_disconnect).setOnClickListener(this);

        this.manager = ((MainActivity) getActivity()).getManager();
        this.channel = ((MainActivity) getActivity()).getChannel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_opponent_detail, container, false);
    }

    private void onClickDisconnect() {
        disconnect();
    }

    private void onClickConnect() {
        connect(new WifiP2pConfig());
    }

    public void resetViews() {
        TextView view = (TextView) getView().findViewById(R.id.lbl_device_info);
        view.setText(R.string.empty);
        getView().setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_connect:
                onClickConnect();
                break;
            case R.id.btn_disconnect:
                onClickDisconnect();
        }
    }

    /*
    Implemented ConnectionInfoListener
     */
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    /*
    Implemented DeviceActionListener
     */
    @Override
    public void cancelDisconnect() {
        if (manager != null) {
            if (device == null || device.status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (device.status == WifiP2pDevice.AVAILABLE
                    || device.status == WifiP2pDevice.INVITED) {
                manager.cancelConnect(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(int reason) {

                    }
                });
            }
        }
    }

    @Override
    public void connect(WifiP2pConfig config) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        progressDialog = ProgressDialog.show(getActivity(), getResources().getString(R.string.dialog_title_connect),
                device.deviceName + getResources().getString(R.string.dialog_message_to_connect), true, true,
                new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        ((DeviceActionListener) getActivity()).cancelDisconnect();
                    }
                }
        );

        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

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
        resetViews();

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
        this.device = device;
        getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) getView().findViewById(R.id.lbl_device_info);
        view.setText(device.toString());
    }

    /*
    Implemented OnReceiveListener
     */
    @Override
    public void onStateChanged(Intent intent) {

    }

    @Override
    public void onPeersChanged(Intent intent) {

    }

    @Override
    public void onConnectionChanged(Intent intent) {
        if (manager == null) {
            return;
        }

        NetworkInfo networkInfo = (NetworkInfo) intent
                .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

        if (networkInfo.isConnected()) {
            manager.requestConnectionInfo(channel, this);
        } else {
            resetViews();
        }
    }

    @Override
    public void onDeviceChanged(Intent intent) {

    }
}
