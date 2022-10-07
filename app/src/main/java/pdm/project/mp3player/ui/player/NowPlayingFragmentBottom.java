package pdm.project.mp3player.ui.player;

import static android.content.Context.MODE_PRIVATE;
import static pdm.project.mp3player.MainActivity.ARTIST_NAME;
import static pdm.project.mp3player.MainActivity.ARTIST_TO_FRAG;
import static pdm.project.mp3player.MainActivity.PATH_TO_FRAG;
import static pdm.project.mp3player.MainActivity.SHOW_MINI_PLAYER;
import static pdm.project.mp3player.MainActivity.SONG_NAME;
import static pdm.project.mp3player.MainActivity.SONG_NAME_TO_FRAG;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import pdm.project.mp3player.R;

public class NowPlayingFragmentBottom extends Fragment implements ServiceConnection {

    ImageView nextButton, albumArt;
    TextView artistName, songName;
    FloatingActionButton playPauseButton;
    View view;
    MusicService musicService;
    public static String MUSIC_FILE_LAST_PLAYED = "LAST_PLAYED";
    public static String MUSIC_FILE = "STORED_MUSIC";
    public static String ARTIST_NAME = "ARTIST_NAME";
    public static String SONG_NAME = "SONG_NAME";

    public  NowPlayingFragmentBottom () {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_now_playing_bottom, container, false);

        artistName = view.findViewById(R.id.song_artist_mini_player);
        songName = view.findViewById(R.id.song_name_mini_player);
        albumArt = view.findViewById(R.id.bottom_album_art);
        nextButton = view.findViewById(R.id.skip_next_bottom);
        playPauseButton = view.findViewById(R.id.play_pause_mini_player);

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "play pause", Toast.LENGTH_SHORT).show();
                if (musicService != null) {
                    musicService.playPauseButtonClicked();
                    if (musicService.isPlaying()) {
                        playPauseButton.setImageResource(R.drawable.ic_pause);
                    } else {
                        playPauseButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                    }
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "next", Toast.LENGTH_SHORT).show();
                if (musicService != null) {
                    musicService.nextButtonClicked();
                    if (getActivity() != null) {
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences(MUSIC_FILE_LAST_PLAYED, MODE_PRIVATE).edit();
                        editor.putString(MUSIC_FILE, musicService.musicFiles.get(musicService.position).getPath());
                        editor.putString(ARTIST_NAME, musicService.musicFiles.get(musicService.position).getArtist());
                        editor.putString(SONG_NAME, musicService.musicFiles.get(musicService.position).getTitle());
                        editor.apply();

                        SharedPreferences preferences = getActivity().getSharedPreferences(MUSIC_FILE_LAST_PLAYED, MODE_PRIVATE);
                        String path = preferences.getString(MUSIC_FILE, null);
                        String artistName2 = preferences.getString(ARTIST_NAME, null);
                        String songName2 = preferences.getString(SONG_NAME, null);

                        if (path != null) {
                            SHOW_MINI_PLAYER = true;
                            PATH_TO_FRAG = path;
                            ARTIST_TO_FRAG = artistName2;
                            SONG_NAME_TO_FRAG = songName2;
                        } else {
                            SHOW_MINI_PLAYER = false;
                            PATH_TO_FRAG = null;
                            ARTIST_TO_FRAG = null;
                            SONG_NAME_TO_FRAG = null;
                        }

                        if (SHOW_MINI_PLAYER) {
                            if (PATH_TO_FRAG != null) {
                                byte[] art = getAlbumArt(PATH_TO_FRAG);

                                if (art != null) {
                                    Glide.with(getContext()).load(art).into(albumArt);
                                } else {
                                    Glide.with(getContext()).load(R.drawable.avatar_256_725).into(albumArt);
                                }

                                songName.setText(SONG_NAME_TO_FRAG);
                                artistName.setText(ARTIST_TO_FRAG);

                            }
                        }
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SHOW_MINI_PLAYER) {
            if (PATH_TO_FRAG != null) {
                byte[] art = getAlbumArt(PATH_TO_FRAG);

                if (art != null) {
                    Glide.with(getContext()).load(art).into(albumArt);
                } else {
                    Glide.with(getContext()).load(R.drawable.avatar_256_725).into(albumArt);
                }

                songName.setText(SONG_NAME_TO_FRAG);
                artistName.setText(ARTIST_TO_FRAG);

                Intent intent = new Intent(getContext(), MusicService.class);

                if (getContext() != null) {
                    getContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (getContext() != null) {
            getContext().unbindService(this);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder binder = (MusicService.MyBinder) service;
        musicService = binder.getService();

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
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