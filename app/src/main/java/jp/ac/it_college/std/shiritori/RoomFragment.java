package jp.ac.it_college.std.shiritori;


import android.app.ListFragment;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RoomFragment extends ListFragment
        implements OnReceiveListener, DeviceActionListener,
        Handler.Callback, View.OnClickListener, ConnectionInfoListener, GroupInfoListener {

    //変数宣言
    WifiP2pDevice device;
    WifiP2pManager manager;
    WifiP2pManager.Channel channel;
    WifiP2pInfo info;
    List<WifiP2pDevice> peers = new ArrayList<>();

    View contentView;
    LinearLayout gameLayout;
    Handler handler;
    ChatManager chatManager;
    ListView chatListView;
    ChatMessageAdapter messageAdapter;
    List<String> chatList = new ArrayList<>();
    TextView chatLine;
    Thread thread;

    private boolean isMyTurn;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //初期設定
        initSettings();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        //OnReceiveListener削除
        ((MainActivity) getActivity()).getEventManager().removeOnReceiveListener(this);
        super.onDestroyView();
    }

    /**
     * 初期設定
     */
    private void initSettings() {
        device = ((MainActivity) getActivity()).getDevice();
        manager = ((MainActivity) getActivity()).getManager();
        channel = ((MainActivity) getActivity()).getChannel();
        gameLayout = (LinearLayout) contentView.findViewById(R.id.layout_game_room);

        //退室ボタンのOnClickListenerをセット
        contentView.findViewById(R.id.btn_room_exit).setOnClickListener(this);
        //対戦者リストのアダプターをセット
        setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.row_devices, peers));
        //ハンドラー生成
        handler = new Handler(this);

        //OnReceiveListener登録
        ((MainActivity) getActivity()).getEventManager().addOnReceiveListener(this);

        updateThisDevice();
    }

    /**
     * デバイス情報を更新
     */
    public void updateThisDevice() {
        ((TextView) contentView.findViewById(R.id.my_name)).setText(device.deviceName);
    }

    /* ▼ Accessor method ▼ */
    public void setContentView(View contentView) {
        this.contentView = contentView;
    }

    public Handler getHandler() {
        return handler;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public void setChatManager(ChatManager chatManager) {
        this.chatManager = chatManager;
    }
    /* ▲ Accessor method ▲ */

    /**
     * ゲーム開始・準備完了ボタンのセットアップ
     *
     * @param isEnabled
     * @param txtResId
     */
    public void setUpGameButton(boolean isEnabled, int txtResId) {
        Button button = (Button) contentView.findViewById(R.id.btn_game_start_or_ready);
        //ボタンの有効・無効セット
        button.setEnabled(isEnabled);
        //テキストをセット
        button.setText(txtResId);
        //クリックリスナーをセット
        button.setOnClickListener(this);
    }

    /**
     * ルーム退室処理
     */
    public void roomExit() {
        disconnect();
        getFragmentManager().beginTransaction()
                .replace(R.id.container_root, new TitleFragment())
                .commit();
    }

    /**
     * 受け取ったメッセージを処理する
     *
     * @param message 相手側から送られてくるメッセージ
     */
    private void onMessage(String message) {
        switch (message) {
            case MainActivity.GAME_READY:
                gameReady();
                break;
            case MainActivity.GAME_START:
                gameStart();
                break;
            default:
                pushMessage(getString(R.string.txt_opponent), message);
                break;
        }
    }

    /**
     * ゲーム準備処理
     */
    private void gameReady() {
        //準備完了ボタンが押された場合、「準備完了」をセット
        ((TextView) contentView.findViewById(R.id.device_details)).setText(R.string.game_ready);

        //ゲーム開始ボタンを有効化
        contentView.findViewById(R.id.btn_game_start_or_ready).setEnabled(true);
    }

    /**
     * ゲーム開始処理
     */
    public void gameStart() {
        //レイアウトにあるViewをすべてクリア
        gameLayout.removeAllViews();
        //レイアウトを切り替え
        getActivity().getLayoutInflater().inflate(R.layout.layout_game, gameLayout);

        //各ボタンのクリックイベントをセット
        gameLayout.findViewById(R.id.btn_send).setOnClickListener(this);
        gameLayout.findViewById(R.id.btn_room_exit).setOnClickListener(this);
        //アダプターの設定
        messageAdapter = new ChatMessageAdapter(getActivity(), android.R.id.text1, chatList);
        //チャットラインを取得
        chatLine = (TextView) gameLayout.findViewById(R.id.txtChatLine);
        //Chat用のListViewをセット
        chatListView = (ListView) gameLayout.findViewById(R.id.list_chat);
        chatListView.setAdapter(messageAdapter);

        //しりとりの順番用フラグをセット
        if (this instanceof MasterRoomFragment) {
            isMyTurn = true;
        } else {
            isMyTurn = false;
        }

        setChatLineEnabled(isMyTurn);
    }

    /**
     * しりとりの順番に応じてチャットラインの有効/無効をセット
     * @param myTurn
     */
    private void setChatLineEnabled(boolean myTurn) {
        int hint = myTurn ? R.string.txt_hint_my_turn : R.string.txt_hit_opponent_turn;

        chatLine.setHint(hint);
        chatLine.setEnabled(myTurn);
        //Sendボタンの有効/無効をセット
        gameLayout.findViewById(R.id.btn_send).setEnabled(myTurn);
    }

    /**
     * メッセージをListViewにaddする
     * @param name
     * @param msg
     */
    private void pushMessage(String name, String msg) {
        if (name.isEmpty() || msg.isEmpty()) {
            return;
        }

        String result = String.format(
                getText(R.string.end_of_line).toString(), name,
                msg.substring(0, msg.length() - 1), msg.substring(msg.length() - 1));
        messageAdapter.add(result);
        messageAdapter.notifyDataSetChanged();

        //ターンフラグを反転
        isMyTurn = !isMyTurn;
        //チャットラインの有効/無効を設定
        setChatLineEnabled(isMyTurn);
    }

    /**
     * 「Send」ボタンが押された際の処理
     */
    private void onClickSend() {
        if (getChatManager() != null) {
            getChatManager().write(chatLine.getText().toString().getBytes());
            pushMessage(getString(R.string.txt_me), chatLine.getText().toString());
            chatLine.setText("");
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

    /*
    Implemented Handler.CallBack
     */
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

    /*
    Implemented View.OnClickListener
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                onClickSend();
                break;
            case R.id.btn_room_exit:
                roomExit();
                break;
        }
    }


    /*
    Implemented WiFiP2pManager.ConnectionInfoListener
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
    Implemented WiFiP2pManager.GroupInfoListener
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
}
