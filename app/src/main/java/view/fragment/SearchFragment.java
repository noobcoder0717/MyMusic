package view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.mvpmymusic.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import view.MainActivity;

public class SearchFragment extends Fragment {


    @Bind(R.id.toolbar_searchview)
    Toolbar toolbar;

    SearchView mSearchView;

    @Override
    public void onCreate(Bundle savedInstanceState){super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.fragment_search,container,false);
        ButterKnife.bind(this,view);
        setHasOptionsMenu(true);
        AppCompatActivity activity=(AppCompatActivity)getActivity();
        activity.setSupportActionBar(toolbar);
        ActionBar actionBar=activity.getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        return view;
    }




    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.menu_searchfragment,menu);

        MenuItem searchItem=menu.findItem(R.id.search);
        mSearchView=(SearchView) searchItem.getActionView();
        mSearchView.setQueryHint("请输入要查找的歌曲/歌手/专辑名");
        mSearchView.setIconified(false);//搜索栏默认展开，自动弹出输入法
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//                Intent intent=new Intent(getContext(), SearchResultActivity.class);
//                intent.putExtra("query",query);
//                startActivity(intent);
                MainActivity mainActivity=(MainActivity)getActivity();
                SearchResultFragment srf=new SearchResultFragment(query);
                mainActivity.setSearchResultFragment(srf);
                mainActivity.addFragment(srf);
                mainActivity.showFragment(mainActivity.getFRAGMENTNUMBERS());
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case android.R.id.home:
                MainActivity activity=(MainActivity)getActivity();
                activity.pop();
                break;
            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}
