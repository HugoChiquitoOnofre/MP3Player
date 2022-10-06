package pdm.project.mp3player.ui.library;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pdm.project.mp3player.R;
import pdm.project.mp3player.databinding.FragmentLibraryBinding;
import pdm.project.mp3player.ui.MusicAdapter;

public class LibraryFragment extends Fragment {

    private FragmentLibraryBinding binding;
    RecyclerView recyclerView;
    public static MusicAdapter musicAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
            LibraryViewModel libraryViewModel =
                new ViewModelProvider(this).get(LibraryViewModel.class);

        binding = FragmentLibraryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // View view = inflater.inflate(R.layout.fragment_library, container, false);
        // recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        if (!(musicFiles.size() < 1)) {

            Log.e("LibraryFragment: ", "Listando canciones...");
            musicAdapter = new MusicAdapter(getContext(), musicFiles);
            Log.e("LibraryFragment:", "MusicAdapter creado");
            recyclerView.setAdapter(musicAdapter);
            Log.e("LibraryFragment:" ,"Adaptador establecido");
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        }
        // final TextView textView = binding.textNotifications;
        // libraryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}