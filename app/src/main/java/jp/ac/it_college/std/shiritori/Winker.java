package jp.ac.it_college.std.shiritori;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.os.Handler;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Winker {
    private Handler handler;
    private ScheduledExecutorService scheduledExecutor;
    private View view;

    public Winker(View view) {
        this.view = view;
        this.handler = new Handler();
        this.scheduledExecutor = Executors.newScheduledThreadPool(2);
    }

    public void startWink() {
        scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        view.setVisibility(View.VISIBLE);

                        // HONEYCOMBより前のAndroid SDKがProperty Animation非対応のため
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            animateAlpha();
                        }
                    }
                });
            }
        }, 0, 1700, TimeUnit.MILLISECONDS);
    }

    private void animateAlpha() {
        // 実行するAnimatorのリスト
        List<Animator> animatorList = new ArrayList<Animator>();

        // alpha値を0から1へ1000ミリ秒かけて変化させる。
        ObjectAnimator animeFadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        animeFadeIn.setDuration(1000);

        // alpha値を1から0へ600ミリ秒かけて変化させる。
        ObjectAnimator animeFadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        animeFadeOut.setDuration(600);

        // 実行対象Animatorリストに追加。
        animatorList.add(animeFadeIn);
        animatorList.add(animeFadeOut);

        final AnimatorSet animatorSet = new AnimatorSet();

        // リストの順番に実行
        animatorSet.playSequentially(animatorList);

        animatorSet.start();
    }
}
