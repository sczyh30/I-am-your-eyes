package hackxfdu.io.youreyes.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;

/**
 * An abstract base activity for the app.
 * This activity class encapsulates the logic of `onCreate` method.
 *
 * @author sczyh30
 * @since 0.2
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content layout
        if (getLayoutId() != 0) {
            setContentView(getLayoutId());
        }
        // Inject views
        ButterKnife.bind(this);
        // Do initialization
        init(savedInstanceState);
    }

    /**
     * Do some initialization.
     * This method needs to be implemented by all sub-classes.
     */
    protected abstract void init(Bundle savedInstanceState);

    /**
     * Get id of current layout.
     * This method is used to set the content layout.
     *
     * @return id of current layout
     */
    protected int getLayoutId() {
        return 0;
    }

    protected boolean hasBackButton() {
        return false;
    }

}
