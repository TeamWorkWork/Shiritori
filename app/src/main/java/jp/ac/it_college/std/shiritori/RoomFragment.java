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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession.SpellCheckerSessionListener;
import android.view.textservice.SuggestionsInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.ac.it_college.std.shiritori.TimeLimitTimer.TimerActionLister;

public class RoomFragment extends ListFragment
        implements OnReceiveListener, DeviceActionListener,Handler.Callback,
        View.OnClickListener, ConnectionInfoListener, GroupInfoListener,
        SpellCheckerSessionListener, TimerActionLister {

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
    private SpellChecker spellChecker;
    private String lastStr;
    private boolean isGameOver;
    private TextView timeLimit;
    private TimeLimitTimer timer;

    public static final String YOU_WIN = "YOU WIN!";
    public static final String YOU_LOSE = "YOU LOSE...";
    private static final long START_TIME = 20000; //開始時間(20秒)
    private static final long INTERVAL = 10; //インターバル(0.01秒)


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
        //通信切断
        disconnect();
        //タイマーを停止
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
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

        //SpellCheckerのインスタンスを生成
        spellChecker = new SpellChecker(getActivity(), this);
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
        if (!isGameOver) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container_root, new TitleFragment())
                    .commit();
        }
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
            case MainActivity.GAME_OVER:
                gameOver(YOU_WIN);
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

        //タイムリミット
        timeLimit = (TextView) contentView.findViewById(R.id.time_limit);


        //しりとりの順番用フラグをセット
        if (this instanceof MasterRoomFragment) {
            isMyTurn = true;
        } else {
            isMyTurn = false;
        }

        setChatLineEnabled(isMyTurn);
    }

    /**
     * ゲーム終了処理
     * @param gameResult ゲーム結果
     */
    private void gameOver(String gameResult) {
        isGameOver = true;
        if (gameResult.equals(YOU_LOSE)) {
            //相手にゲーム終了を通知
            getChatManager().write(MainActivity.GAME_OVER.getBytes());
        }

        //チャットラインを無効にする
        chatLine.setEnabled(false);

        //ゲーム結果をバンドルにセット
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.GAME_RESULT, gameResult);
        //フラグメントにバンドルをセット
        ResultFragment fragment = new ResultFragment();
        fragment.setArguments(bundle);
        //フラグメント切り替え
        getFragmentManager().beginTransaction()
                .replace(R.id.container_root, fragment)
                .commit();
    }

    /**
     * しりとりの順番に応じてチャットラインの有効/無効をセット
     * @param myTurn
     */
    private void setChatLineEnabled(boolean myTurn) {
        int hint = myTurn ? R.string.txt_hint_my_turn : R.string.txt_hit_opponent_turn;

        if (myTurn) {
            timer = new TimeLimitTimer(START_TIME, INTERVAL, timeLimit, this);
            timer.start();
        } else {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            float time = (float)START_TIME / 1000;
            timeLimit.setText(String.format("%.2f", time));
        }

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

        //相手から送られてきた最後の文字を切り取る
        String lastStr = msg.substring(msg.length() - 1);
        if (name.equals(getString(R.string.txt_opponent))) {
            this.lastStr = lastStr;
        }

        String result = String.format(
                getText(R.string.end_of_line).toString(), name,
                msg.substring(0, msg.length() - 1), lastStr);
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
        String line = chatLine.getText().toString();
        if (line.length() <= 1) {
            Toast.makeText(getActivity(), "２文字以上の単語を入力してください", Toast.LENGTH_SHORT).show();
            return;
        }

        if (lastStr != null &&
                !lastStr.isEmpty() && !line.startsWith(lastStr)) {
            Toast.makeText(getActivity(), "頭文字が違います。", Toast.LENGTH_SHORT).show();
            return;
        }

        //全て半角アルファベットかどうか（大文字でも小文字でもOK）
        String regex = "^[a-zA-z¥s]+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(line);

        //入力値にアルファベット以外が含まれているかチェック
        if (m.find()) {
            //スペルチェックを実行
            spellChecker.spellCheck(line);
        } else {
            //アルファベット以外が含まれている場合、ゲームオーバー
            gameOver(YOU_LOSE);
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

    /*
    Implemented SpellCheckerSession.SpellCheckerSessionListener
     */
    @Override
    public void onGetSuggestions(SuggestionsInfo[] results) {
        for (SuggestionsInfo result : results) {
            if (result.getSuggestionsCount() <= 0) {
                //候補がない場合(スペルOK、メッセージ送信)
                if (getChatManager() != null) {
                    getChatManager().write(chatLine.getText().toString().getBytes());
                    pushMessage(getString(R.string.txt_me), chatLine.getText().toString());
                    chatLine.setText("");
                }
            } else {
                //候補がある場合(スペルミス&ゲームオーバー)
                gameOver(YOU_LOSE);

            }
        }

    }

    @Override
    public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] results) {

    }

    /*
    Implemented TimeLimitTimer.TimerActionLister
     */

    /**
     * 制限時間が0を下回った際に呼ばれる
     */
    @Override
    public void onFinish() {
        //ゲームオーバー処理
        gameOver(YOU_LOSE);
    }
}
