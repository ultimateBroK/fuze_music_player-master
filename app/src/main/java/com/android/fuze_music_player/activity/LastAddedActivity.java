package com.android.fuze_music_player.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.fuze_music_player.R;
import com.android.fuze_music_player.adapter.LastAddedAdapter;
import com.android.fuze_music_player.databinding.ActivityLastAddedBinding;
import com.android.fuze_music_player.model.SongModel;

import java.util.ArrayList;

public class LastAddedActivity extends AppCompatActivity {

    private ActivityLastAddedBinding binding;
    private ArrayList<SongModel> lastAddedList;
    private LastAddedAdapter lastAddedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sử dụng ViewBinding để thiết lập layout
        binding = ActivityLastAddedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo RecyclerView
        binding.lastAddedRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Tải dữ liệu các bài hát mới thêm
        loadLastAddedData();

        // Thiết lập adapter với listener nếu lastAddedList không rỗng
        if (lastAddedList != null && !lastAddedList.isEmpty()) {
            lastAddedAdapter = new LastAddedAdapter(this, lastAddedList, position -> {
                // Xử lý sự kiện khi một bài hát được chọn
                Intent intent = new Intent(LastAddedActivity.this, PlayerActivity.class);
                intent.putExtra("songs", lastAddedList); // Sử dụng Serializable thay vì Parcelable
                intent.putExtra("position", position);
                intent.putExtra("source", "last_added");
                startActivity(intent);
            });
            binding.lastAddedRecyclerView.setAdapter(lastAddedAdapter);
        } else {
            // Xử lý trường hợp lastAddedList rỗng hoặc null
            // Tùy chọn, hiển thị thông báo cho người dùng
        }

        // Xử lý sự kiện nút quay lại
        binding.backLastAdded.setOnClickListener(view -> finish());
    }

    // Tải dữ liệu các bài hát mới thêm
    private void loadLastAddedData() {
        lastAddedList = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
        };
        String sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC";
        Cursor cursor = contentResolver.query(uri, projection, null, null, sortOrder);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                int titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int albumIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int durationIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                int dataIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

                if (idIndex != -1 && titleIndex != -1 && artistIndex != -1 && albumIndex != -1 && durationIndex != -1 && dataIndex != -1) {
                    long id = cursor.getLong(idIndex);
                    String title = cursor.getString(titleIndex);
                    String artist = cursor.getString(artistIndex);
                    String album = cursor.getString(albumIndex);
                    String duration = cursor.getString(durationIndex);
                    String path = cursor.getString(dataIndex);

                    SongModel song = new SongModel();
                    song.setId(id);
                    song.setTitle(title);
                    song.setArtist(artist);
                    song.setAlbum(album);
                    song.setDuration(Long.parseLong(duration));
                    song.setPath(path);
                    lastAddedList.add(song);
                }
            } while (cursor.moveToNext());

            cursor.close();
        }

        Log.d("LastAddedActivity", "lastAddedList loaded with " + lastAddedList.size() + " items");
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
