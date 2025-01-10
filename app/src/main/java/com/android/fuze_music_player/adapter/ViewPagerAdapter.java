package com.android.fuze_music_player.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.android.fuze_music_player.fragment.AlbumsFragment;
import com.android.fuze_music_player.fragment.ArtistsFragment;
import com.android.fuze_music_player.fragment.ForYouFragment;
import com.android.fuze_music_player.fragment.SongsFragment;
import com.android.fuze_music_player.model.SongModel;

import java.util.ArrayList;

public class ViewPagerAdapter extends FragmentStateAdapter {
    // Danh sách các bài hát
    private final ArrayList<SongModel> songModels;

    // Hàm khởi tạo
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, ArrayList<SongModel> songModels) {
        super(fragmentActivity);
        this.songModels = songModels;
    }

    // Phương thức tạo Fragment tương ứng với vị trí của ViewPager
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        // Tạo một Bundle để chuyển dữ liệu sang các Fragment
        Bundle args = new Bundle();
        args.putSerializable("songs", songModels); // Sử dụng Serializable thay vì Parcelable

        // Tạo các Fragment tương ứng với từng vị trí
        switch (position) {
            case 0:
                fragment = new ForYouFragment();
                break;
            case 1:
                fragment = new SongsFragment();
                break;
            case 2:
                fragment = new AlbumsFragment();
                break;
            case 3:
                fragment = new ArtistsFragment();
                break;
            default:
                fragment = new ForYouFragment();
        }

        // Đặt Bundle vào Fragment
        fragment.setArguments(args);
        return fragment;
    }

    // Trả về tổng số lượng Fragment
    @Override
    public int getItemCount() {
        return 4;
    }
}
