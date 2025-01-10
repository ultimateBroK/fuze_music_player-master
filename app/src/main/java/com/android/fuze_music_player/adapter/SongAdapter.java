package com.android.fuze_music_player.adapter;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.android.fuze_music_player.R;
import com.android.fuze_music_player.databinding.ItemMusicBinding;
import com.android.fuze_music_player.model.SongModel;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.MyViewHolder> {
    // Tham chiếu yếu đến đối tượng Context để tránh rò rỉ bộ nhớ
    private final WeakReference<Context> mContext;
    // Danh sách các bài hát
    private final ArrayList<SongModel> mSongs;
    // Trình nghe sự kiện khi người dùng nhấp vào một mục
    private final OnItemClickListener mListener;

    // Hàm khởi tạo
    public SongAdapter(Context context, ArrayList<SongModel> songs, OnItemClickListener listener) {
        this.mSongs = songs;
        this.mContext = new WeakReference<>(context);
        this.mListener = listener;
    }

    // Phương thức tạo một ViewHolder mới
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemMusicBinding binding = ItemMusicBinding.inflate(inflater, parent, false);
        return new MyViewHolder(binding, mListener);
    }

    // Phương thức cập nhật dữ liệu cho một ViewHolder
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        SongModel song = mSongs.get(position);
        holder.binding.songName.setText(song.getTitle());
        holder.binding.artistName.setText(song.getArtist());
        new LoadAlbumArtTask(holder.binding.musicImg, mContext.get()).execute(song.getPath());
    }

    // Phương thức trả về số lượng bài hát
    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    // Phương thức được gọi khi một ViewHolder được tái sử dụng
    @Override
    public void onViewRecycled(@NonNull MyViewHolder holder) {
        super.onViewRecycled(holder);
        Glide.with(holder.itemView.getContext()).clear(holder.binding.musicImg);
    }

    // Interface định nghĩa sự kiện khi người dùng nhấp vào một mục
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    // Lớp ViewHolder chứa các view của mỗi mục trong danh sách
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final ItemMusicBinding binding;

        public MyViewHolder(@NonNull ItemMusicBinding binding, OnItemClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            // Thêm listener vào view của mỗi mục
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

        // Tải ảnh bìa album từ đường dẫn bài hát
        @Override
        protected byte[] doInBackground(String... params) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(params[0]);
                return retriever.getEmbeddedPicture();
            } catch (Exception e) {
                Log.e("MusicAdapter", "Error retrieving album art for URI: " + params[0], e);
                return null;
            } finally {
                try {
                    retriever.release();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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