package com.aixinwu.axw.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aixinwu.axw.R;
import com.aixinwu.axw.database.ProductReadDbHelper;
import com.aixinwu.axw.database.ProductReaderContract;
import com.aixinwu.axw.fragment.ShoppingCart;
import com.aixinwu.axw.model.Product;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.simple.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class ProductDetailActivity extends AppCompatActivity
        implements AppBarLayout.OnOffsetChangedListener{

    private static final int QUERY_YES = 0x100;
    private static final int QUERY_NO = 0x101;

    private static boolean ISQUERYED = false;

    private Product entity;
    private int productId;

    private double pastPrice = 1000;

    private WebView mTVDetails;
    private EditText mTVNumber;
    private TextView mTVPopDetails;
    private TextView mTVPrice;
    private TextView mTVTopPrice;
    private TextView mProductCaption;
    private TextView StockNum;
    private TextView pastPriceText;
    private TextView limitTextView;
    private Button mBtnAddToCart;
    private Button mBtnMinute;
    private Button mBtnPlus;

    private View mPop;

    private PopupWindow mPopupWindow;

    private ImageView mImgDetails;
    private ImageView mImgClose;
    private ImageView mImgIcon;

    private Button mBtnOK;

    private int numOfCouldBuy = 0;
    private int originalLimit = 0;

    private Button cartBtn;
    private boolean isPopOpened;

    private int number = 1;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case QUERY_YES:
                    int number = (int) msg.obj;
                    updateDatabase(number);
                    break;
                case QUERY_NO:
                    insert2Sqlite();
                    break;
                case 5566:
                    initDatas();
                    break;
            }
        }
    };


    private SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.product_appbar);
        appBarLayout.addOnOffsetChangedListener(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.product_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CollapsingToolbarLayout layout = (CollapsingToolbarLayout)findViewById(R.id.product_layout);
        layout.setTitleEnabled(false);
        productId = getIntent().getIntExtra("productId",0);

        new Thread(new Runnable() {
            @Override
            public void run() {

                if (GlobalParameterApplication.getLogin_status() == 1)
                    entity = getMyProduct();
                else
                    entity = getProduct();
                Message msg = new Message();
                msg.what=5566;
                handler.sendMessage(msg);
            }
        }).start();

        initViews();

        pastPriceText = (TextView) findViewById(R.id.tv_activity_product_details_past_price);
        limitTextView = (TextView) findViewById(R.id.limit);
        addListeners();
        cartBtn = (Button) findViewById(R.id.btn_activity_product_details_buy_now);
        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent();
                data.putExtra("msg","cart");
                setResult(RESULT_OK, data);
                finish();
                overridePendingTransition(R.anim.scale_fade_in, R.anim.slide_out_bottom);
            }
        });
    }

    private void addListeners() {
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setWindowBehind(false);
                isPopOpened = false;
            }
        });

        mBtnAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPopOpened == false) {
                    mPopupWindow.setAnimationStyle(R.style.PopupWindowStyle);
                    mPopupWindow.showAtLocation(mImgDetails, Gravity.BOTTOM, 0, 0);
                    isPopOpened = true;
                    setWindowBehind(true);
                }
            }
        });

        mBtnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String num = mTVNumber.getText().toString();
                if (GlobalParameterApplication.getLogin_status() == 1){

                    if (num.length() == 0){
                        Toast.makeText(getApplication(),"请输入商品数量",Toast.LENGTH_SHORT).show();
                    }else{
                        int num2 = Integer.valueOf(num);
                        if (num2 == 0){
                            Toast.makeText(getApplication(), "商品数量不能为0", Toast.LENGTH_LONG).show();
                        }else{
                            queryDatabase();
                            if (isPopOpened == true) {
                                mPopupWindow.dismiss();
                            }
                        }
                    }
                }
                else{
                    Toast.makeText(getApplication(), "请先登录", Toast.LENGTH_LONG).show();
                }
            }
        });

        mImgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPopOpened == true) {
                    mPopupWindow.dismiss();
                }
            }
        });

        mBtnPlus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String num = mTVNumber.getText().toString();
                number = Integer.valueOf(num);
                if (number < entity.getStock()) {
                    ++number;
                }
                mTVNumber.setText(number + "");
            }
        });

        mBtnMinute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String num = mTVNumber.getText().toString();
                number = Integer.valueOf(num);
                if (number > 1) {
                    --number;
                    mTVNumber.setText(number + "");
                }
            }
        });

    }


    private void insert2Sqlite() {
        double price = entity.getPrice();
        String id = entity.getId() + "";
        String category = "种类";
        String name = entity.getProduct_name();
        String imgurl = entity.getImage_url();
        int stock = entity.getStock();
        ProductReadDbHelper mDbHelper = new ProductReadDbHelper(getApplicationContext());
        db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ProductReaderContract.ProductEntry.COLUMN_NAME_ENTRY_ID, id);
        values.put(ProductReaderContract.ProductEntry.COLUMN_NAME_PRICE, price + "");
        values.put(ProductReaderContract.ProductEntry.COLUMN_NAME_NAME, name);
        values.put(ProductReaderContract.ProductEntry.COLUMN_NAME_NUMBER, number + "");
        values.put(ProductReaderContract.ProductEntry.COLUMN_NAME_CATEGORY, category);
        values.put(ProductReaderContract.ProductEntry.COLUMN_NAME_IMG, imgurl);
        values.put(ProductReaderContract.ProductEntry.COLUMN_NAME_STOCK, numOfCouldBuy);
        long rawID = -1;

        rawID = db.insert(
                ProductReaderContract.ProductEntry.TABLE_NAME,
                null,
                values);
    }

    private void queryDatabase() {
        ISQUERYED = false;

        ProductReadDbHelper mDbHelper = new ProductReadDbHelper(getApplicationContext());
        db = mDbHelper.getWritableDatabase();

        Cursor c = null;
        try {
            String selection = ProductReaderContract.ProductEntry.COLUMN_NAME_ENTRY_ID + " = ?";
            String[] selectionArgs = new String[]{entity.getId() + ""};//changed on 8.3
            c = db.query(
                    ProductReaderContract.ProductEntry.TABLE_NAME,  // The table to query
//                projection,                               // The columns to return
                    null,
                    selection,                                // The columns for the WHERE clause
                    selectionArgs,                            // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    null                                      // The sort order
            );

            if (c != null) {
                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    String itemId = c.getString(c.getColumnIndex(ProductReaderContract.ProductEntry
                            .COLUMN_NAME_ENTRY_ID));
                    int number = Integer.parseInt(c.getString(c.getColumnIndex(ProductReaderContract
                            .ProductEntry
                            .COLUMN_NAME_NUMBER)));
                    if (itemId.equals(entity.getId() + "")) {
                        ISQUERYED = true;
                        Message message = Message.obtain();
                        message.what = QUERY_YES;
                        message.obj = number;
                        handler.sendMessage(message);
                    }
                }
            }
            if (!ISQUERYED) {
                Message message = Message.obtain();
                message.what = QUERY_NO;
                handler.sendMessage(message);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

    }

    private void updateDatabase(int sqlNumber) {

        if (sqlNumber + number <= numOfCouldBuy){
            sqlNumber += number;

            ContentValues values = new ContentValues();
            values.put(ProductReaderContract.ProductEntry.COLUMN_NAME_NUMBER, sqlNumber + "");
            values.put(ProductReaderContract.ProductEntry.COLUMN_NAME_STOCK, numOfCouldBuy);

            String selection = ProductReaderContract.ProductEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
            String[] selectionArgs = {String.valueOf(entity.getId())};

            int count = db.update(
                    ProductReaderContract.ProductEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs);
            Toast.makeText(getApplication(), "您已经成功添加到购物车~", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(ProductDetailActivity.this,"添加失败，购物车中已有此商品"+sqlNumber+"件，您还可再添加"+(numOfCouldBuy-sqlNumber)+"件商品",Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        mTVDetails = (WebView) findViewById(R.id.tv_activity_product_details_details);
        mBtnAddToCart = (Button) findViewById(R.id.btn_activity_product_details_add_to_cart);
        mImgDetails = (ImageView) findViewById(R.id.img_activity_product);
        mTVTopPrice = (TextView) findViewById(R.id.tv_activity_product_details_price);
        mProductCaption = (TextView) findViewById(R.id.detail_product_name);
        mPop = LayoutInflater.from(this).inflate(R.layout.popup_add_to_cart, null);
        mImgIcon = (ImageView) mPop.findViewById(R.id.img_pop_icon);
        mBtnOK = (Button) mPop.findViewById(R.id.btn_pop_ok);
        mBtnMinute = (Button) mPop.findViewById(R.id.btn_pop_minute);
        mBtnPlus = (Button) mPop.findViewById(R.id.btn_pop_plus);
        mImgClose = (ImageView) mPop.findViewById(R.id.img_pop_close);
        mTVNumber = (EditText) mPop.findViewById(R.id.tv_pop_number);
        mTVPopDetails = (TextView) mPop.findViewById(R.id.tv_pop_details);
        mTVPrice = (TextView) mPop.findViewById(R.id.tv_pop_price);

        mTVNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String num = mTVNumber.getText().toString();
                if (!num.equals("")){
                    int numberTmp = Integer.valueOf(num);
                    if (numberTmp > entity.getStock()){
                        Toast.makeText(ProductDetailActivity.this,"库存只有"+entity.getStock(),Toast.LENGTH_SHORT).show();
                        number = entity.getStock();
                        mTVNumber.setText(String.valueOf(number));
                    }else{
                        if (numberTmp > numOfCouldBuy){
                            Toast.makeText(ProductDetailActivity.this,"限购只剩"+numOfCouldBuy+"件可以购买",Toast.LENGTH_SHORT).show();
                            number = numOfCouldBuy;
                            mTVNumber.setText(String.valueOf(number));
                        }
                        else
                            number = numberTmp;
                    }
                }else
                    number = 1;

            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        mPop.setFocusable(true);
        int PopHeight = getWindow().getAttributes().height;
        mPopupWindow = new PopupWindow(mPop, getWindow().getAttributes().width, PopHeight*2);
        mPopupWindow.setFocusable(true);
        StockNum = (TextView) findViewById(R.id.tv_activity_product_details_stock);
        pastPriceText = (TextView) findViewById(R.id.tv_activity_product_details_past_price);
    }


    private void initDatas() {
        if(entity==null){
            mTVDetails.loadUrl("file:///android_asset/error.html");
            return;
        }
        ImageLoader.getInstance().displayImage(entity.getImage_url(), mImgDetails);
        ImageLoader.getInstance().displayImage(entity.getImage_url(), mImgIcon);

        pastPriceText.setText("爱心币：" + pastPrice);
        if (originalLimit != 0)
            limitTextView.setText("限购："+originalLimit);
        else
            limitTextView.setText("限购：无");

        mProductCaption.setText(entity.getProduct_name());
        mTVDetails.setWebChromeClient(new WebChromeClient());
        mTVDetails.setWebViewClient(new WebViewClient(){
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                view.loadUrl("file:///android_asset/error.html");
            }
        });
        WebSettings webSettings= mTVDetails.getSettings();
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        mTVDetails.loadUrl(entity.getDescriptionUrl());

        mTVTopPrice.setText("爱心币：" + entity.getPrice());
        mTVPrice.setText("爱心币：" + entity.getPrice());
        mTVPopDetails.setText(entity.getShortdescription());
        StockNum.setText("库存： " +(entity.getStock() + ""));
    }

    protected Product getMyProduct(){
        Product dbData = null;
        JSONObject data = new JSONObject();
        data.put("token", GlobalParameterApplication.getToken());
        String jsonstr = data.toJSONString();
        try {
            URL url = new URL(GlobalParameterApplication.getSurl() + "/item_aixinwu_item_get/"+productId);
            HttpURLConnection conn = null;
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.getOutputStream().write(jsonstr.getBytes());
            String ostr = null;
            ostr = IOUtils.toString(conn.getInputStream());
            org.json.JSONObject outjson = null;
            org.json.JSONArray result = null;
            outjson = new org.json.JSONObject(ostr);
            result = outjson.getJSONArray("items");
            String [] imageurl = result.getJSONObject(0).getString("image").split(",");
            String productname = result.getJSONObject(0).getString("name");
            double productprice = result.getJSONObject(0).getDouble("price");
            pastPrice = result.getJSONObject(0).getDouble("original_price");
            int productid = result.getJSONObject(0).getInt("id");
            String descurl = result.getJSONObject(0).getString("desp_url");
            String descdetail = result.getJSONObject(0).getString("desc");
            String shortdesc = result.getJSONObject(0).getString("short_desc");
            String despUrl   = result.getJSONObject(0).getString("desp_url");
            int stock = result.getJSONObject(0).getInt("stock");
            int limit = result.getJSONObject(0).getInt("limit");
            int already_buy = result.getJSONObject(0).getInt("already_buy");
            originalLimit = limit;
            if (limit == 0)
                limit = stock;

            if ( imageurl[0].equals("") ) {
                dbData = new Product(productid,
                        productname,
                        productprice,
                        stock,
                        GlobalParameterApplication.imgSurl+"121000239217360a3d2.jpg",
                        descdetail,
                        shortdesc,
                        despUrl,
                        limit,
                        already_buy
                );
            } else
                dbData = new Product(productid,
                        productname,
                        productprice,
                        stock,
                        GlobalParameterApplication.axwUrl+imageurl[0],
                        descdetail,
                        shortdesc,
                        despUrl,
                        limit,
                        already_buy
                );
            if (limit == stock)
                numOfCouldBuy = stock;
            else
                numOfCouldBuy = (limit - already_buy) > 0 ? (limit-already_buy) : 0;
        }catch (Exception e){
            e.printStackTrace();
        }
        return dbData;
    }

    private Product getProduct(){
        Product dbData = null;
        try {
            URL url = new URL(GlobalParameterApplication.getSurl() + "/item_aixinwu_item_get/"+productId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            java.lang.String ostr;
            org.json.JSONObject outjson = null;

            if (conn.getResponseCode() == 200) {
                InputStream is = conn.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int i;
                while ((i = is.read()) != -1) {
                    baos.write(i);
                }
                ostr = baos.toString();
                org.json.JSONArray result = null;
                outjson = new org.json.JSONObject(ostr);
                result = outjson.getJSONArray("items");

                String jsonall = result.toString();
                String[] imageurl = result.getJSONObject(0).getString("image").split(",");
                String productname = result.getJSONObject(0).getString("name");
                double productprice = result.getJSONObject(0).getDouble("price");
                pastPrice = result.getJSONObject(0).getDouble("original_price");
                int productid = result.getJSONObject(0).getInt("id");
                String descurl = result.getJSONObject(0).getString("desp_url");
                String descdetail = result.getJSONObject(0).getString("desc");
                String shortdesc = result.getJSONObject(0).getString("short_desc");
                String despUrl = result.getJSONObject(0).getString("desp_url");
                int stock = result.getJSONObject(0).getInt("stock");

                if (imageurl[0].equals("")) {
                    dbData = new Product(productid,
                            productname,
                            productprice,
                            stock,
                            GlobalParameterApplication.imgSurl + "121000239217360a3d2.jpg",
                            descdetail,
                            shortdesc,
                            despUrl
                    );
                } else
                    dbData = new Product(productid,
                            productname,
                            productprice,
                            stock,
                            GlobalParameterApplication.axwUrl + imageurl[0],
                            descdetail,
                            shortdesc,
                            despUrl
                    );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dbData;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        int range = appBarLayout.getTotalScrollRange();
        int off = Math.abs(verticalOffset);
        if (off > (range - 100)) {
            mProductCaption.setVisibility(View.VISIBLE);
        } else {
            mProductCaption.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
            default:break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isPopOpened) {
            mPopupWindow.dismiss();
            return;
        } else {
            finish();
            overridePendingTransition(R.anim.scale_fade_in, R.anim.slide_out_bottom);
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isPopOpened) {
            mPopupWindow.dismiss();
            mPopupWindow = null;
        }

        if (db != null && db.isOpen()) {
            db.close();
            db = null;
        }
    }

    private void setWindowBehind(boolean isSetWindowBehind) {
        final WindowManager.LayoutParams lp = getWindow().getAttributes();
        if (isSetWindowBehind) {
            lp.alpha = 0.3f;
        } else {
            lp.alpha = 1.0f;
        }
        getWindow().setAttributes(lp);
    }

}
