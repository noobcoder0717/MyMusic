package adpater;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mvpmymusic.R;

import java.util.List;

import bean.RecentSong;
import bean.SongUrlSorted;
import callback.OnItemClickListener;

public class RecentSongAdapter extends RecyclerView.Adapter<RecentSongAdapter.ViewHolder> {
    OnItemClickListener onItemClickListener;
    List<RecentSong> songList;

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView songname;
        TextView singer_album;
        View songView;
        ViewHolder(View view){
            super(view);
            songView=view;
            songname=view.findViewById(R.id.songname);
            singer_album=view.findViewById(R.id.singer_album);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public RecentSongAdapter(List<RecentSong> songList){
        this.songList=songList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song,parent,false);
        ViewHolder holder=new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder,final int position){
        RecentSong song=songList.get(position);
        List<String> singers=song.getSingers();
        String album=song.getAlbumName();
        String singer="";
        for(int i=0;i<singers.size();i++){
            singer+=singers.get(i);
            if(i!=singers.size()-1)
                singer+='/';
        }
        viewHolder.songname.setText(song.getSongName());
        viewHolder.singer_album.setText(singer+" - "+album);
        viewHolder.songView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.onClick(position);
            }
        });
    }
    @Override
    public int getItemCount(){
        return songList.size();
    }

}
