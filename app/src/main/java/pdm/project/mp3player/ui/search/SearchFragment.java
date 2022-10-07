package pdm.project.mp3player.ui.search;

import static pdm.project.mp3player.MainActivity.listSearchSongs;
import static pdm.project.mp3player.MainActivity.musicFiles;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

import pdm.project.mp3player.R;
import pdm.project.mp3player.databinding.FragmentSearchBinding;
import pdm.project.mp3player.model.MusicFiles;

public class SearchFragment extends Fragment implements SearchView.OnQueryTextListener {

    private FragmentSearchBinding binding;
    RecyclerView recyclerView;
    SearchView searchView;
    SearchAdapter searchAdapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SearchViewModel searchViewModel =
                new ViewModelProvider(this).get(SearchViewModel.class);

        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        searchView = root.findViewById(R.id.searchText);
        searchView.setOnQueryTextListener(this);

        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String userInput = newText.toLowerCase(Locale.ROOT);
        listSearchSongs = new ArrayList<>();
        Log.e("search", userInput);
        for (MusicFiles song: musicFiles) {
            if ((!userInput.isEmpty()) && (song.getTitle().toLowerCase().contains(userInput) || song.getArtist().toLowerCase(Locale.ROOT).contains(userInput))) {
                listSearchSongs.add(song);
            }
        }

        Log.e("SearchFragment: ", "Listando canciones encontradas...");
        searchAdapter = new SearchAdapter(getContext(), listSearchSongs);
        Log.e("SearchFragment:", "MusicAdapter creado");
        recyclerView.setAdapter(searchAdapter);
        Log.e("SearchFragment:" ,"Adaptador establecido");
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        return true;
    }
}