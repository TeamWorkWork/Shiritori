package jp.ac.it_college.std.shiritori;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ResultFragment extends Fragment implements View.OnClickListener {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.layout_result, container, false);
        contentView.findViewById(R.id.btn_to_title).setOnClickListener(this);
        String result = getArguments().getString(MainActivity.GAME_RESULT);
        ((TextView) contentView.findViewById(R.id.lbl_result)).setText(result);
        return contentView;
    }

    private void gotoTitle() {
        getFragmentManager().beginTransaction()
                .replace(R.id.container_root, new TitleFragment())
                .commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_to_title:
                gotoTitle();
                break;
        }
    }
}
