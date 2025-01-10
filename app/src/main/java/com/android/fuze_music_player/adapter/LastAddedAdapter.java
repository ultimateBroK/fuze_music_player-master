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

public class LastAddedAdapter extends RecyclerView.Adapter<LastAddedAdapter.LastAddedViewHolder> {

    // Tham chiếu yếu đến Context để tránh rò rỉ bộ nhớ
    private final WeakReference<Context> contextRef;
    // Danh sách các bài hát mới thêm
    private final ArrayList<SongModel> lastAddedList;
    // Listener để xử lý sự kiện khi người dùng nhấp vào một bài hát
    private final OnItemClickListener listener;

    // Hàm khởi tạo
    public LastAddedAdapter(Context context, ArrayList<SongModel> lastAddedList, OnItemClickListener listener) {
        this.contextRef = new WeakReference<>(context);
        this.lastAddedList = lastAddedList;
        this.listener = listener;
    }

    // Tạo ViewHolder mới
    @NonNull
    @Override
    public LastAddedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = contextRef.get();
        if (context == null) {
            throw new IllegalStateException("Context is null");
        }
        View view = LayoutInflater.from(context).inflate(R.layout.item_music, parent, false);
        return new LastAddedViewHolder(view, listener);
    }

    // Gán dữ liệu vào ViewHolder
    @Override
    public void onBindViewHolder(@NonNull LastAddedViewHolder holder, int position) {
        SongModel song = lastAddedList.get(position);
        holder.songTitle.setText(song.getTitle());
        holder.songArtist.setText(song.getArtist());

        Context context = contextRef.get();
        if (context != null) {
            // Tải ảnh bìa album bất đồng bộ
            new LoadAlbumArtTask(holder.albumArt, context).execute(song.getPath());
        }
    }

    // Số lượng mục trong danh sách
    @Override
    public int getItemCount() {
        return lastAddedList.size();
    }

    // Interface để định nghĩa sự kiện khi người dùng nhấp vào một bài hát
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    // ViewHolder chứa các view của một mục trong RecyclerView
    public static class LastAddedViewHolder extends RecyclerView.ViewHolder {

        TextView songTitle, songArtist;
        ImageView albumArt;

        public LastAddedViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            songTitle = itemView.findViewById(R.id.song_name);
            songArtist = itemView.findViewById(R.id.artist_name);
            albumArt = itemView.findViewById(R.id.music_img);

            // Xử lý sự kiện khi người dùng nhấp vào một mục
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

    // Lớp AsyncTask để tải ảnh bìa album bất đồng bộ
    private static class LoadAlbumArtTask extends AsyncTask<String, Void, byte[]> {
        private final WeakReference<ImageView> imageViewReference;
        private final WeakReference<Context> contextReference;

        public LoadAlbumArtTask(ImageView imageView, Context context) {
            this.imageViewReference = new WeakReference<>(imageView);
            this.contextReference = new WeakReference<>(context);
        }

        // Tải ảnh bìa album từ đường dẫn tệp nhạc
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

        // Hiển thị ảnh bìa album lên ImageView
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
