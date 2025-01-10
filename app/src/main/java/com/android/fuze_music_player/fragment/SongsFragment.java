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
import com.android.fuze_music_player.activity.PlayerActivity;
import com.android.fuze_music_player.adapter.SongAdapter;
import com.android.fuze_music_player.databinding.FragmentSongsBinding;
import com.android.fuze_music_player.model.SongModel;

import java.util.ArrayList;

public class SongsFragment extends Fragment {

    private FragmentSongsBinding binding;
    private SongAdapter songAdapter;
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
        binding = FragmentSongsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() != null) {
            // Cập nhật tiêu đề của tiêu đề chính
            TextView title = getActivity().findViewById(R.id.title);
            title.setText("Songs");
            getActivity().findViewById(R.id.header_layout).setVisibility(View.VISIBLE);
        }

        // Cấu hình RecyclerView
        binding.songsList.setHasFixedSize(true);
        songAdapter = new SongAdapter(getContext(), songModels, position -> {
            // Mở Activity phát nhạc khi một bài hát được chọn
            Intent intent = new Intent(getContext(), PlayerActivity.class);
            intent.putExtra("songs", songModels); // Sử dụng Serializable thay vì Parcelable
            intent.putExtra("position", position);
            startActivity(intent);
        });
        binding.songsList.setAdapter(songAdapter);
        binding.songsList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.songsList.setItemViewCacheSize(20); // Tùy chọn tối ưu hóa
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            // Cập nhật tiêu đề của tiêu đề chính
            TextView title = getActivity().findViewById(R.id.title);
            title.setText("Songs");
            getActivity().findViewById(R.id.header_layout).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getActivity() != null) {
            // Ẩn tiêu đề khi fragment không còn hiển thị
            getActivity().findViewById(R.id.header_layout).setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
