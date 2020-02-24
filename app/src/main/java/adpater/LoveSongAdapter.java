package adpater;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mvpmymusic.R;

import java.util.List;

import bean.LoveSong;
import callback.OnItemClickListener;
import callback.OnChoiceClickListener;

public class LoveSongAdapter extends RecyclerView.Adapter<LoveSongAdapter.ViewHolder> {
    OnItemClickListener onItemClickListener;
    OnChoiceClickListener onChoiceClickListener;

    public void setOnChoiceClickListener(OnChoiceClickListener onChoiceClickListener) {
        this.onChoiceClickListener = onChoiceClickListener;
    }

    List<LoveSong> songList;

    public LoveSongAdapter(List<LoveSong> songList){
        this.songList=songList;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView songname;
        TextView singer_album;
        View songView;
        TextView lovesong_num;
        ImageView playing;
        LinearLayout choice;
        ViewHolder(View view){
            super(view);
            songView=view.findViewById(R.id.songview);
            songname=view.findViewById(R.id.lovesongname);
            singer_album=view.findViewById(R.id.lovesinger_album);
            lovesong_num=view.findViewById(R.id.lovesong_num);
            playing=view.findViewById(R.id.playing);
            choice=view.findViewById(R.id.choice);
        }
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lovesong,parent,false);
        ViewHolder holder=new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder,final int position){
        LoveSong song=songList.get(position);
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
        viewHolder.choice.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                onChoiceClickListener.onClick(position);
            }
        });

        if(song.isPlaying().equals("no")){
            viewHolder.lovesong_num.setText((position+1)+"");
            viewHolder.lovesong_num.setVisibility(View.VISIBLE);
            viewHolder.playing.setVisibility(View.GONE);
        }
        else{
            viewHolder.lovesong_num.setText((position+1)+"");
            viewHolder.lovesong_num.setVisibility(View.GONE);
            viewHolder.playing.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount(){
        return songList.size();
    }

}
