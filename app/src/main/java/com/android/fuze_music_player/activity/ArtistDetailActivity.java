package com.android.fuze_music_player.activity;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.android.fuze_music_player.R;
import com.android.fuze_music_player.adapter.SongAdapter;
import com.android.fuze_music_player.databinding.ActivityArtistDetailBinding;
import com.android.fuze_music_player.model.SongModel;

import java.io.IOException;
import java.util.ArrayList;

public class ArtistDetailActivity extends AppCompatActivity implements SongAdapter.OnItemClickListener {

    private ActivityArtistDetailBinding binding;
    private SongAdapter songAdapter;
    private ArrayList<SongModel> artistSongs = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sử dụng ViewBinding để thiết lập layout
        binding = ActivityArtistDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Lấy tên nghệ sĩ từ Intent
        String artist = getIntent().getStringExtra("artist");
        if (artist != null) {
            binding.artistName.setText(artist);
            artistSongs = getSongsByArtist(artist);

            // Lấy hình ảnh album đầu tiên của nghệ sĩ
            byte[] image;
            try {
                image = getArtistImage(artist);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (image != null) {
                Glide.with(this)
                        .asBitmap()
                        .load(image)
                        .apply(new RequestOptions().fitCenter())
                        .into(binding.artistArt);
            } else {
                binding.artistArt.setImageResource(R.drawable.music_note);
            }
        }

        // Thiết lập RecyclerView
        binding.songsList.setHasFixedSize(true);
        songAdapter = new SongAdapter(this, artistSongs, this);
        binding.songsList.setAdapter(songAdapter);
        binding.songsList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        // Xử lý sự kiện nút quay lại
        binding.backButton.setOnClickListener(view -> finish());
    }

    // Lấy danh sách các bài hát của nghệ sĩ
    private ArrayList<SongModel> getSongsByArtist(String artist) {
        ArrayList<SongModel> songs = new ArrayList<>();
        for (SongModel song : MainActivity.songModels) {
            if (song.getArtist().equals(artist)) {
                songs.add(song);
            }
        }
        return songs;
    }

    // Lấy hình ảnh album của nghệ sĩ từ bài hát đầu tiên trong danh sách
    private byte[] getArtistImage(String artist) throws IOException {
        for (SongModel song : MainActivity.songModels) {
            if (song.getArtist().equals(artist)) {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(song.getPath());
                byte[] art = retriever.getEmbeddedPicture();
                retriever.release();
                return art;
            }
        }
        return null;
    }

    @Override
    public void onItemClick(int position) {
        // Xử lý sự kiện khi một bài hát được chọn, mở PlayerActivity với bài hát đã chọn
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("position", position);
        intent.putExtra("songs", artistSongs); // Sử dụng Serializable thay vì Parcelable
        intent.putExtra("source", "ArtistDetailActivity");
        startActivity(intent);
        overridePendingTransition(R.anim.slide_up_in, R.anim.no_animation); // Thêm hoạt ảnh
    }
}
