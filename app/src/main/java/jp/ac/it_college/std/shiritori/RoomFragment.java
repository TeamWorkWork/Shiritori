package jp.ac.it_college.std.shiritori;


import android.app.ListFragment;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RoomFragment extends ListFragment implements OnReceiveListener {

    private WifiP2pDevice device;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.device = ((MainActivity) getActivity()).getDevice();
        ((TextView) getView().findViewById(R.id.my_name)).setText(device.deviceName);
        ((MainActivity) getActivity()).getEventManager().addOnReceiveListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_room, container, false);
    }

    @Override
    public void onStateChanged(Intent intent) {

    }

    @Override
    public void onPeersChanged(Intent intent) {

    }

    @Override
    public void onConnectionChanged(Intent intent) {

    }

    @Override
    public void onDeviceChanged(Intent intent) {

    }
}
