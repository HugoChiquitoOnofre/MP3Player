package pdm.project.mp3player.ui.search;

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

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MySearchHolder> {

    private Context mContext;
    public static ArrayList<MusicFiles> searchFiles;
    View view;

    public SearchAdapter(Context mContext, ArrayList<MusicFiles> searchFiles) {
        this.mContext = mContext;
        this.searchFiles = searchFiles;
    }

    @NonNull
    @Override
    public MySearchHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false);
        return new MySearchHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MySearchHolder holder, int position) {
        holder.musicFileName.setText(searchFiles.get(position).getTitle());
        byte[] image = getAlbumArt(searchFiles.get(position).getPath());

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
                intent.putExtra("sender", "Search");
                intent.putExtra("position", holder.getAdapterPosition());

                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return searchFiles.size();
    }

    public class MySearchHolder extends RecyclerView.ViewHolder {
        TextView musicFileName;
        ImageView musicImage;

        public MySearchHolder(@NonNull View itemView) {
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
