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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MasterRoomFragment extends ListFragment
        implements OnReceiveListener, View.OnClickListener, ConnectionInfoListener,
        GroupInfoListener, DeviceActionListener, Handler.Callback {

    private WifiP2pDevice device;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiP2pInfo info;
    private List<WifiP2pDevice> peers = new ArrayList<>();
    private Handler handler;
    private ChatManager chatManager;
    private View contentView;
    private Thread thread;


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
        handler = new Handler(this);

        discover();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //リスナー登録
        ((MainActivity) getActivity()).getEventManager().addOnReceiveListener(this);
        contentView = inflater.inflate(R.layout.fragment_master_room, container, false);
        return contentView;
    }

    @Override
    public void onDestroyView() {
        //リスナー削除
        ((MainActivity) getActivity()).getEventManager().removeOnReceiveListener(this);
        getHandler().removeCallbacks(getThread());
        super.onDestroyView();
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
        disconnect();
        getFragmentManager().beginTransaction()
                .replace(R.id.container_root, new TitleFragment())
                .commit();
    }

    private void onClickGameStart() {

    }

    private void onMessage(String message) {
        if (message.equals(MainActivity.GAME_READY)) {
        }
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
            manager.requestGroupInfo(channel, this);
        } else {
            peers.clear();
            ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
            discover();
        }
    }

    @Override
    public void onDeviceChanged(Intent intent) {
    }

    /*
    Implemented ConnectionInfoListener
     */
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        this.info = info;

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


    /*
    Implemented GroupInfoListener
     */
    @Override
    public void onGroupInfoAvailable(WifiP2pGroup group) {
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

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MainActivity.MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                onMessage(readMessage);
                break;

            case MainActivity.MY_HANDLE:
                ChatManager obj = (ChatManager) msg.obj;
                setChatManager(obj);
                break;

        }
        return true;
    }
}
