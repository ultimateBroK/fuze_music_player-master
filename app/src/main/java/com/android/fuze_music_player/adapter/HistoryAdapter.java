package com.android.fuze_music_player.adapter;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.android.fuze_music_player.R;
import com.android.fuze_music_player.model.SongModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    // Sử dụng WeakReference để tránh rò rỉ bộ nhớ
    private final WeakReference<Context> contextRef;
    private final OnItemClickListener listener;
    private ArrayList<SongModel> historyList;

    // Khởi tạo HistoryAdapter với Context, danh sách bài hát lịch sử và listener
    public HistoryAdapter(Context context, ArrayList<SongModel> historyList, OnItemClickListener listener) {
        this.contextRef = new WeakReference<>(context);
        this.historyList = historyList;
        this.listener = listener;
    }

    // Tạo ViewHolder mới khi cần hiển thị một mục trong RecyclerView
    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = contextRef.get();
        if (context == null) {
            throw new IllegalStateException("Context is null");
        }
        View view = LayoutInflater.from(context).inflate(R.layout.item_music, parent, false);
        return new HistoryViewHolder(view, listener);
    }

    // Cập nhật dữ liệu cho ViewHolder
    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        SongModel song = historyList.get(position);
        holder.songTitle.setText(song.getTitle());
        holder.songArtist.setText(song.getArtist());

        Context context = contextRef.get();
        if (context != null) {
            new LoadAlbumArtTask(holder.albumArt, context).execute(song.getPath());
        }
    }

    // Trả về số lượng mục trong danh sách
    @Override
    public int getItemCount() {
        return historyList.size();
    }

    // Cập nhật danh sách lịch sử và thông báo cho RecyclerView cập nhật giao diện
    public void updateHistoryList(ArrayList<SongModel> newHistoryList) {
        historyList = newHistoryList;
        notifyDataSetChanged();
    }

    // Interface để xử lý sự kiện khi người dùng nhấp vào một mục trong RecyclerView
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    // ViewHolder để lưu trữ các view cho mỗi mục trong RecyclerView
    public static class HistoryViewHolder extends RecyclerView.ViewHolder {

        TextView songTitle, songArtist;
        ImageView albumArt;

        public HistoryViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            songTitle = itemView.findViewById(R.id.song_name);
            songArtist = itemView.findViewById(R.id.artist_name);
            albumArt = itemView.findViewById(R.id.music_img);

            // Gắn sự kiện onClick cho mỗi mục trong RecyclerView
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }

    // AsyncTask để tải ảnh bìa album bất đồng bộ
    private static class LoadAlbumArtTask extends AsyncTask<String, Void, byte[]> {
        private final WeakReference<ImageView> imageViewReference;
        private final WeakReference<Context> contextReference;

        public LoadAlbumArtTask(ImageView imageView, Context context) {
            this.imageViewReference = new WeakReference<>(imageView);
            this.contextReference = new WeakReference<>(context);
        }

        // Tải ảnh bìa album từ đường dẫn nhạc
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        protected byte[] doInBackground(String... params) {
            try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
                retriever.setDataSource(params[0]);
                return retriever.getEmbeddedPicture();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        // Cập nhật ảnh bìa album vào ImageView
        @Override
        protected void onPostExecute(byte[] result) {
            ImageView imageView = imageViewReference.get();
            Context context = contextReference.get();
            if (imageView != null && context != null) {
                if (result != null) {
                    Glide.with(context).asBitmap().load(result).into(imageView);
                } else {
                    Glide.with(context).load(R.drawable.music_note).into(imageView);
                }
            }
        }
    }
}
