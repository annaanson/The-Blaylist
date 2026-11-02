package com.example.blaylist;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

/**
 * Shows the songs as a list of parchment cards: image (if any), title and author (if any).
 */
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    public interface OnSongClickListener {
        void onSongClick(int index);
    }

    private final OnSongClickListener listener;
    private Song[] songs = new Song[0];

    public SongAdapter(OnSongClickListener listener) {
        this.listener = listener;
    }

    public void setSongs(Song[] songs) {
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
        holder.bind(songs[position]);
    }

    @Override
    public int getItemCount() {
        return songs.length;
    }

    /** The song's name, or its file name when it has no name. */
    static String displayName(View view, Song song) {
        if (song.getName() != null && !song.getName().isEmpty()) {
            return song.getName();
        }
        if (song.getPath_to_song() != null) {
            return new File(song.getPath_to_song()).getName();
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
            author = item.findViewById(R.id.song_author);

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
                    listener.onSongClick(index);
                }
            });
        }

        void bind(Song song) {
            title.setText(displayName(itemView, song));

            if (song.getAuthor() != null && !song.getAuthor().isEmpty()) {
                author.setText(song.getAuthor());
                author.setVisibility(View.VISIBLE);
            } else {
                author.setVisibility(View.GONE);
            }

            if (song.getImage() != null) {
                art.setImageBitmap(song.getImage());
            } else {
                art.setImageResource(R.drawable.art_placeholder);
            }
        }
    }
}
