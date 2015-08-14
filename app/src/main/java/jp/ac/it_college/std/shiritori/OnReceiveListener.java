package jp.ac.it_college.std.shiritori;

import android.content.Intent;

public interface OnReceiveListener {
    void onStateChanged(Intent intent);
    void onPeersChanged(Intent intent);
    void onConnectionChanged(Intent intent);
    void onDeviceChanged(Intent intent);
}
