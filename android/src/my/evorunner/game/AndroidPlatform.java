package my.evorunner.game;

import android.app.Activity;
import android.widget.Toast;

public class AndroidPlatform implements NativePlatform{
    private Activity context;
    public AndroidPlatform(Activity context) {
        this.context = context;
    }
    @Override
    public void displayError(final String errorMessage) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,errorMessage,Toast.LENGTH_SHORT).show();
            }
        });
    }
}
