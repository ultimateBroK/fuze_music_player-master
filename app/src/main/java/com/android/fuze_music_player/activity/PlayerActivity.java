package com.android.fuze_music_player.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.android.fuze_music_player.R;
import com.android.fuze_music_player.adapter.HistoryAdapter;
import com.android.fuze_music_player.model.SongModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener, HistoryAdapter.OnItemClickListener {

    // Các hằng số cho chế độ lặp lại
    static final int REPEAT_OFF = 0;
    static final int REPEAT_ALL = 1;
    static final int REPEAT_ONE = 2;
    private static final int REQUEST_CODE_QUEUE = 1;

    // Các biến toàn cục cho danh sách bài hát, chế độ shuffle, vị trí hiện tại, và trạng thái MediaPlayer
    static ArrayList<SongModel> listSongs = new ArrayList<>();
    static ArrayList<SongModel> shuffleQueue = new ArrayList<>();
    static int shufflePosition = -1;
    static Uri uri;
    static MediaPlayer mediaPlayer;
    static boolean shuffleBoolean = false;
    private final Handler handler = new Handler();

    // Các thành phần giao diện người dùng
    TextView song_name, song_artist, duration_played, duration_total;
    ImageView cover_art;
    ImageButton skipNextBtn, skipPreviousBtn, repeatBtn, shuffleBtn, backBtn, musicQueueBtn;
    FloatingActionButton playPauseBtn;
    SeekBar seekBar;
    int position = -1;
    int repeatMode = REPEAT_OFF;
    private Thread playThread, prevThread, nextThread;
    private boolean isPlaying = false;
    private GestureDetector gestureDetector;

    // Biến để lưu trữ ảnh album hiện tại
    private Bitmap currentAlbumArt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initViews(); // Khởi tạo các view
        getIntentMethod(); // Lấy dữ liệu từ Intent

        // Khởi tạo GestureDetector cho các thao tác vuốt
        gestureDetector = new GestureDetector(this, new GestureListener());

        // Thiết lập thông tin bài hát
        song_name.setText(listSongs.get(position).getTitle());
        song_artist.setText(listSongs.get(position).getArtist());

        // Kiểm tra trạng thái MediaPlayer
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            updateUI(); // Cập nhật giao diện người dùng
        } else {
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.start();
            isPlaying = true;
            updateUI();
        }

        // Xử lý sự kiện nút quay lại
        backBtn.setOnClickListener(view -> finish());

        // Xử lý sự kiện thay đổi seek bar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Cập nhật vị trí seek bar và thời gian chơi hiện tại
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    duration_played.setText(formattedTime(mCurrentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        });

        // Xử lý sự kiện nút shuffle
        shuffleBtn.setOnClickListener(v -> toggleShuffle());

        // Xử lý sự kiện nút repeat
        repeatBtn.setOnClickListener(v -> toggleRepeat());

        // Thêm bài hát vào lịch sử
        addSongToHistory(listSongs.get(position));

        // Xử lý sự kiện khi nhấn nút music_queue
        musicQueueBtn.setOnClickListener(v -> {
            Intent queueIntent = new Intent(PlayerActivity.this, QueueActivity.class);
            queueIntent.putExtra("queueList", listSongs);
            queueIntent.putExtra("currentSongPosition", position); // Truyền vị trí bài hát hiện tại
            startActivityForResult(queueIntent, REQUEST_CODE_QUEUE);
        });
    }

    // Cập nhật giao diện người dùng
    private void updateUI() {
        seekBar.setMax(mediaPlayer.getDuration() / 1000);
        playPauseBtn.setImageResource(mediaPlayer.isPlaying() ? R.drawable.pause_40dp : R.drawable.play_arrow_40dp);
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    duration_played.setText(formattedTime(mCurrentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        });
        // Cập nhật ảnh bìa
        if (currentAlbumArt != null) {
            updateCoverArt(currentAlbumArt);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_QUEUE && resultCode == RESULT_OK) {
            int newSongPosition = data.getIntExtra("newSongPosition",-1);
            if (newSongPosition != -1 && newSongPosition != position) {
                position = newSongPosition;
                playSongAtPosition(); // Chơi bài hát ở vị trí mới
            }
        }
    }

    // Xử lý sự kiện vuốt sang phải
    private void onSwipeRight() {
        prevBtnClicked();
    }

    // Xử lý sự kiện vuốt sang trái
    private void onSwipeLeft() {
        nextBtnClicked();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    protected void onResume() {
        setButtonListeners(); // Thiết lập listener cho các nút
        super.onResume();
    }

    // Thiết lập listener cho các nút
    private void setButtonListeners() {
        prevThreadBtn();
        nextThreadBtn();
        playThreadBtn();
    }

    // Thiết lập listener cho nút trước đó
    private void prevThreadBtn() {
        prevThread = new Thread() {
            @Override
            public void run() {
                skipPreviousBtn.setOnClickListener(v -> prevBtnClicked());
            }
        };
        prevThread.start();
    }

    // Xử lý sự kiện khi nút trước đó được nhấn
    private void prevBtnClicked() {
        changeSongPosition(false); // Thay đổi vị trí bài hát (lùi)
        playSongAtPosition(); // Chơi bài hát ở vị trí hiện tại
    }

    // Thiết lập listener cho nút tiếp theo
    private void nextThreadBtn() {
        nextThread = new Thread() {
            @Override
            public void run() {
                skipNextBtn.setOnClickListener(v -> nextBtnClicked());
            }
        };
        nextThread.start();
    }

    // Xử lý sự kiện khi nút tiếp theo được nhấn
    private void nextBtnClicked() {
        changeSongPosition(true); // Thay đổi vị trí bài hát (tiến)
        playSongAtPosition(); // Chơi bài hát ở vị trí hiện tại
    }

    // Thiết lập listener cho nút play/pause
    private void playThreadBtn() {
        playThread = new Thread() {
            @Override
            public void run() {
                playPauseBtn.setOnClickListener(v -> playPauseBtnClicked());
            }
        };
        playThread.start();
    }

    // Xử lý sự kiện khi nút play/pause được nhấn
    private void playPauseBtnClicked() {
        if (mediaPlayer == null) {
            Log.e("PlayerActivity", "MediaPlayer is null or in an unexpected state.");
            return;
        }

        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isPlaying = false;
                playPauseBtn.setImageResource(R.drawable.play_arrow_40dp);
            } else {
                mediaPlayer.start();
                isPlaying = true;
                playPauseBtn.setImageResource(R.drawable.pause_40dp);
            }

            seekBar.setMax(mediaPlayer.getDuration() / 1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                        duration_played.setText(formattedTime(mCurrentPosition));
                    }
                    handler.postDelayed(this, 1000);
                }
            });
        } catch (IllegalStateException e) {
            Log.e("PlayerActivity", "IllegalStateException: " + e.getMessage());
        }
    }

    // Định dạng thời gian thành chuỗi phút:giây
    private String formattedTime(int mCurrentPosition) {
        String totalout;
        String totalNew;
        String seconds = String.valueOf(mCurrentPosition % 60);
        String minutes = String.valueOf(mCurrentPosition / 60);
        totalout = minutes + ":" + seconds;
        totalNew = minutes + ":" + "0" + seconds;
        if (seconds.length() == 1) {
            return totalNew;
        } else {
            return totalout;
        }
    }

    // Lấy dữ liệu từ Intent
    private void getIntentMethod() {
        Intent intent = getIntent();
        position = intent.getIntExtra("position", -1);
        boolean isFromStatus = intent.getBooleanExtra("isFromStatus", false);

        listSongs = (ArrayList<SongModel>) intent.getSerializableExtra("songs");

        if (listSongs != null) {
            playPauseBtn.setImageResource(R.drawable.pause_40dp);
            uri = Uri.parse(listSongs.get(position).getPath());
        }
        if (mediaPlayer != null && !isFromStatus) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.start();
            isPlaying = true;
        } else if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.start();
            isPlaying = true;
        }
        seekBar.setMax(mediaPlayer.getDuration() / 1000);
        metaData(uri);
        sendCurrentSongInfoToMainActivity();
    }

    // Khởi tạo các view
    private void initViews() {
        song_name = findViewById(R.id.song_name);
        song_artist = findViewById(R.id.song_artist);
        duration_played = findViewById(R.id.duration_played);
        duration_total = findViewById(R.id.duration_total);
        cover_art = findViewById(R.id.cover_art);
        skipNextBtn = findViewById(R.id.skip_next);
        skipPreviousBtn = findViewById(R.id.skip_previous);
        repeatBtn = findViewById(R.id.repeat);
        shuffleBtn = findViewById(R.id.shuffle);
        backBtn = findViewById(R.id.back_btn);
        playPauseBtn = findViewById(R.id.play_pause);
        seekBar = findViewById(R.id.play_status);
        musicQueueBtn = findViewById(R.id.music_queue);
    }

    // Lấy thông tin metadata của bài hát
    private void metaData(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(PlayerActivity.uri.toString());

        int durationTotal = (int) (listSongs.get(position).getDuration() / 1000);
        duration_total.setText(formattedTime(durationTotal));

        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap = null;
        if (art != null) {
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            currentAlbumArt = bitmap; // Lưu trữ ảnh album hiện tại
            updateCoverArt(bitmap);
        } else {
            Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.music_note)
                    .into(cover_art);
            ImageView imgGradient = findViewById(R.id.imgGradient);
            FloatingActionButton play_pause = findViewById(R.id.play_pause);
            imgGradient.setBackgroundResource(R.drawable.gradient_background);
            play_pause.setBackgroundResource(R.drawable.gradient_background);
            play_pause.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
        }
    }

    // Cập nhật ảnh bìa
    private void updateCoverArt(Bitmap bitmap) {
        ImageAnimation(this, cover_art, bitmap);

        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(@Nullable Palette palette) {
                Palette.Swatch swatch = palette.getDominantSwatch();
                if (swatch != null) {
                    ImageView imgGradient = findViewById(R.id.imgGradient);
                    FloatingActionButton play_pause = findViewById(R.id.play_pause);
                    imgGradient.setBackgroundResource(R.drawable.gradient_background);

                    GradientDrawable gradientDrawable = new GradientDrawable(
                            GradientDrawable.Orientation.TOP_BOTTOM,
                            new int[]{swatch.getRgb(), 0x00000000}
                    );
                    imgGradient.setBackground(gradientDrawable);
                    play_pause.setBackground(gradientDrawable);

                    play_pause.setBackgroundTintList(ColorStateList.valueOf(swatch.getRgb()));
                }
            }
        });
    }

    // Animation cho ảnh bài hát
    public void ImageAnimation(Context context, @NonNull ImageView imageView, Bitmap bitmap) {
        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                imageView.startAnimation(animIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        imageView.startAnimation(animOut);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        switch (repeatMode) {
            case REPEAT_ALL:
                nextBtnClicked();
                break;
            case REPEAT_ONE:
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    uri = Uri.parse(listSongs.get(position).getPath());
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                    mediaPlayer.setOnCompletionListener(this);
                    mediaPlayer.start();
                    seekBar.setMax(mediaPlayer.getDuration() / 1000);
                    PlayerActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mediaPlayer != null) {
                                int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                                seekBar.setProgress(mCurrentPosition);
                                duration_played.setText(formattedTime(mCurrentPosition));
                            }
                            handler.postDelayed(this, 1000);
                        }
                    });
                }
                break;
            case REPEAT_OFF:
            default:
                nextBtnClicked();
                break;
        }

        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(this);
        }
    }

    // Thay đổi vị trí bài hát
    private void changeSongPosition(boolean isNext) {
        if (shuffleBoolean && repeatMode != REPEAT_ONE) {
            if (isNext) {
                shufflePosition = (shufflePosition + 1) % shuffleQueue.size();
            } else {
                shufflePosition = (shufflePosition - 1 + shuffleQueue.size()) % shuffleQueue.size();
            }
            position = listSongs.indexOf(shuffleQueue.get(shufflePosition));
        } else if (!shuffleBoolean && repeatMode != REPEAT_ONE) {
            if (isNext) {
                position = (position + 1) % listSongs.size();
            } else {
                position = (position - 1 + listSongs.size()) % listSongs.size();
            }
        }
    }

    // Chơi bài hát ở vị trí hiện tại
    private void playSongAtPosition() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }

        uri = Uri.parse(listSongs.get(position).getPath());
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        if (mediaPlayer != null) {
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            song_artist.setText(listSongs.get(position).getArtist());
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mediaPlayer.setOnCompletionListener(PlayerActivity.this);
            playPauseBtn.setImageResource(R.drawable.pause_40dp);
            mediaPlayer.start();
            addSongToHistory(listSongs.get(position));
            sendCurrentSongInfoToMainActivity();
        } else {
            Log.e("PlayerActivity", "MediaPlayer could not be created for the given URI.");
        }
    }

    // Chuyển đổi trạng thái shuffle
    private void toggleShuffle() {
        if (shuffleBoolean) {
            shuffleBoolean = false;
            shuffleBtn.setImageResource(R.drawable.shuffle_24dp);
            shuffleQueue.clear();
        } else {
            shuffleBoolean = true;
            shuffleBtn.setImageResource(R.drawable.shuffle_on_24dp);
            shuffleQueue.clear();
            shuffleQueue.add(listSongs.get(position));
            ArrayList<SongModel> remainingSongs = new ArrayList<>(listSongs);
            remainingSongs.remove(position);
            Random random = new Random();
            while (!remainingSongs.isEmpty()) {
                int randomIndex = random.nextInt(remainingSongs.size());
                shuffleQueue.add(remainingSongs.remove(randomIndex));
            }
            shufflePosition = 0;
        }
    }

    // Chuyển đổi trạng thái repeat
    private void toggleRepeat() {
        switch (repeatMode) {
            case REPEAT_OFF:
                repeatMode = REPEAT_ALL;
                repeatBtn.setImageResource(R.drawable.repeat_on_24dp);
                break;
            case REPEAT_ALL:
                repeatMode = REPEAT_ONE;
                repeatBtn.setImageResource(R.drawable.repeat_one_on_24dp);
                break;
            case REPEAT_ONE:
                repeatMode = REPEAT_OFF;
                repeatBtn.setImageResource(R.drawable.repeat_24dp);
                break;
        }
    }

    // Thêm bài hát vào lịch sử
    private void addSongToHistory(SongModel song) {
        SharedPreferences sharedPreferences = getSharedPreferences("music_player", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("history", null);
        Type type = new TypeToken<ArrayList<SongModel>>() {}.getType();
        ArrayList<SongModel> historyList = gson.fromJson(json, type);

        if (historyList == null) {
            historyList = new ArrayList<>();
        }

        for (int i = 0; i < historyList.size(); i++) {
            if (historyList.get(i).getPath().equals(song.getPath())) {
                historyList.remove(i);
                break;
            }
        }

        historyList.add(0, song);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        json = gson.toJson(historyList);
        editor.putString("history", json);
        editor.apply();
    }

    @Override
    public void onItemClick(int position) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        SongModel song = listSongs.get(position);
        uri = Uri.parse(song.getPath());

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(this);

        metaData(uri);

        song_name.setText(song.getTitle());
        song_artist.setText(song.getArtist());
        seekBar.setMax(mediaPlayer.getDuration() / 1000);
        playPauseBtn.setImageResource(R.drawable.pause_40dp);

        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    duration_played.setText(formattedTime(mCurrentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        });

        addSongToHistory(song);
        sendCurrentSongInfoToMainActivity();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sendCurrentSongInfoToMainActivity();
    }

    // Gửi thông tin bài hát hiện tại về MainActivity
    private void sendCurrentSongInfoToMainActivity() {
        Intent intent = new Intent("com.example.music_player.CURRENT_SONG_INFO");
        intent.putExtra("songTitle", listSongs.get(position).getTitle());
        intent.putExtra("songArtist", listSongs.get(position).getArtist());
        intent.putExtra("songPath", listSongs.get(position).getPath());
        intent.putExtra("isPlaying", mediaPlayer != null && mediaPlayer.isPlaying());
        sendBroadcast(intent);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_down_out); // Không có hoạt ảnh vào, chỉ có hoạt ảnh ra
    }

    // GestureListener để xử lý các thao tác vuốt
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight();
                    } else {
                        onSwipeLeft();
                    }
                    return true;
                }
            }
            return false;
        }
    }
}
