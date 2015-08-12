package jp.ac.it_college.std.shiritori;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity implements OnReceiveListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container_bottom, new StartFragment())
                    .commit();
        }
    }

    /*
    Implemented OnReceiveListener
     */

    /* WiFi Directの有効/無効状態が通知される。機能制限やユーザへの通知に利用 */
    @Override
    public void onStateChanged(Intent intent) {

    }

    /* デバイス情報の変更通知（通信可能なデバイスの発見・ロストなど） */
    @Override
    public void onPeersChanged(Intent intent) {

    }

    /* IPアドレスなどコネクション情報。通信状態の変更通知 */
    @Override
    public void onConnectionChanged(Intent intent) {

    }

    /* 自分自身のデバイス状態の変更通知(相手デバイスではないことに注意) */
    @Override
    public void onDeviceChanged(Intent intent) {

    }
}
