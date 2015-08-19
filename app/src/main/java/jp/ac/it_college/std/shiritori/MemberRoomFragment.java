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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MemberRoomFragment extends ListFragment
        implements OnReceiveListener, View.OnClickListener,
        DeviceActionListener, ConnectionInfoListener, Handler.Callback {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private List<WifiP2pDevice> peers = new ArrayList<>();
    private WifiP2pDevice device;
    private WifiP2pGroup group;
    private WifiP2pInfo info;
    private Handler handler;
    private ChatManager chatManager;
    private Thread thread;
    private View contentView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        contentView = inflater.inflate(R.layout.fragment_master_room, container, false);

        //リスナー登録
        ((MainActivity) getActivity()).getEventManager().addOnReceiveListener(this);

        this.device = ((MainActivity) getActivity()).getDevice();
        ((TextView) contentView.findViewById(R.id.my_name)).setText(device.deviceName);
        contentView.findViewById(R.id.btn_game_ready).setOnClickListener(this);
        contentView.findViewById(R.id.btn_room_exit).setOnClickListener(this);

        setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.row_devices, peers));
        manager = ((MainActivity) getActivity()).getManager();
        channel = ((MainActivity) getActivity()).getChannel();
        handler = new Handler(this);

        if (getArguments() != null) {
            group = getArguments().getParcelable(MainActivity.WIFI_GROUP);
            info = getArguments().getParcelable(MainActivity.WIFI_INFO);
            //対戦相手をリストにセット
            setOpponents(info, group);
            onConnectionInfoAvailable(info);
        }

        return contentView;
    }

    @Override
    public void onDestroyView() {
        //リスナー削除
        ((MainActivity) getActivity()).getEventManager().removeOnReceiveListener(this);
        getHandler().removeCallbacks(getThread());
        super.onDestroyView();
    }

    public Handler getHandler() {
        return handler;
    }

    public Thread getThread() {
        return thread;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public void setChatManager(ChatManager chatManager) {
        this.chatManager = chatManager;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_game_ready:
                onClickGameReady();
                break;
            case R.id.btn_room_exit:
                roomExit();
                break;
        }
    }

    private void onClickGameReady() {
        if (chatManager != null) {
            chatManager.write(MainActivity.GAME_READY.getBytes());
            ((TextView) contentView.findViewById(R.id.my_status)).setText(R.string.game_ready);
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

    private void roomExit() {
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
        if (manager == null) {
            return;
        }

        NetworkInfo networkInfo = (NetworkInfo) intent
                .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

        if (!networkInfo.isConnected()) {
            roomExit();
        }
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

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {

        if (info.isGroupOwner) {
            try {
                thread = new GroupOwnerSocketHandler(getHandler());
                thread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            thread = new ClientSocketHandler(getHandler(), info.groupOwnerAddress);
            thread.start();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MainActivity.MESSAGE_READ:

                break;

            case MainActivity.MY_HANDLE:
                ChatManager obj = (ChatManager) msg.obj;
                setChatManager(obj);
                break;
        }
        return true;
    }
}
