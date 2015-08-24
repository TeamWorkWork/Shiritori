package jp.ac.it_college.std.shiritori;


import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MemberRoomFragment extends RoomFragment
        implements OnReceiveListener, View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setContentView(inflater.inflate(R.layout.fragment_master_room, container, false));
        super.onCreateView(inflater, container, savedInstanceState);

        //準備完了ボタンの設定
        setUpGameButton(true, R.string.game_ready);

        //受け取ったinfo/group情報をセットする
        if (getArguments() != null) {
            WifiP2pInfo info = getArguments().getParcelable(MainActivity.WIFI_INFO);
            WifiP2pGroup group = getArguments().getParcelable(MainActivity.WIFI_GROUP);
            onConnectionInfoAvailable(info);
            onGroupInfoAvailable(group);
        }
        return contentView;
    }

    /**
     * 「準備完了」ボタンが押された際の処理
     */
    private void onClickGameReady() {
        if (getChatManager() != null) {
            getChatManager().write(MainActivity.GAME_READY.getBytes());
            ((TextView) contentView.findViewById(R.id.my_status)).setText(R.string.game_ready);
        }
    }

    /*
    Implemented View.OnClickListener
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_game_start_or_ready:
                onClickGameReady();
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
        if (manager == null) {
            return;
        }

        NetworkInfo networkInfo = (NetworkInfo) intent
                .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

        if (!networkInfo.isConnected()) {
            //disconnect
            roomExit();
        }
    }

    @Override
    public void onDeviceChanged(Intent intent) {

    }

}
