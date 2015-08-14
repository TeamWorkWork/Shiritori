package jp.ac.it_college.std.shiritori;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;

public interface DeviceActionListener {

    void cancelDisconnect();

    void connect(WifiP2pConfig config);

    void disconnect();

    void searchPeer();
}
