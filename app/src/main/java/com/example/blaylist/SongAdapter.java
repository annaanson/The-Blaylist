package com.example.blaylist;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.bumptech.glide.Glide;
/**
 * Shows the songs as a list of parchment cards: image (if any), title and author (if any).
 */
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    /** Song list cards: list updates and card taps. */
    private static final String TAG = "Blaylist.SongAdapter";

    public interface OnSongClickListener {
        void onSongClick(int index);
    }

    private final OnSongClickListener listener;
    private List<Song> songs = new ArrayList<>();

    public SongAdapter(OnSongClickListener listener) {
        this.listener = listener;
    }

    public void setSongs(List<Song> songs) {
        Log.d(TAG, "setSongs: " + songs.size() + " songs");
        this.songs = songs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        holder.bind(songs.get(position));
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    /** The song's name, or its file name when it has no name. */
    static String displayName(View view, Song song) {
        if (song.getTitle() != null && !song.getTitle().isEmpty()) {
            return song.getTitle();
        }

        return view.getContext().getString(R.string.unknown_song);
    }

    class SongViewHolder extends RecyclerView.ViewHolder {
        private final ImageView art;
        private final TextView title;
        private final TextView author;

        SongViewHolder(@NonNull View item) {
            super(item);
            art = item.findViewById(R.id.song_art);
            title = item.findViewById(R.id.song_title);
            author = item.findViewById(R.id.song_artist);

            // the card sinks a little under the finger
            item.setOnTouchListener((v, event) -> {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(90).start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.animate().scaleX(1f).scaleY(1f).setDuration(160).start();
                        break;
                }
                return false;
            });
            item.setOnClickListener(v -> {
                int index = getBindingAdapterPosition();
                if (index != RecyclerView.NO_POSITION) {
                    Log.d(TAG, "card #" + index + " clicked");
                    listener.onSongClick(index);
                } else {
                    Log.e(TAG, "card clicked but it has no position (list is changing) - click ignored");
                }
            });
        }

        void bind(Song song) {
            title.setText(displayName(itemView, song));

            if (song.getArtist() != null && !song.getArtist().isEmpty()) {
                author.setText(song.getArtist());
                author.setVisibility(View.VISIBLE);
            } else {
                author.setVisibility(View.GONE);
            }

            Glide.with(art)                      // tied to this ImageView
                    .load(song.getContentUri())   // the cover embedded in the mp3, see BlaylistGlideModule
                    .placeholder(R.drawable.art_placeholder)
                    .error(R.drawable.art_placeholder)
                    .into(art);
        }
    }
}
