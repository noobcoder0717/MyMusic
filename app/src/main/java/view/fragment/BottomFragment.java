package view.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.mvpmymusic.R;

import org.litepal.LitePal;

import java.io.File;
import java.util.zip.Inflater;

import bean.LoveSong;
import bean.OnlineSong;
import bean.RecentSong;
import event.PlayingStatusEvent;

public class BottomFragment extends DialogFragment {
    LoveSong ls;
    RecentSong rs;
    OnlineSong os;
    LoveSongFragment lsf;

    public BottomFragment(LoveSongFragment lsf,LoveSong ls,RecentSong rs,OnlineSong os){
        this.lsf=lsf;
        this.ls=ls;
        this.rs=rs;
        this.os=os;
    }

    @Override
    public void onStart(){
        super.onStart();
        Window window=getDialog().getWindow();
        WindowManager.LayoutParams lp=window.getAttributes();
        lp.gravity= Gravity.BOTTOM;
        lp.width=getResources().getDisplayMetrics().widthPixels;
        lp.height=getResources().getDisplayMetrics().heightPixels/3;
        window.setAttributes(lp);
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setWindowAnimations(R.style.bottom_fragment);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.fragment_bottom,container,false);
        final BottomFragment bottomFragment=this;
        ImageView albumImg=view.findViewById(R.id.albumimage_bottomfragment);
        TextView songname=view.findViewById(R.id.songname_bottomfragment);
        TextView singer=view.findViewById(R.id.singername_bottomfragment);
        LinearLayout delete=view.findViewById(R.id.delete);
        String path=getContext().getExternalFilesDir("")+"/mvpmymusic/albumimg/";
        if(ls!=null){//lovesong
            String imgPath=path+ls.getAlbummid()+".png";
            File file=new File(imgPath);
            Glide.with(this).load(file).into(albumImg);
            songname.setText(ls.getSongName());
            singer.setText("歌手:"+getSingerNames(ls));
        }else if(rs!=null){//recentsong
            String imgPath=path+rs.getAlbummid()+".png";
            File file=new File(imgPath);
            Glide.with(this).load(file).into(albumImg);
            songname.setText(rs.getSongName());
            singer.setText("歌手:"+getSingerNames(rs));
        }else{
            String imgPath=path+os.getAlbummid()+".png";
            File file=new File(imgPath);
            Glide.with(this).load(file).into(albumImg);
            songname.setText(os.getSongName());
            singer.setText("歌手:"+getSingerNames(os));
        }


        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog=new AlertDialog.Builder(getContext());
                dialog.setMessage("您确定要将该音乐删除吗？");
                dialog.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(ls!=null){//lovesong
                            LitePal.deleteAll(LoveSong.class,"url = ?",ls.getUrl());
                            lsf.refreshUI(new PlayingStatusEvent());
                        }else if(rs!=null){//recentsong
                        }else{
                        }
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                dialog.show();
                bottomFragment.dismiss();
            }
        });







        return view;
    }

    public String getSingerNames(OnlineSong os){
        String SingerNames="";
        for(int i=0;i<os.getSingers().size();i++){
            SingerNames+=os.getSingers().get(i);
            if(i!=os.getSingers().size()-1)
                SingerNames+="/";
        }
        return SingerNames;
    }

    public String getSingerNames(RecentSong rs){
        String SingerNames="";
        for(int i=0;i<rs.getSingers().size();i++){
            SingerNames+=rs.getSingers().get(i);
            if(i!=rs.getSingers().size()-1)
                SingerNames+="/";
        }
        return SingerNames;
    }

    public String getSingerNames(LoveSong ls){
        String SingerNames="";
        for(int i=0;i<ls.getSingers().size();i++){
            SingerNames+=ls.getSingers().get(i);
            if(i!=ls.getSingers().size()-1)
                SingerNames+="/";
        }
        return SingerNames;
    }
}
