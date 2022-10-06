package pdm.project.mp3player.ui.home;

import static pdm.project.mp3player.MainActivity.musicFiles;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import pdm.project.mp3player.R;
import pdm.project.mp3player.model.MusicFiles;

public class AlbumDetails extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView albumPhoto;
    String albumName;
    ArrayList<MusicFiles> albumSongs = new ArrayList<>();
    AlbumDetailAdapter albumDetailAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_details);
        recyclerView = findViewById(R.id.recyclerViewAlbum);
        albumPhoto = findViewById(R.id.album_photo);
        albumName = getIntent().getStringExtra("AlbumName");

        int j = 0;

        for (int i = 0; i < musicFiles.size(); i++) {
            if (albumName.equals(musicFiles.get(i).getAlbum())) {
                Log.e("AlbumDetails::onCreate", albumName + "-" + musicFiles.get(i).getAlbum());
                albumSongs.add(j, musicFiles.get(i));
                j++;
            }
        }
        Log.e("AlbumDetails::onCreate", "albumSongs" + "-" + albumSongs.size());
        byte[] image = getAlbumArt(albumSongs.get(0).getPath());
        if (image != null) {
            Glide.with(this).load(image).into(albumPhoto);
        } else {
            Glide.with(this).load(R.drawable.avatar_256_725).into(albumPhoto);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("AlbumDetails::onResume", "albumSongs - " + albumSongs.size());
        if (albumSongs.size() > 0) {
            Log.e("AlbumDetails::onResume", "Mostrando canciones album");
            albumDetailAdapter = new AlbumDetailAdapter(this, albumSongs);
            recyclerView.setAdapter(albumDetailAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        }
    }

    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        Log.e("AlbumAdapter::GetAlbumArt", uri);
        retriever.setDataSource(uri);

        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();

        return  art;
    }
}