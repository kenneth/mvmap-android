package com.mvmap.news.android.request;

import com.android.volley.Request.Method;

public enum HTTPREQ {

	
	NEWS_CATEGORY("http://api.mvmap.com/item/category?version=2", Method.GET),
	NEWS_LIST("http://api.mvmap.com/item", Method.GET),
	NEWS_ITEM("http://api.mvmap.com/item/%s?version=2", Method.GET),
	
	;
	
	
	
	
	public String base;
	public int reqtype;
	private HTTPREQ(String base, int reqtype){
		this.base = base;
		this.reqtype = reqtype;
	}
}
