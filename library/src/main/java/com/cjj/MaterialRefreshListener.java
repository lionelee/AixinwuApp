package com.cjj;

public abstract class MaterialRefreshListener {
    public void onfinish(){};
    public abstract void onRefresh(MaterialRefreshLayout materialRefreshLayout);
    public void onLoadMore(MaterialRefreshLayout materialRefreshLayout){};
}
