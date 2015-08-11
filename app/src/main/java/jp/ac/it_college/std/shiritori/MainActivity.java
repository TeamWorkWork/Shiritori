package jp.ac.it_college.std.shiritori;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Viewを点滅させる
        TextView startTextView = (TextView) findViewById(R.id.lbl_start);
        Winker winker = new Winker(startTextView);
        winker.startWink();
    }

}
