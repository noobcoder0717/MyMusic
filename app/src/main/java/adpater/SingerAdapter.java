package adpater;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mvpmymusic.R;

import java.util.List;

public class SingerAdapter extends RecyclerView.Adapter<SingerAdapter.ViewHolder> {

    List<String> singernameList;
    Context mContext;

    public SingerAdapter(Context context,List<String> singernameList){
        this.singernameList = singernameList;
        mContext=context;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView img;
        TextView text;
        ViewHolder(View view){
            super(view);
            text=view.findViewById(R.id.singer_name);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int type){
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_singer,parent,false);
        final ViewHolder viewholder=new ViewHolder(view);
        return viewholder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder,int position){
        String name=singernameList.get(position);
        viewHolder.text.setText(name);
    }

    @Override
    public int getItemCount(){
        return singernameList.size();
    }
}