package com.android.fuze_music_player.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.fuze_music_player.adapter.QueueAdapter;
import com.android.fuze_music_player.databinding.ActivityQueueBinding;
import com.android.fuze_music_player.model.SongModel;

import java.util.ArrayList;

public class QueueActivity extends AppCompatActivity {

    private ActivityQueueBinding binding;
    private ArrayList<SongModel> queueList;
    private QueueAdapter queueAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sử dụng ViewBinding để thiết lập layout
        binding = ActivityQueueBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo RecyclerView
        binding.queueRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        queueList = (ArrayList<SongModel>) intent.getSerializableExtra("queueList");
        int currentSongPosition = intent.getIntExtra("currentSongPosition", -1); // Nhận vị trí bài hát hiện tại

        // Thiết lập adapter với listener nếu queueList không rỗng
        if (queueList != null && !queueList.isEmpty()) {
            queueAdapter = new QueueAdapter(this, queueList, currentSongPosition, position -> {
                // Khi một bài hát trong danh sách hàng đợi được chọn
                Intent resultIntent = new Intent();
                resultIntent.putExtra("newSongPosition", position);
                setResult(RESULT_OK, resultIntent);
                finish();
            });
            binding.queueRecyclerView.setAdapter(queueAdapter);
        } else {
            // Xử lý trường hợp queueList rỗng hoặc null
            // Tùy chọn, hiển thị thông báo cho người dùng
        }

        // Xử lý nút quay lại
        binding.backQueue.setOnClickListener(view -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null; // Đảm bảo ViewBinding được dọn dẹp đúng cách
    }
}
