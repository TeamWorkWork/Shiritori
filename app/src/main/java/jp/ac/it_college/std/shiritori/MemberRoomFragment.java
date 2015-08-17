package jp.ac.it_college.std.shiritori;


import android.app.ListFragment;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MemberRoomFragment extends ListFragment
        implements OnReceiveListener, View.OnClickListener, DeviceActionListener {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private List<WifiP2pDevice> peers = new ArrayList<>();
    private WifiP2pDevice device;
    private WifiP2pGroup group;
    private WifiP2pInfo info;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.device = ((MainActivity) getActivity()).getDevice();
        ((TextView) getView().findViewById(R.id.my_name)).setText(device.deviceName);
        getView().findViewById(R.id.btn_game_ready).setOnClickListener(this);
        getView().findViewById(R.id.btn_room_exit).setOnClickListener(this);

        setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.row_devices, peers));
        manager = ((MainActivity) getActivity()).getManager();
        channel = ((MainActivity) getActivity()).getChannel();

        if (getArguments() != null) {
            group = getArguments().getParcelable(MainActivity.WIFI_GROUP);
            info = getArguments().getParcelable(MainActivity.WIFI_INFO);
        }

        //対戦相手をリストにセット
        setOpponents(info, group);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        //リスナー登録
        ((MainActivity) getActivity()).getEventManager().addOnReceiveListener(this);
        return inflater.inflate(R.layout.fragment_member_room, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //リスナー削除
        ((MainActivity) getActivity()).getEventManager().removeOnReceiveListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_game_ready:
                break;
            case R.id.btn_room_exit:
                onClickRoomExit();
                break;
        }
    }

    private void setOpponents(WifiP2pInfo info, WifiP2pGroup group) {
        peers.clear();
        if (info.isGroupOwner) {
         peers.addAll(group.getClientList());
        } else {
            peers.add(group.getOwner());
        }
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    private void onClickRoomExit() {
        disconnect();
        getFragmentManager().beginTransaction()
                .replace(R.id.container_root, new OpponentListFragment())
                .commit();
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

    }

    @Override
    public void onDeviceChanged(Intent intent) {

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
