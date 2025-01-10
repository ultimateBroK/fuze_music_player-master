package com.android.fuze_music_player.activity;

import static com.android.fuze_music_player.service.SongService.REQUEST_CODE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.android.fuze_music_player.R;
import com.android.fuze_music_player.adapter.ViewPagerAdapter;
import com.android.fuze_music_player.model.SongModel;
import com.android.fuze_music_player.service.SongService;
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static ArrayList<SongModel> songModels;
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private ViewPagerAdapter viewPagerAdapter;
    private ImageView playingCoverArt;
    private TextView playingSongName, playingArtistName;
    private SongModel lastPlayedSong;
    private SharedPreferences sharedPreferences;
    private TextView headerTitle;
    private int currentPosition = -1;
    private boolean isPlaying = false;

    private final BroadcastReceiver songInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "com.example.music_player.CURRENT_SONG_INFO".equals(intent.getAction())) {
                String songTitle = intent.getStringExtra("songTitle");
                String songArtist = intent.getStringExtra("songArtist");
                String songPath = intent.getStringExtra("songPath");
                isPlaying = intent.getBooleanExtra("isPlaying", false);

                currentPosition = SongService.findSongPositionByPath(songPath, songModels);
                playingSongName.setText(songTitle);
                playingArtistName.setText(songArtist);

                byte[] image = null;
                try {
                    image = SongService.getAlbumArt(songPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (image != null) {
                    Glide.with(MainActivity.this).asBitmap().load(image).into(playingCoverArt);
                } else {
                    playingCoverArt.setImageResource(R.drawable.music_note);
                }
            }
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playingCoverArt = findViewById(R.id.playing_cover_art);
        playingSongName = findViewById(R.id.playing_song_name);
        playingArtistName = findViewById(R.id.playing_artist_name);
        headerTitle = findViewById(R.id.title);

        sharedPreferences = getSharedPreferences("music_player", MODE_PRIVATE);

        boolean checkPermission = SongService.checkPermission(this, this);
        if (checkPermission) {
            try {
                initApp();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        registerReceiver(songInfoReceiver, new IntentFilter("com.example.music_player.CURRENT_SONG_INFO"), Context.RECEIVER_NOT_EXPORTED);

        RelativeLayout playingStatusLayout = findViewById(R.id.playing_status);
        playingStatusLayout.setOnClickListener(v -> {
            if (isPlaying && currentPosition != -1) {
                Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                intent.putExtra("position", currentPosition);
                intent.putExtra("songs", songModels);
                intent.putExtra("isFromStatus", true); // Thêm cờ để nhận biết mở từ trạng thái đang phát nhạc
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "No song is currently playing", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.slide_up_in, R.anim.no_animation); // Hoạt ảnh vào
    }


    private void initApp() throws IOException {
        songModels = SongService.getAllSongs(this);
        SongService.saveSongListToPreferences(this, songModels);
        initViewPager();
        loadLastPlayedSong();
        updatePlayingStatusBar(lastPlayedSong);
    }

    private void initViewPager() {
        tabLayout = findViewById(R.id.tab_layout);
        viewPager2 = findViewById(R.id.view_pager);
        viewPagerAdapter = new ViewPagerAdapter(this, songModels);
        viewPager2.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setIcon(R.drawable.face_24dp);
                    break;
                case 1:
                    tab.setIcon(R.drawable.music_note_24dp);
                    break;
                case 2:
                    tab.setIcon(R.drawable.album_24dp);
                    break;
                case 3:
                    tab.setIcon(R.drawable.artist_24dp);
                    break;
            }
        }).attach();

        viewPager2.setCurrentItem(0);
        updateHeader(0);

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateHeader(position);
            }
        });
    }

    private void updateHeader(int position) {
        LinearLayout headerLayout = findViewById(R.id.header_layout);
        switch (position) {
            case 0:
                headerTitle.setText("For You");
                headerLayout.setVisibility(View.VISIBLE);
                break;
            case 1:
                headerTitle.setText("Songs");
                headerLayout.setVisibility(View.VISIBLE);
                break;
            case 2:
                headerTitle.setText("Albums");
                headerLayout.setVisibility(View.VISIBLE);
                break;
            case 3:
                headerTitle.setText("Artists");
                headerLayout.setVisibility(View.VISIBLE);
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    initApp();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadLastPlayedSong() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString("last_played_song", null);
        lastPlayedSong = gson.fromJson(json, SongModel.class);
    }

    private void updatePlayingStatusBar(SongModel song) throws IOException {
        if (song != null) {
            playingSongName.setText(song.getTitle());
            playingArtistName.setText(song.getArtist());
            byte[] image = SongService.getAlbumArt(song.getPath());
            if (image != null) {
                Glide.with(this).asBitmap().load(image).into(playingCoverArt);
            } else {
                playingCoverArt.setImageResource(R.drawable.music_note);
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(songInfoReceiver);
    }
}
