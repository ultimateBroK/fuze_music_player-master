package com.android.fuze_music_player.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.fuze_music_player.R;
import com.android.fuze_music_player.activity.AlbumDetailActivity;
import com.android.fuze_music_player.adapter.AlbumAdapter;
import com.android.fuze_music_player.databinding.FragmentAlbumsBinding;
import com.android.fuze_music_player.model.SongModel;
import com.android.fuze_music_player.service.AlbumService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AlbumsFragment extends Fragment {

    private FragmentAlbumsBinding binding;
    private AlbumAdapter albumAdapter;
    private ArrayList<SongModel> songModels;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Nhận dữ liệu songModels từ arguments nếu có
        if (getArguments() != null) {
            songModels = (ArrayList<SongModel>) getArguments().getSerializable("songs"); // Sử dụng Serializable
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout và binding cho fragment
        binding = FragmentAlbumsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Thiết lập RecyclerView
        binding.albumsList.setHasFixedSize(true);
        binding.albumsList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Loại bỏ các album trùng lặp và sắp xếp danh sách album theo thứ tự chữ cái
        ArrayList<AlbumAdapter.Album> uniqueAlbumModels = AlbumService.getUniqueAlbums(songModels);
        Collections.sort(uniqueAlbumModels, Comparator.comparing(AlbumAdapter.Album::getName));

        if (uniqueAlbumModels != null && !uniqueAlbumModels.isEmpty()) {
            albumAdapter = new AlbumAdapter(getContext(), uniqueAlbumModels, album -> {
                // Mở Activity chi tiết album khi một album được chọn
                Intent intent = new Intent(getActivity(), AlbumDetailActivity.class);
                intent.putExtra("album", album.getName());
                startActivity(intent);
            });
            binding.albumsList.setAdapter(albumAdapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cập nhật tiêu đề của tiêu đề chính
        TextView title = getActivity().findViewById(R.id.title);
        title.setText("Albums");

        // Hiển thị tiêu đề
        getActivity().findViewById(R.id.header_layout).setVisibility(View.VISIBLE);
    }


    @Override
    public void onStop() {
        super.onStop();
        // Ẩn tiêu đề khi fragment không còn hiển thị
        getActivity().findViewById(R.id.header_layout).setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
