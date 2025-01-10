package com.android.fuze_music_player.service;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.fuze_music_player.model.SongModel;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

public class SongService {

    public static final int REQUEST_CODE = 1;

    public SongService() {
    }

    public static ArrayList<SongModel> getAllSongs(Context context) {
        ArrayList<SongModel> songModels = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
        };
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, sortOrder)) {
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String title = cursor.getString(titleColumn);
                    String artist = cursor.getString(artistColumn);
                    String album = cursor.getString(albumColumn);
                    String duration = cursor.getString(durationColumn);
                    String path = cursor.getString(pathColumn);

                    SongModel songModel = new SongModel(id, title, artist, album, Long.parseLong(duration), path);
                    songModels.add(songModel);
                }
            }
        } catch (Exception e) {
            Log.e("GetAllAudio", "Error fetching audio files", e);
        }

        return songModels;
    }

    public static int findSongPositionByPath(String path, ArrayList<SongModel> songModels) {
        for (int i = 0; i < songModels.size(); i++) {
            if (songModels.get(i).getPath().equals(path)) {
                return i;
            }
        }
        return -1;
    }

    public static byte[] getAlbumArt(String uri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }


    public static boolean checkPermission(Activity activity, Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_MEDIA_AUDIO)) {
                new AlertDialog.Builder(context)
                        .setTitle("Permission needed")
                        .setMessage("This permission is needed to access your audio files.")
                        .setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, REQUEST_CODE))
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .create().show();
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, REQUEST_CODE);
            }
            return false;
        } else {
            return true;
        }
    }

    public static void saveSongListToPreferences(Context context, ArrayList<SongModel> songList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("music_player", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(songList);
        editor.putString("song_list", json);
        editor.apply();
    }

}
