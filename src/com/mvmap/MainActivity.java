package com.mvmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import com.actionbarsherlock.app.SherlockActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import com.mvmap.adapter.NewsListAdapter;
import com.mvmap.model.NewsItem;

import android.os.Bundle;
import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;


public class MainActivity extends SherlockActivity implements OnItemClickListener {
	private static final int NUM = 20;
	private ListView categoryListView;
	private PullToRefreshListView titleListView;
	private ArrayList<HashMap<String, Object>> menuData = new ArrayList<HashMap<String, Object>>();
	private ArrayList<NewsItem> titleData;// = new ArrayList<NewsItem>();
	private SimpleAdapter mAdapter;
	private NewsListAdapter titleAdapter;
	private int currentCategoryId;
	private ProgressBar mProgressBar;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        

        // configure the SlidingMenu
        SlidingMenu menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setFadeDegree(0.35f);
        menu.setBehindOffset(200);
        menu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        menu.setMenu(R.layout.menu);
        
        mProgressBar = (ProgressBar) findViewById(R.id.pb);
        categoryListView = (ListView) findViewById(R.id.listview);
//        titleListView = (ListView) findViewById(R.id.pull_to_refresh_listview);
         titleListView = (PullToRefreshListView) findViewById(R.id.pull_to_refresh_listview);
        titleListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                // Do work to refresh the list here.
                requestList(currentCategoryId);
            }
        });
        
        categoryListView.setOnItemClickListener(this);
        titleListView.setOnItemClickListener(this);  
        
        
        if (categoryListView == null) {
        	System.out.println("list view is null");
        } else {
        	System.out.println("get list view");
        }
        
        
        FinalHttp http = new FinalHttp();
        // 取分类
        http.get("http://api.mvmap.com/item/category", new AjaxCallBack<String> () {
        	@Override
        	public void onStart() {
        		System.out.println("====> start");
        	}
        	
        	@Override
        	public void onSuccess(String t) {
//        		System.out.println("=====> success t : " + t);
        		Gson g = new Gson();
        		Map<integer, String> map = g.fromJson(t, new TypeToken<Map<Integer, String>>() {}.getType());
        		System.out.println("====>" + map);
        		
        		Iterator iter = map.entrySet().iterator(); 
        		while (iter.hasNext()) { 
        			HashMap<String, Object> category = new HashMap<String, Object>();
        		    Map.Entry entry = (Map.Entry) iter.next(); 
        		    int  key = (Integer) entry.getKey(); 
        		    String val = (String) entry.getValue(); 
        		    category.put("cat_id", key);
        		    category.put("title", val);
        		    menuData.add(category);
        		} 
        		
        		mAdapter = new SimpleAdapter(MainActivity.this, menuData, R.layout.menu_item, new String[]{ "title"}, new int[]{R.id.txt_title });
                categoryListView.setAdapter(mAdapter);
                
                requestList(menuData.get(0).get("cat_id"));
        	}
        	
        	@Override
        	public void onFailure(Throwable t, int errorNo, String strMsg) {
        		System.out.println("failure");
        	}
        });
    }
    
    /*
     * 取分类对应的新闻列表
     */
    private void requestList(Object cat_id) {
    	mProgressBar.setVisibility(View.VISIBLE);
    	
    	currentCategoryId = (Integer) cat_id;
    	categoryListView.setSelection(0);
    	
    	FinalHttp http = new FinalHttp();
        // 取分类
        http.get("http://api.mvmap.com/item?cat_id=" + cat_id + "&num=" + NUM, new AjaxCallBack<String> () {
        	@Override
        	public void onStart() {
        		System.out.println("====> start");
        	}
        	
        	@Override
        	public void onSuccess(String t) {
        		mProgressBar.setVisibility(View.INVISIBLE);
//        		System.out.println("=====> success t : " + t);
        		Gson g = new Gson();
        		titleData = g.fromJson(t, new TypeToken<ArrayList<NewsItem>>() {}.getType());
//        		System.out.println("====>" + newsArray);
        		for (int i = 0; i < titleData.size(); i++) {
        			NewsItem item = titleData.get(i);
        			System.out.println("标题:" + item.title);
        			System.out.println("分类id:" + item.cat_id);
        			System.out.println("id:" + item.id);
        			System.out.println("----------------------");
        		}
        		
        		titleAdapter = new NewsListAdapter(MainActivity.this, titleData, titleListView);
        		titleListView.setAdapter(titleAdapter);
        		
        		titleListView.onRefreshComplete();
        	}
        	
        	@Override
        	public void onFailure(Throwable t, int errorNo, String strMsg) {
        		System.out.println("failure");
        	}
        });
    }
    
    

    
     

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (arg0 == categoryListView) {
			// 点击菜单中的分类时
			
			HashMap<String, Object> da = menuData.get(arg2);
			System.out.println("cat_id : " + da.get("cat_id") + "  title: " + da.get("title"));
			
			requestList(da.get("cat_id"));
		} else {
			// 点击列表中的某条新闻时
			
			FinalHttp http = new FinalHttp();
			System.out.println("site:" + "http://api.mvmap.com/item/" + titleData.get(arg2).id);
	        // 取分类
	        http.get("http://api.mvmap.com/item/" + titleData.get(arg2).id, new AjaxCallBack<String> () {
	        	@Override
	        	public void onStart() {
	        		System.out.println("====> start ");
	        	}
	        	
	        	@Override
	        	public void onSuccess(String t) {
//	        		System.out.println("=====> success t : " + t);
	        		Gson g = new Gson();
	        		ArrayList<Map<String, String>> newsArray = g.fromJson(t, new TypeToken<ArrayList<Map<String, String>>>() {}.getType());
//	        		System.out.println("====>" + newsArray);
	        		System.out.println("content : " + newsArray.get(0).get("content"));
	        		
	        		Intent intent = new Intent(MainActivity.this, DetailActivity.class);
	        		intent.putExtra("content", newsArray.get(0).get("content"));
	        		intent.putExtra("title", newsArray.get(0).get("title"));
	        		MainActivity.this.startActivity(intent);
	        	}
	        	
	        	@Override
	        	public void onFailure(Throwable t, int errorNo, String strMsg) {
	        		System.out.println("failure");
	        	}
	        });
		}
		
	}
    
}