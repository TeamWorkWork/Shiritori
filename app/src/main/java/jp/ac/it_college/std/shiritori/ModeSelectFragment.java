package jp.ac.it_college.std.shiritori;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ModeSelectFragment extends Fragment implements View.OnClickListener{

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //各ボタンのクリックリスナーをセット
        getView().findViewById(R.id.btn_search_room).setOnClickListener(this);
        getView().findViewById(R.id.btn_create_room).setOnClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mode_select, container, false);
    }

    private void searchRoomBtnClicked() {
        getFragmentManager().beginTransaction()
                .replace(R.id.container_root, new OpponentListFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_search_room:
                //ルーム検索ボタンが押された時の処理
                searchRoomBtnClicked();
                break;
            case R.id.btn_create_room:
                break;
        }
    }
}
