package pdm.project.mp3player.provider;

import static pdm.project.mp3player.MainActivity.albums;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;


import java.util.ArrayList;

import pdm.project.mp3player.model.MusicFiles;

public class MusicStore {
    private static ArrayList<MusicFiles> audioList;
    private static Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private static ArrayList<String> duplicated = new ArrayList<>();

    public static ArrayList<MusicFiles> getAllAudio(Context context) {
        audioList = new ArrayList<>();
        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID
        };
        Cursor cursor = context.getContentResolver().query(uri, projection,
                null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String album = cursor.getString(0);
                String title = cursor.getString(1);
                String duration = cursor.getString(2);
                String path = cursor.getString(3);
                String artist = cursor.getString(4);
                String id = cursor.getString(5);

                MusicFiles musicFiles = new MusicFiles(path, title, artist, album, duration, id);
                Log.e("Path:" + path, "Album" + album);
                audioList.add(musicFiles);

                if (!duplicated.contains(album)) {
                    albums.add(musicFiles);
                    duplicated.add(album);
                }
            }
            cursor.close();
        }
        return audioList;
    }


}
