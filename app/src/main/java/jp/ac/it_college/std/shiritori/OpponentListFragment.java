package jp.ac.it_college.std.shiritori;


import android.app.ListFragment;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class OpponentListFragment extends ListFragment implements PeerListListener{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_opponent_list, container, false);
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {

    }
}
