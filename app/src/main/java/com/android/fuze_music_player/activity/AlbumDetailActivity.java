package com.android.fuze_music_player.activity;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.android.fuze_music_player.R;
import com.android.fuze_music_player.adapter.SongAdapter;
import com.android.fuze_music_player.databinding.ActivityAlbumDetailBinding;
import com.android.fuze_music_player.model.SongModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class AlbumDetailActivity extends AppCompatActivity {

    private ActivityAlbumDetailBinding binding;
    private SongAdapter songAdapter;
    private ArrayList<SongModel> albumSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sử dụng ViewBinding để thiết lập layout
        binding = ActivityAlbumDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Lấy tên album từ Intent
        String albumName = getIntent().getStringExtra("album");
        if (albumName != null) {
            binding.albumName.setText(albumName);

            // Lấy danh sách các bài hát trong album
            albumSongs = getAlbumSongs(albumName);
            if (!albumSongs.isEmpty()) {
                // Lấy ảnh bìa album từ bài hát đầu tiên trong album
                byte[] albumArt;
                try {
                    albumArt = getAlbumArt(albumSongs.get(0).getPath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (albumArt != null) {
                    Glide.with(this)
                            .asBitmap()
                            .load(albumArt)
                            .apply(new RequestOptions().fitCenter())
                            .into(binding.albumArt);
                } else {
                    binding.albumArt.setImageResource(R.drawable.music_note); // Ảnh mặc định nếu không có ảnh bìa
                }
            }

            // Thiết lập adapter cho danh sách bài hát
            songAdapter = new SongAdapter(this, albumSongs, position -> {
                // Xử lý sự kiện khi một bài hát được chọn, mở PlayerActivity với bài hát đã chọn
                Intent intent = new Intent(AlbumDetailActivity.this, PlayerActivity.class);
                intent.putExtra("songs", albumSongs); // Sử dụng Serializable thay vì Parcelable
                intent.putExtra("position", position);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up_in, R.anim.no_animation); // Thêm hoạt ảnh
            });
            binding.songsList.setAdapter(songAdapter);
            binding.songsList.setLayoutManager(new LinearLayoutManager(this));
        }

        // Xử lý sự kiện nút quay lại
        binding.backButton.setOnClickListener(view -> finish());
    }

    // Lấy danh sách các bài hát trong album và sắp xếp theo id từ bé đến lớn
    private ArrayList<SongModel> getAlbumSongs(String albumName) {
        ArrayList<SongModel> songsInAlbum = MainActivity.songModels.stream()
                .filter(song -> albumName.equals(song.getAlbum()))
                .collect(Collectors.toCollection(ArrayList::new));

        // Sắp xếp các bài hát theo id từ bé đến lớn
        songsInAlbum.sort(Comparator.comparingLong(SongModel::getId));

        return songsInAlbum;
    }

    // Lấy ảnh bìa album từ đường dẫn của bài hát
    private byte[] getAlbumArt(String uri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(uri);
            return retriever.getEmbeddedPicture();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            retriever.release();
        }
    }
}
