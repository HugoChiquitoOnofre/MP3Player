package pdm.project.mp3player.ui.player;

import static android.content.Context.USER_SERVICE;
import static pdm.project.mp3player.ui.player.ApplicationClass.ACTION_NEXT;
import static pdm.project.mp3player.ui.player.ApplicationClass.ACTION_PLAY;
import static pdm.project.mp3player.ui.player.ApplicationClass.ACTION_PREVIOUS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String actionName = intent.getAction();
        Intent serviceIntent = new Intent(context, MusicService.class);

        if (actionName != null) {
            switch (actionName) {
                case ACTION_PLAY:
                    serviceIntent.putExtra("actionName", "playPause");
                    context.startService(serviceIntent);
                    break;
                case ACTION_NEXT:
                    serviceIntent.putExtra("actionName", "next");
                    context.startService(serviceIntent);
                    break;
                case ACTION_PREVIOUS:
                    serviceIntent.putExtra("actionName", "previous");
                    context.startService(serviceIntent);
                    break;
            }
        }
    }
}
