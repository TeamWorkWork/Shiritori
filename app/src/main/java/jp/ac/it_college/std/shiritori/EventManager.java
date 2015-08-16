package jp.ac.it_college.std.shiritori;

import android.content.Intent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EventManager implements OnReceiveListener {
    private List<OnReceiveListener> listeners = new ArrayList<>();
    private Iterator<OnReceiveListener> iterator;

    public EventManager() {
        iterator = listeners.iterator();
    }

    public void addOnReceiveListener(OnReceiveListener listener) {
        listeners.add(listener);
    }

    /*
    Implemented OnReceiveListener
     */

    /* WiFi Directの有効/無効状態が通知される。機能制限やユーザへの通知に利用 */
    @Override
    public void onStateChanged(Intent intent) {
        while (iterator.hasNext()) {
            iterator.next().onStateChanged(intent);
        }
    }


    /* デバイス情報の変更通知（通信可能なデバイスの発見・ロストなど） */
    @Override
    public void onPeersChanged(Intent intent) {
        while (iterator.hasNext()) {
            iterator.next().onPeersChanged(intent);
        }
    }

    /* IPアドレスなどコネクション情報。通信状態の変更通知 */
    @Override
    public void onConnectionChanged(Intent intent) {
        while (iterator.hasNext()) {
            iterator.next().onConnectionChanged(intent);
        }
    }

    /* 自分自身のデバイス状態の変更通知(相手デバイスではないことに注意) */
    @Override
    public void onDeviceChanged(Intent intent) {
        while (iterator.hasNext()) {
            iterator.next().onDeviceChanged(intent);
        }
    }
}
