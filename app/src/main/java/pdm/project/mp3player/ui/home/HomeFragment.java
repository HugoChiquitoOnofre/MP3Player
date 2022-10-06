package pdm.project.mp3player.ui.home;

import static pdm.project.mp3player.MainActivity.albums;
import static pdm.project.mp3player.MainActivity.musicFiles;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pdm.project.mp3player.R;
import pdm.project.mp3player.databinding.FragmentHomeBinding;
import pdm.project.mp3player.ui.MusicAdapter;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    RecyclerView recyclerView;
    public static AlbumAdapter albumAdapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = root.findViewById(R.id.recyclerView2);
        recyclerView.setHasFixedSize(true);

        if (!(albums.size() < 1)) {
            Log.e("HomeFragment: ", "Listando albumes...");
            albumAdapter = new AlbumAdapter(getContext(), albums);
            Log.e("HomeFragment:", "AlbumAdapter creado");
            recyclerView.setAdapter(albumAdapter);
            Log.e("HomeFragment:" ,"Adaptador establecido");
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        }
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}