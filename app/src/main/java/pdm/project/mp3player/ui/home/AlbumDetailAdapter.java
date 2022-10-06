package pdm.project.mp3player.ui.home;

import android.content.Context;
import android.content.Intent;
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

public class AlbumDetailAdapter extends RecyclerView.Adapter<AlbumDetailAdapter.MyHolder> {
    private Context mContext;
    public static ArrayList<MusicFiles> albumFiles;
    View view;

    public AlbumDetailAdapter(Context mContext, ArrayList<MusicFiles> albumFiles) {
        this.mContext = mContext;
        this.albumFiles = albumFiles;
    }

    @NonNull
    @Override
    public AlbumDetailAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumDetailAdapter.MyHolder holder, int position) {
        holder.musicFileName.setText(albumFiles.get(position).getTitle());
        byte[] image = getAlbumArt(albumFiles.get(position).getPath());

        if (image != null) {
            Glide.with(mContext).asBitmap()
                    .load(image)
                    .into(holder.musicImage);
        } else {
            Glide.with(mContext).asBitmap()
                    .load(R.drawable.avatar_256_725)
                    .into(holder.musicImage);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PlayerActivity.class);
                intent.putExtra("sender", "AlbumDetails");
                intent.putExtra("position", holder.getAdapterPosition());

                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumFiles.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        TextView musicFileName;
        ImageView musicImage;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            musicFileName = itemView.findViewById(R.id.musicFileName);
            musicImage = itemView.findViewById(R.id.musicImage);
        }
    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        Log.e("AlbumDetailAdapter::GetAlbumArt", uri);
        retriever.setDataSource(uri);

        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();

        return art;
    }
}
