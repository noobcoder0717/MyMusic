package adpater;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mvpmymusic.R;

import java.util.List;

import bean.SongUrl;
import bean.SongUrlSorted;
import callback.OnItemClickListener;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {
    OnItemClickListener onItemClickListener;

    List<String> songList;
    List<List<String>> singerList;
    List<String> albumList;
    List<String> songmidList;
    List<SongUrlSorted> songUrlList;
    Context mContext;
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

    public SongAdapter(Context context, List<String> songList, List<List<String>> singerList, List<String> albumList, List<String> songmidList, List<SongUrlSorted> songUrlList){
        mContext=context;
        this.songList=songList;
        this.singerList=singerList;
        this.albumList=albumList;
        this.songmidList=songmidList;
        this.songUrlList=songUrlList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song,parent,false);
        ViewHolder holder=new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position){
        String song=songList.get(position);
        List<String> singers=singerList.get(position);
        String album=albumList.get(position);
        String singer="";
//        final SongUrlSorted stringUrl=songUrlList.get(position);
//        final String finalUrl=stringUrl.getUrl();
        for(int i=0;i<singers.size();i++){
            singer+=singers.get(i);
            if(i!=singers.size()-1)
                singer+="/";
        }
        viewHolder.songname.setText(song);
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
