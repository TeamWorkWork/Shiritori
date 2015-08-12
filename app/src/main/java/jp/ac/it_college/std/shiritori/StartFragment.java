package jp.ac.it_college.std.shiritori;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class StartFragment extends Fragment implements View.OnClickListener{

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // タイトル画面の「START」ラベルを点滅させる
        TextView startTextView = (TextView) getView().findViewById(R.id.lbl_start);
        new Winker(startTextView).startWink();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_start, container, false);
        view.findViewById(R.id.container_start).setOnClickListener(this);

        return view;
    }

    private void startClicked() {
        //「START」ラベルが押された場合フラグメントを切り替える
        getFragmentManager().beginTransaction()
                .replace(R.id.container_bottom, new ModeSelectFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.container_start:
                startClicked();
                break;
        }
    }
}
