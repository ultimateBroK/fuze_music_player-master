package com.android.fuze_music_player.service;

import com.android.fuze_music_player.model.SongModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ArtistService {

    public ArtistService() {
    }

    // Hàm lấy danh sách các nghệ sĩ độc nhất từ danh sách các bài hát và sắp xếp theo bảng chữ cái
    public static ArrayList<String> getUniqueArtists(ArrayList<SongModel> songs) {
        Set<String> artistSet = new HashSet<>();
        for (SongModel song : songs) {
            artistSet.add(song.getArtist());
        }
        ArrayList<String> uniqueArtists = new ArrayList<>(artistSet);
        Collections.sort(uniqueArtists); // Sắp xếp theo thứ tự bảng chữ cái
        return uniqueArtists;
    }
}
