package jp.ac.it_college.std.shiritori;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Viewを点滅させる
        TextView startTextView = (TextView) findViewById(R.id.lbl_start);
        Winker winker = new Winker(startTextView);
        winker.startWink();

        // Viewのクリックリスナーをセット
        startTextView.setOnClickListener(this);
    }

    private void startClicked() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lbl_start:
                startClicked();
        }
    }
}
