package com.android.fuze_music_player.adapter;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.android.fuze_music_player.R;
import com.android.fuze_music_player.activity.MainActivity;
import com.android.fuze_music_player.model.SongModel;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder> {
    // Khai báo các thuộc tính cần thiết
    private final Context context;
    private final ArrayList<String> artists;
    private final OnArtistClickListener listener;

    // Khởi tạo ArtistAdapter với các tham số cần thiết
    public ArtistAdapter(Context context, ArrayList<String> artists, OnArtistClickListener listener) {
        this.context = context;
        this.artists = artists;
        this.listener = listener;
    }

    // Tạo ViewHolder mới khi cần hiển thị một mục mới
    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_artist, parent, false);
        return new ArtistViewHolder(view);
    }

    // Cập nhật các thành phần của ViewHolder với dữ liệu tương ứng
    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder holder, int position) {
        String artist = artists.get(position);
        holder.artistName.setText(artist);

        // Tải ảnh của nghệ sĩ bất đồng bộ
        new LoadArtistImageTask(holder.artistImage, context).execute(artist);

        // Thiết lập sự kiện click cho mỗi mục
        holder.itemView.setOnClickListener(v -> listener.onArtistClick(artist));
    }

    // Trả về số lượng các mục
    @Override
    public int getItemCount() {
        return artists.size();
    }

    // Interface để định nghĩa sự kiện click cho mỗi nghệ sĩ
    public interface OnArtistClickListener {
        void onArtistClick(String artist);
    }

    // ViewHolder chứa các thành phần giao diện của một mục
    public static class ArtistViewHolder extends RecyclerView.ViewHolder {
        TextView artistName;
        ImageView artistImage;

        public ArtistViewHolder(@NonNull View itemView) {
            super(itemView);
            artistName = itemView.findViewById(R.id.artist_name);
            artistImage = itemView.findViewById(R.id.music_img);
        }
    }

    // AsyncTask để tải ảnh của nghệ sĩ bất đồng bộ
    private static class LoadArtistImageTask extends AsyncTask<String, Void, byte[]> {
        private final WeakReference<ImageView> imageViewReference;
        private final WeakReference<Context> contextReference;

        public LoadArtistImageTask(ImageView imageView, Context context) {
            this.imageViewReference = new WeakReference<>(imageView);
            this.contextReference = new WeakReference<>(context);
        }

        // Tải ảnh của nghệ sĩ trong background
        @Override
        protected byte[] doInBackground(String... params) {
            String artist = params[0];
            for (SongModel song : MainActivity.songModels) {
                if (song.getArtist().equals(artist)) {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    try {
                        retriever.setDataSource(song.getPath());
                        return retriever.getEmbeddedPicture();
                    } catch (Exception e) {
                        Log.e("ArtistAdapter", "Error retrieving artist image for URI: " + song.getPath(), e);
                    } finally {
                        try {
                            retriever.release();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            return null;
        }

        // Cập nhật ảnh của nghệ sĩ vào ImageView sau khi tải xong
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