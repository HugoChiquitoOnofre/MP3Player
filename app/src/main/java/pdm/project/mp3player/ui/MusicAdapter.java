package pdm.project.mp3player.ui;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;

import java.util.ArrayList;

import pdm.project.mp3player.PlayerActivity;
import pdm.project.mp3player.R;
import pdm.project.mp3player.model.MusicFiles;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<MusicFiles> musicFiles;

    public MusicAdapter(Context mContext, ArrayList<MusicFiles> musicFiles) {
        this.mContext = mContext;
        this.musicFiles = musicFiles;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.fileName.setText(musicFiles.get(position).getTitle());
        byte[] image = getAlbumArt(musicFiles.get(position).getPath());

        if (image != null) {
            Glide.with(mContext).asBitmap()
                    .load(image)
                    .into(holder.albumArt);
        }
        else {
            Glide.with(mContext).asBitmap()
                    .load(R.drawable.avatar_256_725 )
                    .into(holder.albumArt);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PlayerActivity.class);
                intent.putExtra("position", holder.getAdapterPosition()); // check later
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return musicFiles.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView fileName;
            ImageView albumArt;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                fileName = itemView.findViewById(R.id.musicFileName);
                albumArt = itemView.findViewById(R.id.musicImage);
            }
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
