package pdm.project.mp3player.ui.player;

import static pdm.project.mp3player.PlayerActivity.listSongs;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;

import pdm.project.mp3player.model.MusicFiles;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener{

    MyBinder myBinder = new MyBinder();
    MediaPlayer mediaPlayer;
    ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    Uri uri;
    int position = -1;
    ActionPlaying actionPlaying;
    public static String MUSIC_FILE_LAST_PLAYED = "LAST_PLAYED";
    public static String MUSIC_FILE = "STORED_MUSIC";
    public static String ARTIST_NAME = "ARTIST_NAME";
    public static String SONG_NAME = "SONG_NAME";


    @Override
    public void onCreate() {
        super.onCreate();
        musicFiles = listSongs;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("onBind", "Method");
        return myBinder;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (actionPlaying != null) {
            actionPlaying.nextBtnClicked();
            if (mediaPlayer != null) {
                createMediaPlayer(position);
                mediaPlayer.start();
                onComplete();
            }
        }
    }

    public void onComplete() {
        mediaPlayer.setOnCompletionListener(this);
    }

    public class MyBinder extends Binder {

        public MusicService getService() {
            return MusicService.this;
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("servicePosition", -1);

        String actionName = intent.getStringExtra("actionName");

        if (myPosition != -1) {
            playMedia(myPosition);
        }

        if (actionName != null) {
            switch (actionName) {
                case "playPause":
                    // Toast.makeText(this, "playPause", Toast.LENGTH_SHORT).show();
                    playPauseButtonClicked();
                    break;
                case "next":
                    // Toast.makeText(this, "next", Toast.LENGTH_SHORT).show();
                    nextButtonClicked();
                    break;
                case "previous":
                   // Toast.makeText(this, "previous", Toast.LENGTH_SHORT).show();
                    previousButtonClicked();
                    break;
            }
        }

        return START_STICKY;
    }


    private void playMedia(int startPosition) {
        musicFiles = listSongs;
        position = startPosition;

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();

            if (musicFiles != null) {
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        } else {
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }

    public void start(){
        mediaPlayer.start();
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void stop(){
        mediaPlayer.stop();
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void release() {
        mediaPlayer.release();
    }

    public int getDuration() {
       return mediaPlayer.getDuration();
    }

    public void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    public void createMediaPlayer(int positionInner) {
        position = positionInner;
        uri = Uri.parse(musicFiles.get(position).getPath());
        SharedPreferences.Editor editor = getSharedPreferences(MUSIC_FILE_LAST_PLAYED, MODE_PRIVATE).edit();
        editor.putString(MUSIC_FILE, uri.toString());

        editor.putString(ARTIST_NAME, musicFiles.get(position).getArtist());

        editor.putString(SONG_NAME, musicFiles.get(position).getTitle());
        editor.apply();

        try {
            mediaPlayer = MediaPlayer.create(getBaseContext(), Uri.fromFile(new File(uri.toString())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public void getCallBack(ActionPlaying actionPlaying) {
        this.actionPlaying = actionPlaying;
    }

    void playPauseButtonClicked() {
        if (actionPlaying != null) {
            actionPlaying.playPauseBtnClicked();
        }
    }

    void nextButtonClicked() {
        if (actionPlaying != null) {
            actionPlaying.nextBtnClicked();
        }
    }

    private void previousButtonClicked() {
        if (actionPlaying != null) {
            actionPlaying.prevBtnClicked();
        }
    }
}
