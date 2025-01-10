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

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.QueueViewHolder> {

    private final WeakReference<Context> contextRef;
    private final ArrayList<SongModel> queueList;
    private final OnItemClickListener listener;
    private final int currentSongPosition;

    // Khởi tạo adapter với dữ liệu đầu vào
    public QueueAdapter(Context context, ArrayList<SongModel> queueList, int currentSongPosition, OnItemClickListener listener) {
        this.contextRef = new WeakReference<>(context);
        this.queueList = queueList;
        this.currentSongPosition = currentSongPosition;
        this.listener = listener;
    }

    // Tạo view holder mới khi cần hiển thị một mục trong danh sách
    @NonNull
    @Override
    public QueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = contextRef.get();
        if (context == null) {
            throw new IllegalStateException("Context is null");
        }
        View view = LayoutInflater.from(context).inflate(R.layout.item_music, parent, false);
        return new QueueViewHolder(view, listener);
    }

    // Bind dữ liệu vào view holder
    @Override
    public void onBindViewHolder(@NonNull QueueViewHolder holder, int position) {
        SongModel song = queueList.get(position);
        holder.songTitle.setText(song.getTitle());
        holder.songArtist.setText(song.getArtist());

        Context context = contextRef.get();
        if (context != null) {
            new LoadAlbumArtTask(holder.albumArt, context).execute(song.getPath());
        }

        // Đặt background khác nhau cho mục đang chơi và các mục khác
        if (position == currentSongPosition) {
            holder.itemView.setBackgroundResource(R.color.highlight_color);
        } else {
            holder.itemView.setBackgroundResource(android.R.color.transparent);
        }

        // Thiết lập sự kiện click cho mỗi mục
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });
    }

    // Trả về số lượng mục trong danh sách
    @Override
    public int getItemCount() {
        return queueList.size();
    }

    // Interface để xử lý sự kiện click trên mỗi mục
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    // View holder để quản lý các view trong mỗi mục
    public static class QueueViewHolder extends RecyclerView.ViewHolder {

        TextView songTitle, songArtist;
        ImageView albumArt;

        public QueueViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            songTitle = itemView.findViewById(R.id.song_name);
            songArtist = itemView.findViewById(R.id.artist_name);
            albumArt = itemView.findViewById(R.id.music_img);

            // Thiết lập sự kiện click cho toàn bộ mục
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

    // Lớp AsyncTask để tải ảnh album bất đồng bộ
    private static class LoadAlbumArtTask extends AsyncTask<String, Void, byte[]> {
        private final WeakReference<ImageView> imageViewReference;
        private final WeakReference<Context> contextReference;

        public LoadAlbumArtTask(ImageView imageView, Context context) {
            this.imageViewReference = new WeakReference<>(imageView);
            this.contextReference = new WeakReference<>(context);
        }

        // Tải ảnh album từ đường dẫn file
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

        // Cập nhật ảnh album vào ImageView
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
