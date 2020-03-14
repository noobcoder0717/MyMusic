package view.fragment;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mvpmymusic.R;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import view.MainActivity;

public class MainFragment extends Fragment {

    private OnClick onSearchButtonClick;

//    @Bind(R.id.my)
//    TextView my;
//
//    @Bind(R.id.find)
//    TextView find;
//
//    @Bind(R.id.cloudvillage)
//    TextView cloudvillage;
//
//    @Bind(R.id.video)
//    TextView video;

    @Bind(R.id.toolbar_mainwindow)
    Toolbar toolbar;

    @Bind(R.id.drawerlayout)
    DrawerLayout mDrawerLayout;

    @Bind(R.id.recent_play)
    LinearLayout recentPlay;

    @Bind(R.id.love_list)
    LinearLayout lovelist;


    public interface OnClick{
        public void onClick(MenuItem item);
    }
    public void setOnClick(OnClick onSearchButtonClick){
        this.onSearchButtonClick=onSearchButtonClick;
    }
    public OnClick getOnButtonClick(){
        return onSearchButtonClick;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.fragment_main,container,false);
        ButterKnife.bind(this,view);

        recentPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity activity1=(MainActivity)getActivity();
                RecentPlayFragment rpf=new RecentPlayFragment();
                activity1.setRecentPlayFragment(rpf);
                activity1.addFragment(rpf);
                activity1.showFragment(activity1.getFRAGMENTNUMBERS());
            }
        });

        lovelist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity activity1=(MainActivity)getActivity();
                LoveSongFragment lsf=new LoveSongFragment();
                activity1.setLoveSongFragment(lsf);
                activity1.addFragment(lsf);
                activity1.showFragment(activity1.getFRAGMENTNUMBERS());
            }
        });

        setHasOptionsMenu(true);
        AppCompatActivity activity=(AppCompatActivity)getActivity();
        activity.setSupportActionBar(toolbar);
        ActionBar actionbar=activity.getSupportActionBar();
        if(actionbar!=null){
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.menu);//设置点击弹出drawerlayout的按钮
        }
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.menu_mainwindow,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.search:
                MainActivity activity1=(MainActivity)getActivity();
                SearchFragment sf=new SearchFragment();
                activity1.setSearchFragment(sf);
                activity1.addFragment(sf);
                activity1.showFragment(activity1.getFRAGMENTNUMBERS());
                break;
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }



}

