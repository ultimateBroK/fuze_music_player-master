package com.android.fuze_music_player.service;

import com.android.fuze_music_player.adapter.AlbumAdapter;
import com.android.fuze_music_player.model.SongModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AlbumService {
    public AlbumService() {
    }

    // Hàm lấy danh sách các album duy nhất từ danh sách các bài hát
    public static ArrayList<AlbumAdapter.Album> getUniqueAlbums(ArrayList<SongModel> songs) {
        Map<String, Map<String, Integer>> albumArtistCountMap = new HashMap<>();
        Map<String, String> albumArtPathMap = new HashMap<>();

        for (SongModel song : songs) {
            String albumName = song.getAlbum();
            String artistName = song.getArtist();
            String songPath = song.getPath();

            albumArtPathMap.put(albumName, songPath);

            Map<String, Integer> artistCountMap = albumArtistCountMap.getOrDefault(albumName, new HashMap<>());
            artistCountMap.put(artistName, artistCountMap.getOrDefault(artistName, 0) + 1);
            albumArtistCountMap.put(albumName, artistCountMap);
        }

        ArrayList<AlbumAdapter.Album> uniqueAlbums = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> entry : albumArtistCountMap.entrySet()) {
            String albumName = entry.getKey();
            Map<String, Integer> artistCountMap = entry.getValue();

            String dominantArtist = getDominantArtist(artistCountMap);
            String albumArtPath = albumArtPathMap.get(albumName);

            uniqueAlbums.add(new AlbumAdapter.Album(albumName, dominantArtist, albumArtPath));
        }
        return uniqueAlbums;
    }

    // Hàm lấy nghệ sĩ chính trong một album
    public static String getDominantArtist(Map<String, Integer> artistCountMap) {
        return artistCountMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Various Artists");
    }
}
