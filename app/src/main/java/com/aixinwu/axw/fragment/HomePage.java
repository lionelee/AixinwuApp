package com.aixinwu.axw.fragment;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.aixinwu.axw.Adapter.ProductAdapter;
import com.aixinwu.axw.Adapter.VolunteerAdapter;
import com.aixinwu.axw.R;
import com.aixinwu.axw.activity.MainActivity;
import com.aixinwu.axw.activity.ProductDetailActivity;
import com.aixinwu.axw.activity.ProductListActivity;
import com.aixinwu.axw.activity.VolActivityList;
import com.aixinwu.axw.activity.VolunteerApply;
import com.aixinwu.axw.model.Product;
import com.aixinwu.axw.model.VolunteerActivity;
import com.aixinwu.axw.tools.ADInfo;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.aixinwu.axw.tools.NetInfo;
import com.aixinwu.axw.tools.ViewFactory;
import com.aixinwu.axw.view.BaseViewPager;
import com.aixinwu.axw.view.CycleViewPager;
import com.aixinwu.axw.view.MyGridView;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.simple.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liangyuding on 2016/4/6.
 */
public class HomePage extends CycleViewPager{
    private View view;

    private List<ImageView> views = new ArrayList<ImageView>();
    private List<ADInfo> infos = new ArrayList<ADInfo>();
    private String[] imageUrls = {
        	/*"http://aixinwu.sjtu.edu.cn/images/slider/p0.jpg",
        	"http://aixinwu.sjtu.edu.cn/images/slider/p1.jpg",
    		"http://aixinwu.sjtu.edu.cn/images/slider/p2.jpg",*/
            "http://aixinwu.sjtu.edu.cn/images/welcome/main0.jpg",
            "http://aixinwu.sjtu.edu.cn/images/welcome/main1.jpg",
            "http://aixinwu.sjtu.edu.cn/images/welcome/main2.jpg"};

    private List<Product> productList = new ArrayList<Product>();
    private List<Product> leaseList = new ArrayList<Product>();
    private List<VolunteerActivity> volList = new ArrayList<VolunteerActivity>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        view = inflater.inflate(R.layout.home_page, container, false);
        mActivity = getActivity();
        //继承自父类
        rollPictureLayout = (RelativeLayout)view.findViewById(R.id.rollPicture);
        viewPager = (BaseViewPager) view.findViewById(R.id.viewPager);
        indicatorLayout = (LinearLayout) view
                .findViewById(R.id.layout_viewpager_indicator);
        setViewPagerScrollSpeed(1000);//设置滑动速度
        init();
        initialize();

        mThread.start();
        return view;
    }

    private void initialize() {
        for(int i = 0; i < imageUrls.length; i ++){
            ADInfo info = new ADInfo();
            info.setUrl(imageUrls[i]);
            info.setContent("ͼƬ-->" + i );
            infos.add(info);
        }

        views.add(ViewFactory.getImageView(getActivity(), infos.get(infos.size() - 1).getUrl()));
        for (int i = 0; i < infos.size(); i++) {
            views.add(ViewFactory.getImageView(getActivity(), infos.get(i).getUrl()));
        }
        views.add(ViewFactory.getImageView(getActivity(), infos.get(0).getUrl()));
        setCycle(true);
        setData(views, infos, mAdCycleViewListener);
        setWheel(true);
        setTime(2000);
        setIndicatorCenter();
    }

    private CycleViewPager.ImageCycleViewListener mAdCycleViewListener = new CycleViewPager.ImageCycleViewListener() {

        @Override
        public void onImageClick(ADInfo info, int position, View imageView) {
            if (isCycle()) {
                position = position - 1;
            }
        }

    };

    @Override
    public void onStart () {

        RelativeLayout productlist = (RelativeLayout) getActivity().findViewById(R.id.exchange_more);
        productlist.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ProductListActivity.class);
                intent.putExtra("type", "exchange");
                getActivity().startActivity(intent);
            }
        });

        RelativeLayout leaselist = (RelativeLayout) getActivity().findViewById(R.id.lend_more_more);
        leaselist.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ProductListActivity.class);
                String s = "rent";
                intent.putExtra("type", s);
                getActivity().startActivity(intent);
            }
        });

        RelativeLayout vollist = (RelativeLayout) getActivity().findViewById(R.id.vol_more_more);
        vollist.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), VolActivityList.class);
                getActivity().startActivity(intent);
            }
        });
        super.onStart();
    }

    public Thread mThread = new Thread(){
        @Override
        public void run(){
            super.run();
            productList = new ArrayList<> (getDbData("exchange"));
            leaseList =  new ArrayList<> (getDbData("rent"));
            volList =  new ArrayList<> (getVolunteer());
            Message msg = new Message();
            msg.what=1321;
            nHandler.sendMessage(msg);
        }

    };

    public Handler nHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1321:
                    ProductAdapter adapter1 = new ProductAdapter(
                            getActivity(),
                            R.layout.product_item,
                            productList
                    );
                    ProductAdapter adapter2 = new ProductAdapter(
                            getActivity(),
                            R.layout.product_item,
                            leaseList
                    );
                    VolunteerAdapter adapter3 = new VolunteerAdapter(
                            getActivity(),
                            R.layout.volunteer_item,
                            volList
                    );
                    MyGridView gridView1 = (MyGridView) getActivity().findViewById(R.id.grid1);
                    gridView1.setAdapter (adapter1);
                    gridView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            Product product = productList.get(i);
                            Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("productId", product.getId());
                            intent.putExtras(bundle);
                            getActivity().startActivity(intent);
                        }
                    });
                    MyGridView gridView2 = (MyGridView) getActivity().findViewById(R.id.grid2);
                    gridView2.setAdapter(adapter2);
                    gridView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            Product product = leaseList.get(i);
                            Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("productId", product.getId());
                            intent.putExtras(bundle);
                            getActivity().startActivity(intent);
                        }
                    });
                    MyGridView gridView3 = (MyGridView) getActivity().findViewById(R.id.grid3);
                    gridView3.setAdapter(adapter3);
                    gridView3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            VolunteerActivity product = volList.get(i);
                            Intent intent = new Intent(getActivity(), VolunteerApply.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("volActivityId", product);
                            intent.putExtras(bundle);
                            getActivity().startActivity(intent);
                        }
                    });
            }
        }
    };

    static public List<Product> getDbData(String type){
        List<Product> dbData = new ArrayList<Product>();
        String MyToken= GlobalParameterApplication.getToken();
        String surl = GlobalParameterApplication.getSurl();
        JSONObject itemsrequest = new JSONObject();
        String typestr = "";
        int start = 0;
        itemsrequest.put("startAt", start);
        itemsrequest.put("length", 100);
        switch (type) {
            case "exchange":
                typestr = "置换";
                break;
            case "rent":
                typestr = "租赁";
                break;
            case "cash":
                typestr = "现金";
                break;
        }

        itemsrequest.put("type", typestr);

        try {
            URL url = new URL(surl + "/item_aixinwu_item_get_list");
            Log.i("HomePage","getconnection");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            conn.getOutputStream().write(itemsrequest.toJSONString().getBytes());
            java.lang.String ostr = IOUtils.toString(conn.getInputStream());
            org.json.JSONObject outjson = null;

                JSONArray result = null;
                outjson = new org.json.JSONObject(ostr);
                result = outjson.getJSONArray("items");
                start += result.length();
                for (int i = 0; i < result.length(); i++){
                    String jsonall = result.getJSONObject(i).toString();
                    Log.i("JSONALL", jsonall);
                    String [] imageurl = result.getJSONObject(i).getString("image").split(",");
                    String logname = result.getJSONObject(i).getString("name");
                    String value = result.getJSONObject(i).getString("price") + "";
                    String iid = result.getJSONObject(i).getInt("id") + "";
                    String descurl = result.getJSONObject(i).getString("desp_url");
                    String descdetail = result.getJSONObject(i).getString("desc");
                    String shortdesc = result.getJSONObject(i).getString("short_desc");
                    String despUrl   = result.getJSONObject(i).getString("desp_url");
                    int stock = result.getJSONObject(i).getInt("stock");
                    if ( imageurl[0].equals("") ) {
                        dbData.add(new Product(result.getJSONObject(i).getInt("id"),
                                result.getJSONObject(i).getString("name"),
                                result.getJSONObject(i).getDouble("price"),
                                stock,
                                GlobalParameterApplication.imgSurl+"121000239217360a3d2.jpg",
                                descdetail,
                                shortdesc,
                                despUrl
                        ));
                    } else
                        dbData.add(new Product(result.getJSONObject(i).getInt("id"),
                                result.getJSONObject(i).getString("name"),
                                result.getJSONObject(i).getDouble("price"),
                                stock,
                                GlobalParameterApplication.axwUrl+imageurl[0],
                                descdetail,
                                shortdesc,
                                despUrl
                        ));
                }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dbData;
    }

    static public List<VolunteerActivity> getVolunteer(){
        List<VolunteerActivity> dbData = new ArrayList<VolunteerActivity>();
        String MyToken= GlobalParameterApplication.getToken();
        String surl = GlobalParameterApplication.getSurl();
        JSONObject itemsrequest = new JSONObject();
        itemsrequest.put("token",MyToken);

        try {
            URL url = new URL(surl + "/aixinwu_volunteer_act_get");
            Log.i("LoveCoin","getconnection");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            conn.getOutputStream().write(itemsrequest.toJSONString().getBytes());
            java.lang.String ostr = IOUtils.toString(conn.getInputStream());

            JSONArray outjson = null;
            outjson = new org.json.JSONArray(ostr);
            System.out.println(ostr);
            for (int i1 = 0; i1 < outjson.length(); ++i1){
                int need = outjson.getJSONObject(i1).getInt("num_needed");
                int signed = outjson.getJSONObject(i1).getInt("num_signed");
                dbData.add(new VolunteerActivity(outjson.getJSONObject(i1).getInt("id"),
                        outjson.getJSONObject(i1).getString("image"),
                        outjson.getJSONObject(i1).getString("name"),
                        outjson.getJSONObject(i1).getDouble("pay_cash"),
                        outjson.getJSONObject(i1).getString("work_date"),
                        need,
                        signed,
                        outjson.getJSONObject(i1).getDouble("workload"),
                        outjson.getJSONObject(i1).getString("site"),
                        outjson.getJSONObject(i1).getInt("joined"),
                        outjson.getJSONObject(i1).getString("about"),
                        outjson.getJSONObject(i1).getString("content")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dbData;
    }
}
