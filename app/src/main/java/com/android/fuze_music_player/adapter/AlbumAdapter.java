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

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.MyViewHolder> {
    // Lưu trữ một tham chiếu yếu đến Context để tránh rò rỉ bộ nhớ
    private final WeakReference<Context> mContext;
    // Danh sách các Album
    private final ArrayList<Album> mAlbums;
    // Listener để xử lý sự kiện nhấp vào Album
    private final OnAlbumClickListener mListener;

    // Khởi tạo AlbumAdapter với Context, danh sách Album và Listener
    public AlbumAdapter(Context mContext, ArrayList<Album> albums, OnAlbumClickListener listener) {
        this.mContext = new WeakReference<>(mContext);
        this.mAlbums = albums;
        this.mListener = listener;
    }

    // Tạo một ViewHolder mới
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = mContext.get();
        if (context == null) {
            throw new IllegalStateException("Context is null");
        }
        View view = LayoutInflater.from(context).inflate(R.layout.item_album, parent, false);
        return new MyViewHolder(view, mListener);
    }

    // Gán dữ liệu cho ViewHolder
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Album album = mAlbums.get(position);
        holder.albumName.setText(album.getName());
        holder.artistName.setText(album.getDominantArtist());
        new LoadAlbumArtTask(holder.albumArt, mContext.get()).execute(album.getPath());
        holder.itemView.setTag(album);
    }

    // Trả về số lượng Album
    @Override
    public int getItemCount() {
        return mAlbums.size();
    }

    // Xóa hình ảnh khi ViewHolder được tái sử dụng
    @Override
    public void onViewRecycled(@NonNull MyViewHolder holder) {
        super.onViewRecycled(holder);
        Glide.with(holder.itemView.getContext()).clear(holder.albumArt);
    }

    // Interface để định nghĩa sự kiện nhấp vào Album
    public interface OnAlbumClickListener {
        void onAlbumClick(Album album);
    }

    // ViewHolder để lưu trữ các views của mỗi item Album
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView albumName;
        TextView artistName;
        ImageView albumArt;

        public MyViewHolder(@NonNull View itemView, OnAlbumClickListener listener) {
            super(itemView);
            albumName = itemView.findViewById(R.id.album_name);
            artistName = itemView.findViewById(R.id.artist_name);
            albumArt = itemView.findViewById(R.id.music_img);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onAlbumClick((Album) itemView.getTag());
                    }
                }
            });
        }
    }

    // Lớp AsyncTask để tải hình ảnh album một cách bất đồng bộ
    private static class LoadAlbumArtTask extends AsyncTask<String, Void, byte[]> {
        private final WeakReference<ImageView> imageViewReference;
        private final WeakReference<Context> contextReference;

        public LoadAlbumArtTask(ImageView imageView, Context context) {
            this.imageViewReference = new WeakReference<>(imageView);
            this.contextReference = new WeakReference<>(context);
        }

        // Tải hình ảnh album từ đường dẫn được cung cấp
        @Override
        protected byte[] doInBackground(String... params) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(params[0]);
                return retriever.getEmbeddedPicture();
            } catch (Exception e) {
                Log.e("AlbumAdapter", "Error retrieving album art for URI: " + params[0], e);
                return null;
            } finally {
                try {
                    retriever.release();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // Hiển thị hình ảnh album lên ImageView
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

    // Lớp Album để lưu trữ thông tin về một album
    public static class Album implements Serializable {
        private final String name;
        private final String dominantArtist;
        private final String path;

        public Album(String name, String dominantArtist, String path) {
            this.name = name;
            this.dominantArtist = dominantArtist;
            this.path = path;
        }

        public String getName() {
            return name;
        }

        public String getDominantArtist() {
            return dominantArtist;
        }

        public String getPath() {
            return path;
        }

        // Các phương thức equals và hashCode để so sánh các đối tượng Album
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Album album = (Album) o;

            if (!name.equals(album.name)) return false;
            return dominantArtist.equals(album.dominantArtist);
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + dominantArtist.hashCode();
            return result;
        }
    }
}