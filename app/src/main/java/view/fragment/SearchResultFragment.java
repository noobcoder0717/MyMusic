package view.fragment;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mvpmymusic.R;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import adpater.SingerAdapter;
import adpater.SongAdapter;
import bean.CurrentSong;
import bean.OnlineSong;
import bean.SaveSong;
import bean.SongUrlSorted;
import butterknife.Bind;
import butterknife.ButterKnife;
import callback.OnItemClickListener;
import presenter.SearchResultPresenter;
import service.PlayService;
import view.ISearchResultView;
import view.MainActivity;

import static java.lang.Thread.sleep;

public class SearchResultFragment extends Fragment implements ISearchResultView {
    @Bind(R.id.toolbar_searchresultview)
    Toolbar toolbar;

    @Bind(R.id.singer_recyclerview)
    RecyclerView singerRecyclerView;

    @Bind(R.id.song_recyclerview)
    RecyclerView songRecyclerView;


    SearchResultPresenter searchResultPresenter;//presenter
    PlayService.PlayBinder playBinder;

    String query;//要查询的歌曲/歌手/专辑
    ProgressDialog progressDialog;//显示加载中的进度框

    SingerAdapter singerAdapter;
    SongAdapter songAdapter;

    //singer adapter
    List<String> singers=new ArrayList<>();

    //song adapter
    List<String> songnameList=new ArrayList<>();
    List<List<String>> singerList=new ArrayList<>();//对应每首单曲的歌手
    List<String> albumList=new ArrayList<>();
    List<String> songmidList=new ArrayList<>();
    List<String> albummidList=new ArrayList<>();
    List<SongUrlSorted> songUrlList=new ArrayList<>();
    List<Integer> intervalList=new ArrayList<>();

    //记录SaveSong信息（时长）
    List<Integer> songDuration=new ArrayList<>();
    List<SaveSong> saveSongList=new ArrayList<>();


    private ServiceConnection playConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            playBinder=(PlayService.PlayBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    public SearchResultFragment(String query){
        this.query=query;
    }


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent playIntent = new Intent(getActivity(),PlayService.class);
        getActivity().bindService(playIntent,playConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.fragment_searchresult,container,false);
        ButterKnife.bind(this,view);
        searchResultPresenter=new SearchResultPresenter(this);
        AppCompatActivity activity=(AppCompatActivity)getActivity();
        setHasOptionsMenu(true);//使toolbar的返回按钮起作用
        activity.setSupportActionBar(toolbar);
        ActionBar actionBar=activity.getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);//自带返回按钮
        }
        singerAdapter=new SingerAdapter(getContext(),singers);
        songAdapter=new SongAdapter(getContext(),songnameList,singerList,albumList,songmidList,songUrlList);

        showDialog();
        loadSongData();
        return view;
    }

    public void loadSongData(){
        searchResultPresenter.getResult(query);
    }

    @Override
    public void showResult(){

        singers=searchResultPresenter.loadSingers();
        songnameList=searchResultPresenter.loadSongnameList();
        singerList=searchResultPresenter.loadSingerList();
        songmidList=searchResultPresenter.loadSongmidList();
        albumList=searchResultPresenter.loadAlbumList();
        songUrlList=searchResultPresenter.loadSongUrlList();
        songDuration=searchResultPresenter.loadSongDuration();
        albummidList=searchResultPresenter.loadAlbummidList();
        intervalList=searchResultPresenter.loadIntervalList();

        LitePal.deleteAll(SaveSong.class);

        Collections.sort(songUrlList, new Comparator<SongUrlSorted>() {
            @Override
            public int compare(SongUrlSorted s1, SongUrlSorted s2) {
                if(s1.getIdx()<s2.getIdx())
                    return -1;
                else if(s1.getIdx()>s2.getIdx())
                    return 1;
                else
                    return 0;
            }
        });

        //将搜索得到的歌曲信息保存下来
        for(int i=0;i<songnameList.size();i++){
            SaveSong ss = new SaveSong();
            ss.setSingers(singerList.get(i));
            ss.setDuration(songDuration.get(i));
            ss.setSongmId(songmidList.get(i));
            ss.setSongName(songnameList.get(i));
            ss.setAlbumName(albumList.get(i));
            ss.setUrl(songUrlList.get(i).getUrl());
            ss.setAlbummid(albummidList.get(i));
            ss.setPosition(i);
            ss.setNeedPay(songUrlList.get(i).isNeedPay());
            ss.setInterval(intervalList.get(i));
            ss.save();
        }

        saveSongList = LitePal.findAll(SaveSong.class);
        singerAdapter=new SingerAdapter(getContext(),singers);

        songAdapter=new SongAdapter(getContext(),songnameList,singerList,albumList,songmidList,songUrlList);
        songAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onClick(int position) {//点击播放歌曲
                LitePal.deleteAll(CurrentSong.class);
                for(int i=0;i<songnameList.size();i++){
                    CurrentSong cs = new CurrentSong();
                    cs.setSingers(singerList.get(i));
                    cs.setDuration(songDuration.get(i));
                    cs.setSongmId(songmidList.get(i));
                    cs.setSongName(songnameList.get(i));
                    cs.setAlbumName(albumList.get(i));
                    cs.setUrl(songUrlList.get(i).getUrl());
                    cs.setAlbummid(albummidList.get(i));
                    cs.setPosition(i);
                    cs.setNeedPay(songUrlList.get(i).isNeedPay());
                    cs.setInterval(intervalList.get(i));
                    cs.save();
                }
                List<CurrentSong> currentSongList=LitePal.findAll(CurrentSong.class);
                CurrentSong cs = currentSongList.get(position);
                playBinder.play(cs);
            }
        });

        LinearLayoutManager llm=new LinearLayoutManager(getActivity());
        llm.setOrientation(RecyclerView.HORIZONTAL);

        singerRecyclerView.setLayoutManager(llm);
        singerRecyclerView.setAdapter(singerAdapter);

        songRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        songRecyclerView.setAdapter(songAdapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                MainActivity activity=(MainActivity)getActivity();
                activity.pop();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showDialog(){
        progressDialog=new ProgressDialog(getContext());
        progressDialog.setTitle("加载中...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    @Override
    public void hideDialog(){
        progressDialog.dismiss();
    }

    @Override
    public void showFailure(){
        hideDialog();
        Toast.makeText(getContext(),"网络状态不可用，请检查网络连接",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        getActivity().unbindService(playConnection);
    }
}

