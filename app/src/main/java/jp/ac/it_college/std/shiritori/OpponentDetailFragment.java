package jp.ac.it_college.std.shiritori;


import android.app.ProgressDialog;
import android.content.DialogInterface;
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

public class OpponentDetailFragment extends Fragment implements View.OnClickListener, ConnectionInfoListener{

    private WifiP2pDevice device;
    private ProgressDialog  progressDialog;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //各ボタンのクリックリスナーをセット
        getView().findViewById(R.id.btn_connect).setOnClickListener(this);
        getView().findViewById(R.id.btn_disconnect).setOnClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_opponent_detail, container, false);
    }

    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) getView().findViewById(R.id.lbl_device_info);
        view.setText(device.toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_connect:
                wifiP2pConnect();
                break;
        }
    }

    private void wifiP2pConnect() {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        progressDialog = ProgressDialog.show(getActivity(), getResources().getString(R.string.connect),
                device.deviceName + getResources().getString(R.string.to_connect), true, true,
                        new DialogInterface.OnCancelListener() {

                            @Override
                            public void onCancel(DialogInterface dialog) {
                                ((DeviceActionListener) getActivity()).cancelDisconnect();
                            }
                        }
        );

        ((DeviceActionListener) getActivity()).connect(config);

    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
