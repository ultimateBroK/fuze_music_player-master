package com.android.fuze_music_player.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.fuze_music_player.R;
import com.android.fuze_music_player.activity.HistoryActivity;
import com.android.fuze_music_player.activity.LastAddedActivity;
import com.android.fuze_music_player.activity.PlayerActivity;
import com.android.fuze_music_player.databinding.FragmentForYouBinding;
import com.android.fuze_music_player.model.SongModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;

public class ForYouFragment extends Fragment {

    private FragmentForYouBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout và binding cho fragment
        binding = FragmentForYouBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Thiết lập click listener cho các nút
        binding.lastAddedButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LastAddedActivity.class);
            startActivity(intent);
        });

        binding.historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), HistoryActivity.class);
            startActivity(intent);
        });

        binding.shuffleButton.setOnClickListener(v -> {
            playShuffledMusic();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cập nhật tiêu đề của tiêu đề chính
        TextView title = getActivity().findViewById(R.id.title);
        title.setText("For You");

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

    // Hàm phát nhạc ngẫu nhiên
    private void playShuffledMusic() {
        // Lấy danh sách các bài hát từ SharedPreferences hoặc từ bất kỳ nơi nào bạn lưu trữ chúng
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("music_player", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("song_list", null);
        ArrayList<SongModel> songList = gson.fromJson(json, new TypeToken<ArrayList<SongModel>>() {
        }.getType());

        if (songList != null && !songList.isEmpty()) {
            Collections.shuffle(songList);
            Intent intent = new Intent(getActivity(), PlayerActivity.class);
            intent.putExtra("songs", songList); // Sử dụng Serializable thay vì Parcelable
            intent.putExtra("position", 0);
            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), "No songs available to shuffle", Toast.LENGTH_SHORT).show();
        }
    }
}
