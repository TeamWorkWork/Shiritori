package jp.ac.it_college.std.shiritori;


import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class OpponentDetailFragment extends Fragment implements View.OnClickListener{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_opponent_detail, container, false);
    }

    public void showDetails(WifiP2pDevice device) {
        getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) getView().findViewById(R.id.lbl_device_info);
        view.setText(device.toString());
    }

    @Override
    public void onClick(View v) {

    }
}
