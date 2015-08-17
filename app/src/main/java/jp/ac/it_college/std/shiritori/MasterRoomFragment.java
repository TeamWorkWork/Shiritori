package jp.ac.it_college.std.shiritori;


import android.app.ListFragment;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MasterRoomFragment extends ListFragment
        implements OnReceiveListener, View.OnClickListener, ConnectionInfoListener,
        GroupInfoListener, DeviceActionListener {

    private WifiP2pDevice device;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiP2pInfo info;
    private List<WifiP2pDevice> peers = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity) getActivity()).getEventManager().addOnReceiveListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.device = ((MainActivity) getActivity()).getDevice();
        ((TextView) getView().findViewById(R.id.my_name)).setText(device.deviceName);

        getView().findViewById(R.id.btn_game_start).setOnClickListener(this);
        getView().findViewById(R.id.btn_room_exit).setOnClickListener(this);

        setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.row_devices, peers));
        manager = ((MainActivity) getActivity()).getManager();
        channel = ((MainActivity) getActivity()).getChannel();

        discover();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_room, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disconnect();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_game_start:
                onClickGameStart();
                break;
            case R.id.btn_room_exit:
                onClickRoomExit();
        }
    }

    private void onClickRoomExit() {
        //タイトル画面に戻る
        getFragmentManager().beginTransaction()
                .replace(R.id.container_root, new TitleFragment())
                .commit();
    }

    private void onClickGameStart() {

    }

    private void discover() {
        if (manager != null) {
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

    /*
    Implemented OnReceiveListener
     */
    @Override
    public void onStateChanged(Intent intent) {
        Toast.makeText(getActivity(), "onStateChanged", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPeersChanged(Intent intent) {
        Toast.makeText(getActivity(), "onPeersChanged", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionChanged(Intent intent) {
        Toast.makeText(getActivity(), "onConnectionChanged", Toast.LENGTH_SHORT).show();
        if (manager == null) {
            return;
        }

        NetworkInfo networkInfo = (NetworkInfo) intent
                .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

        if (networkInfo.isConnected()) {
            manager.requestConnectionInfo(channel, this);
            manager.requestGroupInfo(channel, this);
        } else {
            peers.clear();
            ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
            discover();
        }
    }

    @Override
    public void onDeviceChanged(Intent intent) {
        Toast.makeText(getActivity(), "onDeviceChanged", Toast.LENGTH_SHORT).show();
    }

    /*
    Implemented ConnectionInfoListener
     */
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        Toast.makeText(getActivity(), "onConnectionInfoAvailable", Toast.LENGTH_SHORT).show();
        this.info = info;
    }

    /*
    Implemented GroupInfoListener
     */
    @Override
    public void onGroupInfoAvailable(WifiP2pGroup group) {
        Toast.makeText(getActivity(), "onGroupInfoAvailable", Toast.LENGTH_SHORT).show();
        peers.clear();
        if (info.isGroupOwner) {
            peers.addAll(group.getClientList());
        } else {
            peers.add(group.getOwner());
        }
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
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
