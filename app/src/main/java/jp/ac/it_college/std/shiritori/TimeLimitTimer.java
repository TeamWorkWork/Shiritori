package jp.ac.it_college.std.shiritori;

import android.os.CountDownTimer;
import android.widget.TextView;

public class TimeLimitTimer extends CountDownTimer {

    private TimerActionLister lister;
    private TextView view;

    public TimeLimitTimer(long millisInFuture, long countDownInterval,
                          TextView view, TimerActionLister lister) {
        super(millisInFuture, countDownInterval);
        this.lister = lister;
        this.view = view;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        float sec = (float)millisUntilFinished / 1000;
        String timer = String.format("%.2f", sec);
        view.setText(timer);
    }

    @Override
    public void onFinish() {
        lister.onFinish();
    }

    public interface TimerActionLister {
        void onFinish();
    }
}
