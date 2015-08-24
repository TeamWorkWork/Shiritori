package jp.ac.it_college.std.shiritori;


import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MasterRoomFragment extends RoomFragment
        implements OnReceiveListener, View.OnClickListener {

    private boolean isGameRunning = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setContentView(inflater.inflate(R.layout.fragment_master_room, container, false));
        super.onCreateView(inflater, container, savedInstanceState);

        //ゲーム開始ボタンの設定
        setUpGameButton(false, R.string.game_start, this);

        //周辺の端末にルーム作成を通知
        discover();

        return contentView;
    }

    /**
     * 周囲の端末を検索する
     */
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

    /**
     * 「ゲーム開始」ボタンが押された際の処理
     */
    private void onClickGameStart() {
        if (getChatManager() == null) {
            return;
        }
        // 相手にゲームスタートを通知
        getChatManager().write(MainActivity.GAME_START.getBytes());
        //ゲーム画面に切り替える
        gameStart();
        //ゲーム中かどうかのフラグをtrueに設定
        isGameRunning = true;
    }

    /*
    Implemented View.OnClickListener
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_game_start_or_ready:
                onClickGameStart();
                break;
        }
        super.onClick(v);
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
        super.onConnectionChanged(intent);
        if (manager == null) {
            return;
        }

        NetworkInfo networkInfo = (NetworkInfo) intent
                .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

        if (!networkInfo.isConnected()) {
            //disconnected
            if (isGameRunning) {
                //ルーム退室
                roomExit();
            } else {
                //PeerListをクリア
                peers.clear();
                ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();

                //ゲーム開始ボタンを無効にする
                contentView.findViewById(R.id.btn_game_start_or_ready).setEnabled(false);
                //周囲の端末を再検索
                discover();
            }

        }
    }

    @Override
    public void onDeviceChanged(Intent intent) {
    }

}
