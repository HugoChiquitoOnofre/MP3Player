package pdm.project.mp3player;

import static pdm.project.mp3player.MainActivity.listSearchSongs;
import static pdm.project.mp3player.MainActivity.musicFiles;
import static pdm.project.mp3player.MainActivity.repeatBoolean;
import static pdm.project.mp3player.MainActivity.shuffleBoolean;
import static pdm.project.mp3player.ui.player.ApplicationClass.ACTION_NEXT;
import static pdm.project.mp3player.ui.player.ApplicationClass.ACTION_PLAY;
import static pdm.project.mp3player.ui.player.ApplicationClass.ACTION_PREVIOUS;
import static pdm.project.mp3player.ui.player.ApplicationClass.CHANNEL_ID_2;
import static pdm.project.mp3player.ui.home.AlbumDetailAdapter.albumFiles;
import static pdm.project.mp3player.ui.library.LibraryFragment.musicAdapter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.palette.graphics.Palette;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import pdm.project.mp3player.model.MusicFiles;
import pdm.project.mp3player.ui.player.ActionPlaying;
import pdm.project.mp3player.ui.player.MusicService;
import pdm.project.mp3player.ui.player.NotificationReceiver;

public class PlayerActivity<OnResume> extends AppCompatActivity implements ActionPlaying, ServiceConnection {

    TextView songName, artistName, durationPlayed, durationTotal;
    ImageView coverArt, nextButton, previousButton, backButton, shuffleButton, repeatButton, menuOptions;
    FloatingActionButton playPauseButton;
    SeekBar seekBar;
    int position = -1;
    public static ArrayList<MusicFiles> listSongs = new ArrayList<>();
    static Uri uri;
    // static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Thread playThread, nextThread, prevThread;

    //Service
    MusicService musicService;
    MediaSessionCompat mediaSessionCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "My Audio");
        initViews();
        getIntentMethod();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (musicService != null && fromUser) {
                    musicService.seekTo(progress*1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    int mCurrentPosition = musicService.getCurrentPosition()/1000;
                    seekBar.setProgress(mCurrentPosition);
                    durationPlayed.setText(formattedTime(mCurrentPosition));
                }

                handler.postDelayed(this, 1000);
            }
        });

        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shuffleBoolean == true) {
                    shuffleBoolean = false;
                    shuffleButton.setImageResource(R.drawable.ic_shuffle);
                } else {
                    shuffleBoolean = true;
                    shuffleButton.setImageResource(R.drawable.ic_baseline_shuffle_on_24);
                }
            }
        });

        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (repeatBoolean == true) {
                    repeatBoolean = false;
                    repeatButton.setImageResource(R.drawable.ic_repeat);
                }
                else {
                    repeatBoolean = true;
                    repeatButton.setImageResource(R.drawable.ic_repeat_on);
                }
            }
        });

        menuOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(getApplicationContext(), v);
                popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener((item -> {
                    switch (item.getItemId()){
                        case R.id.delete:
                            //Toast.makeText(getApplicationContext(), "Borrando canción", Toast.LENGTH_SHORT).show();
                            deleteFile(position, v);
                            break;
                    }
                    return true;
                }));
            }
        });
    }

    @Override
    protected void onResume() {
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
        playThreadButton();
        nextThreadButton();
        prevThreadButton();
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    private String formattedTime(int mCurrentPosition) {
        String totalOut = "";
        String totalNew = "";
        String seconds = String.valueOf(mCurrentPosition%60);
        String minutes = String.valueOf(mCurrentPosition/60);

        totalOut = minutes + ":" + seconds;
        totalNew = minutes + ":" + "0" + seconds;

        if (seconds.length() == 1){
            return totalNew;
        } else {
            return totalOut;
        }
    }

    private void initViews() {
        songName = findViewById(R.id.song_name);
        artistName = findViewById(R.id.song_artist);
        durationPlayed = findViewById(R.id.durationPlayed);
        durationTotal = findViewById(R.id.durationTotal);

        coverArt = findViewById(R.id.cover_art);
        nextButton = findViewById(R.id.next);
        previousButton = findViewById(R.id.previous);
        backButton = findViewById(R.id.back_btn);
        shuffleButton = findViewById(R.id.shuffle);
        repeatButton = findViewById(R.id.repeat);
        playPauseButton = findViewById(R.id.play_pause);
        seekBar = findViewById(R.id.seekBar);
        menuOptions = findViewById(R.id.options);
    }

    private void getIntentMethod() {
        position = getIntent().getIntExtra("position", -1);
        String sender = getIntent().getStringExtra("sender");

        if ((sender != null) && (sender.equals("AlbumDetails"))) {
            Log.e("PlayerActivity::getIntentMethod", "Reproduciendo album");
            listSongs = albumFiles;
        } else if ((sender != null) && (sender.equals("Search"))){
            Log.e("PlayerActivity::getIntentMethod", "Reproduciendo canciones de la busqueda");
            listSongs = listSearchSongs;
        } else {
            Log.e("PlayerActivity::getIntentMethod", "Reproduciendo todas las canciones");
            listSongs = musicFiles;
        }

        //listSongs = musicFiles;

        if (listSongs != null) {
            playPauseButton.setImageResource(R.drawable.ic_pause);
            uri = Uri.parse(listSongs.get(position).getPath());
        }
        showNotification(R.drawable.ic_pause);
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("servicePosition",position);
        startService(intent);
    }

    private void metadata(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());

        int durationTotalV = Integer.parseInt(listSongs.get(position).getDuration())/1000;
        durationTotal.setText(formattedTime(durationTotalV));

        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;

        if (art != null) {
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            imageAnimation(this, coverArt, bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch = palette.getDominantSwatch();

                    ImageView gradient = findViewById(R.id.imageViewGradient);
                    if (swatch != null) {
                        ConstraintLayout mContainer = findViewById(R.id.mContainer);
                        gradient.setBackgroundResource(R.drawable.gradiant_bg);
                        mContainer.setBackgroundResource(R.drawable.gradiant_bg); // check

                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[] {
                                        swatch.getRgb(),
                                        0x00000000
                                });

                        gradient.setBackground(gradientDrawable);


                        GradientDrawable gradientDrawable2 = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[] {
                                        swatch.getRgb(),
                                        swatch.getRgb()
                                });

                        mContainer.setBackground(gradientDrawable2);
                        songName.setTextColor(swatch.getTitleTextColor());
                        artistName.setTextColor(swatch.getBodyTextColor());
                    } else {
                        RelativeLayout mContainer = findViewById(R.id.mContainer);
                        gradient.setBackgroundResource(R.drawable.gradiant_bg);
                        mContainer.setBackgroundResource(R.drawable.gradiant_bg); // check

                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[] {
                                        0xff000000,
                                        0x00000000
                                });

                        gradient.setBackground(gradientDrawable);

                        GradientDrawable gradientDrawable2 = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[] {
                                        0xff000000,
                                        0000000000
                                });

                        mContainer.setBackground(gradientDrawable2);
                        songName.setTextColor(Color.WHITE);
                        artistName.setTextColor(Color.DKGRAY);
                    }
                }
            });
        } else {
            Glide.with(this).asBitmap().load(R.drawable.avatar_256_725).into(coverArt);

        }
    }

    public void imageAnimation(Context context, ImageView imageView, Bitmap bitmap){
        Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);

        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(animIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animOut);
    }

    private void prevThreadButton() {
        prevThread = new Thread() {
            @Override
            public void run() {
                super.run();
                previousButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prevBtnClicked();
                    }
                });
            }
        };
        prevThread.start();
    }

    public void prevBtnClicked() {
        if (musicService.isPlaying()) {
            musicService.stop();
            musicService.release();

            position = ((position-1)<0 ? (listSongs.size()-1) : (position-1));
            uri = Uri.parse(listSongs.get(position).getPath());
            try {
                // mediaPlayer = MediaPlayer.create(getApplicationContext(),Uri.fromFile(new File(uri.toString())));
                musicService.createMediaPlayer(position);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metadata(uri);
            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getTitle());

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition()/1000;
                        seekBar.setMax(musicService.getDuration()/1000); // ????
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            //mediaPlayer.setOnCompletionListener(this);
            musicService.onComplete();
            showNotification(R.drawable.ic_pause);
            playPauseButton.setBackgroundResource(R.drawable.ic_pause);
            musicService.start();
        } else {
            musicService.stop();
            musicService.release();

            position = ((position-1)<0 ? (listSongs.size()-1) : (position-1));
            uri = Uri.parse(listSongs.get(position).getPath());
            // mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            musicService.createMediaPlayer(position);
            metadata(uri);
            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getTitle());

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            //mediaPlayer.setOnCompletionListener(this);
            musicService.onComplete();
            showNotification(R.drawable.ic_baseline_play_arrow_24);
            playPauseButton.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
        }
    }

    private void nextThreadButton() {
        nextThread = new Thread() {
            @Override
            public void run() {
                super.run();
                nextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextBtnClicked();
                    }
                });
            }
        };
        nextThread.start();
    }

    public void nextBtnClicked() {
        if (musicService.isPlaying()) {
            musicService.stop();
            musicService.release();
            if (shuffleBoolean && !(repeatBoolean)) {
                position = getRandom(listSongs.size()-1);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position+1)%listSongs.size());
            }

            uri = Uri.parse(listSongs.get(position).getPath());
            try {
                musicService.createMediaPlayer(position);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metadata(uri);
            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getTitle());

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition()/1000;
                        seekBar.setMax(musicService.getDuration()/1000); // ????
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            //mediaPlayer.setOnCompletionListener(this);
            musicService.onComplete();
            showNotification(R.drawable.ic_pause);
            playPauseButton.setBackgroundResource(R.drawable.ic_pause);
            musicService.start();
        } else {
            musicService.stop();
            musicService.release();

            if (shuffleBoolean && !repeatBoolean) {
                position = getRandom(listSongs.size()-1);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position+1)%listSongs.size());
            }

            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metadata(uri);
            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getTitle());

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            //mediaPlayer.setOnCompletionListener(this);
            musicService.onComplete();
            showNotification(R.drawable.ic_baseline_play_arrow_24);
            playPauseButton.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
        }
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i+1);
    }

    private void playThreadButton() {
        playThread = new Thread() {
            @Override
            public void run() {
                super.run();
                playPauseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPauseBtnClicked();
                    }
                });
            }
        };
        playThread.start();
    }

    public void playPauseBtnClicked() {
        if (musicService.isPlaying()) {
            playPauseButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            showNotification(R.drawable.ic_baseline_play_arrow_24);
            musicService.pause();
            seekBar.setMax(musicService.getDuration()/1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });

        } else {
            playPauseButton.setImageResource(R.drawable.ic_pause);
            showNotification(R.drawable.ic_pause);
            musicService.start();
            seekBar.setMax(musicService.getDuration()/1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }

                    handler.postDelayed(this, 1000);
                }
            });
        }
    }

    private void deleteFile(int position, View view){
        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(musicFiles.get(position).getId()));
        File file = new File(musicFiles.get(position).getPath());
        //Log.e("PlayerActivity::delete: ", "Eliminando: " + musicFiles.get(position).getPath());

        nextBtnClicked();
        boolean deleted = file.delete();

        if (deleted == true) {
            musicFiles.remove(position);
            getApplicationContext().getContentResolver().delete(uri, null, null);
            musicAdapter.notifyItemRemoved(position);
            musicAdapter.notifyItemChanged(position, musicFiles.size());
            Snackbar.make(view, "Canción eliminada", Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(view, "No se pudo eliminar", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
        musicService = myBinder.getService();
        musicService.getCallBack(this);

        //Toast.makeText(this, "Conectado" + musicService,Toast.LENGTH_SHORT).show();

        seekBar.setMax(musicService.getDuration()/1000);
        metadata(uri);
        songName.setText(listSongs.get(position).getTitle());
        artistName.setText(listSongs.get(position).getArtist());
        musicService.onComplete();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
    }

    void showNotification(int playPauseButton){
        Log.e("showNotification", "method");
        Intent intent = new Intent(this, PlayerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0, intent, 0);

        Intent prevIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent.getBroadcast(this,0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent.getBroadcast(this,0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent.getBroadcast(this,0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        byte[] picture = null;
        picture = getAlbumArt(listSongs.get(position).getPath());
        Bitmap thumb = null;

        if (picture != null) {
            thumb = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        } else {
            thumb = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_256_725);
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getBaseContext(), CHANNEL_ID_2)
                .setSmallIcon(playPauseButton).setLargeIcon(thumb).
                setContentTitle(listSongs.get(position).getTitle()).
                setContentText(listSongs.get(position).getArtist())
                .addAction(R.drawable.ic_skip_previous, "previous", prevPending)
                .addAction(playPauseButton, "pause", pausePending)
                .addAction(R.drawable.ic_skip_next, "next", nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification.build());
    }

    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        Log.e("GetAlbumArt", uri);
        retriever.setDataSource(uri);

        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();

        return  art;
    }
}