package hackxfdu.io.youreyes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import hackxfdu.io.youreyes.ui.MainActivity;


/**
 * App start activity.
 *
 * @author sczyh30
 */
public class AppStart extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final View view = View.inflate(this, R.layout.activity_welcome, null);
        setContentView(view);
        // Start a transition animation
        AlphaAnimation aa = new AlphaAnimation(0.5f, 1.0f);
        aa.setDuration(1000);
        view.startAnimation(aa);
        aa.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                doStart();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }
        });
    }

    /**
     * Start the main activity.
     */
    private void doStart() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
