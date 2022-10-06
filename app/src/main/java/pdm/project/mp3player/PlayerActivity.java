package pdm.project.mp3player;

import static pdm.project.mp3player.MainActivity.musicFiles;
import static pdm.project.mp3player.MainActivity.repeatBoolean;
import static pdm.project.mp3player.MainActivity.shuffleBoolean;
import static pdm.project.mp3player.ui.home.AlbumDetailAdapter.albumFiles;
import static pdm.project.mp3player.ui.library.LibraryFragment.musicAdapter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.palette.graphics.Palette;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
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

import kotlinx.coroutines.Delay;
import pdm.project.mp3player.model.MusicFiles;
import pdm.project.mp3player.ui.MusicAdapter;

public class PlayerActivity<OnResume> extends AppCompatActivity implements MediaPlayer.OnCompletionListener{

    TextView songName, artistName, durationPlayed, durationTotal;
    ImageView coverArt, nextButton, previousButton, backButton, shuffleButton, repeatButton, menuOptions;
    FloatingActionButton playPauseButton;
    SeekBar seekBar;
    int position = -1;
    static ArrayList<MusicFiles> listSongs = new ArrayList<>();
    static Uri uri;
    static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Thread playThread, nextThread, prevThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        initViews();
        getIntentMethod();

        songName.setText(listSongs.get(position).getTitle());
        artistName.setText(listSongs.get(position).getArtist());
        mediaPlayer.setOnCompletionListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress*1000);
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
                if (mediaPlayer != null) {
                    int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
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
                            Toast.makeText(getApplicationContext(), "Borrando canción", Toast.LENGTH_SHORT).show();
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
        playThreadButton();
        nextThreadButton();
        prevThreadButton();
        super.onResume();

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
        } else {
            Log.e("PlayerActivity::getIntentMethod", "Reproduciendo todas las canciones");
            listSongs = musicFiles;
        }

        //listSongs = musicFiles;

        if (listSongs != null) {
            playPauseButton.setImageResource(R.drawable.ic_pause);

            uri = Uri.parse(listSongs.get(position).getPath());
        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            // Log.e("getIntentMethod", uri.toString());
            try {
                mediaPlayer = MediaPlayer.create(getApplicationContext(),Uri.fromFile(new File(uri.toString())));
                mediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            // Log.e("getIntentMethod", uri.toString());
            try {
                mediaPlayer = MediaPlayer.create(getApplicationContext(),Uri.fromFile(new File(uri.toString())));
                mediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        seekBar.setMax(mediaPlayer.getDuration()/1000);
        metadata(uri);
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

    private void prevBtnClicked() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();

            position = ((position-1)<0 ? (listSongs.size()-1) : (position-1));
            uri = Uri.parse(listSongs.get(position).getPath());
            try {
                mediaPlayer = MediaPlayer.create(getApplicationContext(),Uri.fromFile(new File(uri.toString())));

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
                    if (mediaPlayer != null) {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setMax(mediaPlayer.getDuration()/1000); // ????
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            playPauseButton.setBackgroundResource(R.drawable.ic_pause);
            mediaPlayer.start();
        } else {
            mediaPlayer.stop();
            mediaPlayer.release();

            position = ((position-1)<0 ? (listSongs.size()-1) : (position-1));
            uri = Uri.parse(listSongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metadata(uri);
            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getTitle());

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
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

    private void nextBtnClicked() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (shuffleBoolean && !(repeatBoolean)) {
                position = getRandom(listSongs.size()-1);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position+1)%listSongs.size());
            }

            uri = Uri.parse(listSongs.get(position).getPath());
            try {
                mediaPlayer = MediaPlayer.create(getApplicationContext(),Uri.fromFile(new File(uri.toString())));
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
                    if (mediaPlayer != null) {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setMax(mediaPlayer.getDuration()/1000); // ????
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            playPauseButton.setBackgroundResource(R.drawable.ic_pause);
            mediaPlayer.start();
        } else {
            mediaPlayer.stop();
            mediaPlayer.release();

            if (shuffleBoolean && !repeatBoolean) {
                position = getRandom(listSongs.size()-1);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position+1)%listSongs.size());
            }

            uri = Uri.parse(listSongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metadata(uri);
            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getTitle());

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
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

    private void playPauseBtnClicked() {
        if (mediaPlayer.isPlaying()) {
            playPauseButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            mediaPlayer.pause();
            seekBar.setMax(mediaPlayer.getDuration()/1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });

        } else {
            playPauseButton.setImageResource(R.drawable.ic_pause);
            mediaPlayer.start();
            seekBar.setMax(mediaPlayer.getDuration()/1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
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
    public void onCompletion(MediaPlayer mp) {
        nextBtnClicked();
        if (mediaPlayer != null) {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(this);
        }
    }
}