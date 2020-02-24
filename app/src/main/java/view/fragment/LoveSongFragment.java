package view.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mvpmymusic.R;
import com.google.android.material.appbar.AppBarLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;

import java.util.List;

import adpater.LoveSongAdapter;
import bean.CurrentSong;
import bean.LoveSong;
import butterknife.Bind;
import butterknife.ButterKnife;
import callback.OnChoiceClickListener;
import callback.OnItemClickListener;
import event.PlayingStatusEvent;
import jp.wasabeef.glide.transformations.BlurTransformation;
import service.PlayService;
import view.MainActivity;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class LoveSongFragment extends Fragment {
    List<LoveSong> songList;
    LoveSongAdapter adapter;
    PlayService.PlayBinder playBinder;

    @Bind(R.id.toolbar_lovesong)
    Toolbar toolbar;

    @Bind(R.id.appbarlayout)
    AppBarLayout appBarLayout;

    @Bind(R.id.lovesong_recyclerview)
    RecyclerView songRecyclerView;

    @Bind(R.id.lovesong_coordinatorlayout)
    CoordinatorLayout coordinatorLayout;

    @Bind(R.id.lovesong_framelayout)
    FrameLayout frameLayout;

    @Bind(R.id.error_frame)
    FrameLayout errorFrame;

    @Bind(R.id.blur_image)
    ImageView blurImage;

    private ServiceConnection playConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            playBinder=(PlayService.PlayBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent playIntent=new Intent(getActivity(),PlayService.class);
        getActivity().bindService(playIntent,playConnection, Context.BIND_AUTO_CREATE);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.fragment_lovesong,container,false);
        final LoveSongFragment lsf=this;
        ButterKnife.bind(this,view);
        setHasOptionsMenu(true);

        Window window=getActivity().getWindow();
        window.setStatusBarColor(Color.BLACK);
        AppCompatActivity activity=(AppCompatActivity)getActivity();
        activity.setSupportActionBar(toolbar);
        ActionBar actionBar=activity.getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        songList= LitePal.findAll(LoveSong.class);
        if(songList.size()!=0){
            adapter=new LoveSongAdapter(songList);
            adapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onClick(int position) {
                    LitePal.deleteAll(CurrentSong.class);
                    List<LoveSong> loveSongList=LitePal.findAll(LoveSong.class);
                    //初始化当前播放歌曲列表
                    for(int i=0;i<loveSongList.size();i++){
                        CurrentSong cs = new CurrentSong();
                        LoveSong ls=loveSongList.get(i);
                        cs.setSingers(ls.getSingers());
                        cs.setDuration(ls.getDuration());
                        cs.setSongmId(ls.getSongmId());
                        cs.setSongName(ls.getSongName());
                        cs.setAlbumName(ls.getAlbumName());
                        cs.setUrl(ls.getUrl());
                        cs.setAlbummid(ls.getAlbummid());
                        cs.setPosition(i);
                        cs.setNeedPay(ls.isNeedPay());
                        cs.setInterval(ls.getInterval());
                        cs.save();
                    }
                    List<CurrentSong> currentSongList=LitePal.findAll(CurrentSong.class);
                    CurrentSong cs = currentSongList.get(position);
                    playBinder.play(cs);
                }
            });
            adapter.setOnChoiceClickListener(new OnChoiceClickListener() {
                @Override
                public void onClick(int position) {
                    Log.i("LoveSongFragment","choice clicked");
                    LoveSong loveSong=songList.get(position);
                    BottomFragment fragment=new BottomFragment(lsf,loveSong,null,null);
                    fragment.show(getFragmentManager(),"fragment");
                }
            });
            LinearLayoutManager llm=new LinearLayoutManager(getActivity());
            llm.setOrientation(RecyclerView.VERTICAL);
            songRecyclerView.setAdapter(adapter);
            songRecyclerView.setLayoutManager(llm);
        }
        else{
            frameLayout.setVisibility(View.GONE);
            errorFrame.setVisibility(View.VISIBLE);
        }

        Glide.with(this).load(R.drawable.shingekinogyojin).apply(bitmapTransform(new BlurTransformation(100))).into(blurImage);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.menu_lovesongfragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case android.R.id.home:
                MainActivity activity=(MainActivity)getActivity();
                activity.pop();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();
        refreshUI(new PlayingStatusEvent());
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshUI(PlayingStatusEvent event){
        final LoveSongFragment lsf=this;
        final List<LoveSong> loveSongList=LitePal.findAll(LoveSong.class);
        adapter=new LoveSongAdapter(loveSongList);

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onClick(int position) {
                LitePal.deleteAll(CurrentSong.class);
                for(int i=0;i<loveSongList.size();i++){
                    CurrentSong cs = new CurrentSong();
                    cs.setSingers(loveSongList.get(i).getSingers());
                    cs.setDuration(loveSongList.get(i).getDuration());
                    cs.setSongmId(loveSongList.get(i).getSongmId());
                    cs.setSongName(loveSongList.get(i).getSongName());
                    cs.setAlbumName(loveSongList.get(i).getAlbumName());
                    cs.setUrl(loveSongList.get(i).getUrl());
                    cs.setAlbummid(loveSongList.get(i).getAlbummid());
                    cs.setPosition(loveSongList.get(i).getPosition());
                    cs.setNeedPay(loveSongList.get(i).isNeedPay());
                    cs.setInterval(loveSongList.get(i).getInterval());
                    cs.save();
                }
                List<CurrentSong> currentSongList=LitePal.findAll(CurrentSong.class);
                CurrentSong cs = currentSongList.get(position);
                playBinder.play(cs);
            }
        });

        adapter.setOnChoiceClickListener(new OnChoiceClickListener() {
            @Override
            public void onClick(int position) {
                Log.i("LoveSongFragment","choice clicked");
                LoveSong loveSong=songList.get(position);
                BottomFragment fragment=new BottomFragment(lsf,loveSong,null,null);
                fragment.show(getFragmentManager(),"fragment");
            }
        });

        LinearLayoutManager llm=new LinearLayoutManager(getActivity());
        llm.setOrientation(RecyclerView.VERTICAL);
        songRecyclerView.setAdapter(adapter);
        songRecyclerView.setLayoutManager(llm);
        Log.i("LoveSongFragment",songList.size()+"  "+"refreshUI");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        EventBus.getDefault().unregister(this);

    }

    public int getStatusBarHeight() {
        int result = 0;
        //获取状态栏高度的资源id
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

}
