package com.android.fuze_music_player.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.fuze_music_player.R;
import com.android.fuze_music_player.adapter.HistoryAdapter;
import com.android.fuze_music_player.databinding.ActivityHistoryBinding;
import com.android.fuze_music_player.model.SongModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private ActivityHistoryBinding binding;
    private ArrayList<SongModel> historyList;
    private HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sử dụng ViewBinding để thiết lập layout
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo RecyclerView
        binding.historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Tải dữ liệu lịch sử
        loadHistoryData();

        // Thiết lập adapter với listener
        historyAdapter = new HistoryAdapter(this, historyList, position -> {
            // Xử lý sự kiện khi một bài hát trong lịch sử được chọn
            Intent intent = new Intent(HistoryActivity.this, PlayerActivity.class);
            intent.putExtra("songs", historyList); // Sử dụng Serializable thay vì Parcelable
            intent.putExtra("position", position);
            intent.putExtra("source", "history");
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up_in, R.anim.no_animation); // Thêm hoạt ảnh
        });

        binding.historyRecyclerView.setAdapter(historyAdapter);

        // Xử lý sự kiện nút quay lại
        binding.backHistory.setOnClickListener(view -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại dữ liệu lịch sử khi Activity được tiếp tục
        loadHistoryData();
        historyAdapter.updateHistoryList(historyList);
    }

    // Tải dữ liệu lịch sử từ SharedPreferences
    private void loadHistoryData() {
        SharedPreferences sharedPreferences = getSharedPreferences("music_player", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("history", null);
        Type type = new TypeToken<ArrayList<SongModel>>() {
        }.getType();
        historyList = gson.fromJson(json, type);

        // Nếu lịch sử rỗng hoặc null, khởi tạo danh sách mới
        if (historyList == null) {
            historyList = new ArrayList<>();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null; // Đảm bảo ViewBinding được dọn dẹp đúng cách
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.slide_up_in, R.anim.no_animation); // Thêm hoạt ảnh
    }
}
