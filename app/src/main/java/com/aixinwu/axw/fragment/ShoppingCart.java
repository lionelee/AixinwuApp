package com.aixinwu.axw.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aixinwu.axw.R;
import com.aixinwu.axw.activity.ConfirmOrder;
import com.aixinwu.axw.activity.LoginActivity;
import com.aixinwu.axw.activity.MainActivity;
import com.aixinwu.axw.database.ProductReadDbHelper;
import com.aixinwu.axw.database.ProductReaderContract;

import java.util.ArrayList;

import com.aixinwu.axw.model.ShoppingCartEntity;
import com.aixinwu.axw.tools.GlobalParameterApplication;
import com.aixinwu.axw.widget.RecyclerViewDivider;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yanzhenjie.recyclerview.swipe.SwipeItemLongClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

public class ShoppingCart extends Fragment {

    private static final int MSG_WHAT = 0x223;
    private static final int MSG_NUM = 233;
    private static final int MSG_TOTAL = 456;
    private SwipeMenuRecyclerView mListView;
    private static Activity mContext = MainActivity.mActivity;

    public static ArrayList<String> CheckedProductId = new ArrayList<>();
    public ArrayList<ShoppingCartEntity> orderedDatas = new ArrayList<>();

    private ListPopupWindow popupWindow;
    private int idx = 0;

    /* 结算 */
    private Button mBtnChecking;
    private static TextView mTVTotal;
    private static CheckBox mCheckBox;
    private TextView mTVCheck;

    /**
     * 合计
     */
    private double mTotalMoney = 0;
    private int mTotalChecked = 0;


    private ArrayList<ShoppingCartEntity> mDatas = new ArrayList<>();
    private ProductReadDbHelper mDbHelper;
    private static ShoppingCartAdapter mAdapter;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case MSG_WHAT:
                    mAdapter = new ShoppingCartAdapter(getActivity());
                    mListView.setAdapter(mAdapter);
                    addListeners();
                    break;
                case MSG_NUM:
                    for(int i = 0; i < mDatas.size(); ++i){
                        ShoppingCartEntity spc = mDatas.get(i);
                        if(spc.getId().equals(msg.getData().getString("id"))) {
                            if (msg.getData().getInt("op") == 1)
                                spc.setNumber(spc.getNumber() + 1);
                            else if (msg.getData().getInt("op") == 2)
                                spc.setNumber(spc.getNumber() - 1);
                            mDatas.set(i, spc);
                            break;
                        }
                    }

                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if(preferences.getBoolean("first",true) && mDatas.size()>0){
                new AlertDialog.Builder(getActivity()).setTitle("提示").setMessage("左滑或长按删除购物车中物品")
                        .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                preferences.edit().putBoolean("first",false).commit();
                            }
                        }).setCancelable(false).show();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shopping_cart,null);
        mDbHelper = new ProductReadDbHelper(getActivity());
        initViews(view);
        popupWindow = new ListPopupWindow(getActivity());
        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, new String[]{"删除此项","清空购物车"});
        popupWindow.setAdapter(adapter);
        int w = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,140,getResources().getDisplayMetrics());
        popupWindow.setWidth(w);
        popupWindow.setModal(true);
        popupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        String id = mDatas.get(idx).getId();
                        deleteFromDatabase(id);
                        removeCheck(id);
                        mDatas.remove(idx);
                        mAdapter.notifyItemRemoved(idx);
                        break;
                    case 1:
                        deleteAllFromDatabase();
                        CheckedProductId.clear();
                        mCheckBox.setChecked(false);
                        mAdapter.notifyItemRangeRemoved(0, mDatas.size());
                        mDatas.clear();
                        break;
                    default: break;
                }
                popupWindow.dismiss();
                new CalTotalTask().execute();
            }
        });

        return view;
    }

    //从数据库中获取商品总数和总价并更新
    class CalTotalTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            ProductReadDbHelper mDbHelper = new ProductReadDbHelper(getActivity());
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            double totalprice = 0;
            for (int i = 0; i < CheckedProductId.size(); ++i){
                String checkedid = CheckedProductId.get(i);
                Cursor cursor = db.query(ProductReaderContract.ProductEntry.TABLE_NAME,
                        new String[]{ProductReaderContract.ProductEntry.COLUMN_NAME_PRICE, ProductReaderContract.ProductEntry.COLUMN_NAME_NUMBER},
                        ProductReaderContract.ProductEntry.COLUMN_NAME_ENTRY_ID + "=?", new String[]{checkedid},
                        null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    double price = Double.parseDouble(cursor.getString(cursor.getColumnIndex
                            (ProductReaderContract.ProductEntry.COLUMN_NAME_PRICE)));

                    int number = Integer.parseInt(cursor.getString(cursor.getColumnIndex
                            (ProductReaderContract.ProductEntry.COLUMN_NAME_NUMBER)));

                    totalprice += price * number;
                }
                if(cursor!=null)
                    cursor.close();
            }
            mTotalChecked = CheckedProductId.size();
            mTotalMoney = totalprice;
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mTVTotal.setText("合计：" + mTotalMoney + "爱心币");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        initDatas();
    }

    private void initViews(View view) {
        mListView = (SwipeMenuRecyclerView)view.findViewById(R.id.lv_shopping_cart_activity);
        mListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        if(Build.VERSION.SDK_INT >= 23){
            mListView.addItemDecoration(new RecyclerViewDivider(getActivity(),R.drawable.list_divider));
        }else{
            mListView.addItemDecoration(new RecyclerViewDivider(getActivity()));
        }
        mListView.setSwipeMenuCreator(new SwipeMenuCreator() {
            @Override
            public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(getContext())
                        .setBackground(R.color.accent)
                        .setImage(R.drawable.ic_delete)
                        .setHeight(ViewGroup.LayoutParams.MATCH_PARENT)
                        .setWidth(getResources().getDimensionPixelSize(R.dimen.swipemenu_width));
                swipeRightMenu.addMenuItem(deleteItem);
            }
        });
        mListView.setSwipeMenuItemClickListener(new SwipeMenuItemClickListener() {
            @Override
            public void onItemClick(SwipeMenuBridge menuBridge) {
                menuBridge.closeMenu();
                idx = menuBridge.getAdapterPosition();
                Log.e(">>>>>","swipepress pos:"+idx);
                if (menuBridge.getDirection() == SwipeMenuRecyclerView.RIGHT_DIRECTION) {
                    String id = mDatas.get(idx).getId();
                    deleteFromDatabase(id);
                    removeCheck(id);
                    mDatas.remove(idx);
                    mAdapter.notifyItemRemoved(idx);
                    new CalTotalTask().execute();
                }
            }
        });
        mListView.setSwipeItemLongClickListener(new SwipeItemLongClickListener() {
            @Override
            public void onItemLongClick(View itemView, int position) {
                idx = position;
                Log.e(">>>>>","longpress pos:"+position);
                View v = itemView.findViewById(R.id.love_coin);
                popupWindow.setAnchorView(v);
                popupWindow.setDropDownGravity(Gravity.TOP|Gravity.LEFT);
                popupWindow.show();
            }
        });

        mBtnChecking = (Button) view.findViewById(R.id.btn_activity_shopping_cart_clearing);
        mTVTotal = (TextView) view.findViewById(R.id.tv_activity_shopping_cart_total);
        mCheckBox = (CheckBox) view.findViewById(R.id.cb_activity_shopping_cart);
        mTVCheck = (TextView) view.findViewById(R.id.tv_activity_shopping_cart);

        // 最终结算
        mBtnChecking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GlobalParameterApplication.getLogin_status() == 1 ) {
                    if ( GlobalParameterApplication.whtherBindJC == 1){
                        if (orderedDatas.size() > 0){
                            Intent intent = new Intent(getActivity(), ConfirmOrder.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("size", orderedDatas.size());
                            bundle.putSerializable("mTotalMoney", mTotalMoney);
                            for (int i1 = 0; i1 < orderedDatas.size(); ++i1) {
                                bundle.putSerializable("OrderedData" + i1, orderedDatas.get(i1));
                                bundle.putSerializable("CheckedProductId" + i1, CheckedProductId.get(i1));
                            }
                            orderedDatas.clear();
                            CheckedProductId.clear();
                            mCheckBox.setChecked(false);
                            intent.putExtras(bundle);
                            startActivityForResult(intent,0);
                            getActivity().overridePendingTransition(R.anim.slide_in_bottom,R.anim.scale_fade_out);
                        }else{
                            Toast.makeText(getActivity(),"请选择商品",Toast.LENGTH_SHORT).show();
                        }

                    }
                    else{
                        Toast.makeText(getActivity(),"请先绑定Jaccount",Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivityForResult(intent,0);
                    getActivity().overridePendingTransition(R.anim.slide_in_bottom,R.anim.scale_fade_out);
                }
            }
        });
    }

    private void initDatas() {
//初始化购物车数据
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        final Cursor cursor = db.query(ProductReaderContract.ProductEntry.TABLE_NAME, null, null, null, null, null, null);

        mDatas.clear();

        new Thread() {
            @Override
            public void run() {
                if (cursor != null) {
                    for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                        String id = cursor.getString(cursor.getColumnIndex
                                (ProductReaderContract.ProductEntry
                                        .COLUMN_NAME_ENTRY_ID));

                        String name = cursor.getString(cursor.getColumnIndex
                                (ProductReaderContract.ProductEntry
                                        .COLUMN_NAME_NAME));

                        String category = cursor.getString(cursor.getColumnIndex
                                (ProductReaderContract.ProductEntry
                                        .COLUMN_NAME_CATEGORY));

                        double price = Double.parseDouble(cursor.getString(cursor.getColumnIndex
                                (ProductReaderContract.ProductEntry.COLUMN_NAME_PRICE)));

                        int number = Integer.parseInt(cursor.getString(cursor.getColumnIndex
                                (ProductReaderContract.ProductEntry.COLUMN_NAME_NUMBER)));

                        String imgurl = cursor.getString(cursor.getColumnIndex(
                                ProductReaderContract.ProductEntry.COLUMN_NAME_IMG));

                        int stock = Integer.parseInt(cursor.getString(cursor.getColumnIndex
                                (ProductReaderContract.ProductEntry.COLUMN_NAME_STOCK)));
                        ShoppingCartEntity entity = new ShoppingCartEntity(id, name, category,
                                price, number, imgurl, stock);


                        mDatas.add(entity);
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    if (db != null && db.isOpen()) {
                        db.close();
                    }
                    Message message = Message.obtain();
                    message.what = MSG_WHAT;
                    mHandler.sendMessage(message);
                }
            }
        }.start();

        mTVTotal.setText("合计：0.0爱心币");
    }

    //在数据库中删除物品id 为id的操作
    private static void deleteFromDatabase (String id) {
        ProductReadDbHelper mDbHelper = new ProductReadDbHelper(mContext);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(ProductReaderContract.ProductEntry.TABLE_NAME,
                ProductReaderContract.ProductEntry.COLUMN_NAME_ENTRY_ID + "=?",
                new String[]{id}
        );
    }

    //清空购物车的数据库操作
    private void deleteAllFromDatabase () {
        ProductReadDbHelper mDbHelper = new ProductReadDbHelper(getActivity());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + ProductReaderContract.ProductEntry.TABLE_NAME);
    }

    //结算listerner
    private void addListeners() {
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mTotalChecked = 0;
                    mTotalMoney = 0;
                    orderedDatas.clear();
                    CheckedProductId.clear();
                    for (int i = 0; i < mDatas.size(); ++i) {
                        CheckedProductId.add(mDatas.get(i).getId());
                        orderedDatas.add(mDatas.get(i));
                    }
                } else {
                    CheckedProductId.clear();
                    orderedDatas.clear();
                }
                mAdapter.notifyDataSetChanged();
            }
        });
        mTVCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCheckBox.isChecked()) mCheckBox.setChecked(false);
                else mCheckBox.setChecked(true);
            }
        });
    }

    private void removeCheck(String id){
        int size = CheckedProductId.size();
        for(int i = 0; i < size; ++i){
            if(CheckedProductId.get(i).equals(id)){
                CheckedProductId.remove(i);
                orderedDatas.remove(i);
                if(CheckedProductId.size() == 0)
                    mCheckBox.setChecked(false);
                return;
            }
        }
    }

    //购物车列表adapter
    private class ShoppingCartAdapter extends RecyclerView.Adapter<ShoppingCartAdapter.ViewHolder>{

        private LayoutInflater mInflater;

        public ShoppingCartAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.item_shopping_cart, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bindData(position);
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            CheckBox cb;
            ImageView img, iv_more;
            TextView name, category, price, number;
            Button add_amount, reduce_amount;

            public ViewHolder(View itemView) {
                super(itemView);
                cb = (CheckBox) itemView.findViewById(R.id.cb_item_shopping_cart);
                name = (TextView) itemView.findViewById(R.id.tv_item_shopping_cart_name);
                category = (TextView) itemView.findViewById(R.id
                        .tv_item_shopping_cart_category);
                price = (TextView) itemView.findViewById(R.id
                        .tv_item_shopping_cart_price);
                number = (TextView) itemView.findViewById(R.id
                        .tv_item_shopping_cart_number);
                img = (ImageView) itemView.findViewById(R.id
                        .img_item_shopping_cart_number);
                iv_more = (ImageView) itemView.findViewById(R.id.iv_more);
                add_amount = (Button) itemView.findViewById(R.id.shopping_cart_add);
                reduce_amount = (Button) itemView.findViewById(R.id.shopping_cart_minus);
            }

            public void bindData(final int pos){
                final ShoppingCartEntity entity = mDatas.get(pos);
                category.setText(entity.getCategory());
                name.setText(entity.getName());
                price.setText(String.valueOf(entity.getPrice()));
                number.setText(String.valueOf(entity.getNumber()));
                ImageLoader.getInstance().displayImage(entity.getImgUrl(), img);

                iv_more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        idx = getAdapterPosition();
                        Log.e(">>>>>","moreclick pos:"+pos);
                        popupWindow.setAnchorView(view);
                        popupWindow.setDropDownGravity(Gravity.TOP|Gravity.RIGHT);
                        popupWindow.show();
                    }
                });

                add_amount.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                 new Thread() {
                                     @Override
                                     public void run(){
                                         ProductReadDbHelper mDbHelper = new ProductReadDbHelper(getActivity());
                                         SQLiteDatabase db = mDbHelper.getWritableDatabase();
                                         ContentValues cv = new ContentValues();
                                         int amount = entity.getNumber();
                                         if(amount < entity.getStock()) {
                                             amount++;

                                             cv.put(ProductReaderContract.ProductEntry.COLUMN_NAME_NUMBER, amount);
                                             db.update(ProductReaderContract.ProductEntry.TABLE_NAME, cv,
                                                     ProductReaderContract.ProductEntry.COLUMN_NAME_ENTRY_ID + "=?",
                                                     new String[]{entity.getId()});
                                             Message msg = new Message();
                                             msg.what = MSG_NUM;
                                             Bundle bundle = new Bundle();
                                             bundle.putString("id", entity.getId());
                                             bundle.putInt("op", 1);
                                             msg.setData(bundle);
                                             mHandler.sendMessage(msg);
                                             new CalTotalTask().execute();
                                         }else{
                                             getActivity().runOnUiThread(new Runnable() {
                                                 @Override
                                                 public void run() {
                                                     Toast.makeText(getActivity(),"数量不能超过库存",Toast.LENGTH_SHORT).show();
                                                 }
                                             });
                                         }
                                     }
                                 }.start();
                            }
                        }
                );

                reduce_amount.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new Thread() {
                                    @Override
                                    public void run(){
                                        ProductReadDbHelper mDbHelper = new ProductReadDbHelper(getActivity());
                                        SQLiteDatabase db = mDbHelper.getWritableDatabase();
                                        ContentValues cv = new ContentValues();
                                        int amount = entity.getNumber();
                                        Log.i("Amount", amount + "");
                                        if (amount > 1) {
                                            amount--;
                                            cv.put(ProductReaderContract.ProductEntry.COLUMN_NAME_NUMBER, amount);
                                            db.update(ProductReaderContract.ProductEntry.TABLE_NAME, cv,
                                                    ProductReaderContract.ProductEntry.COLUMN_NAME_ENTRY_ID + "=?",
                                                    new String[]{entity.getId()});
                                            Message msg = new Message();
                                            msg.what = MSG_NUM;
                                            Bundle bundle = new Bundle();
                                            bundle.putString("id", entity.getId());
                                            bundle.putInt("op", 2);
                                            msg.setData(bundle);
                                            mHandler.sendMessage(msg);
                                            new CalTotalTask().execute();
                                        }
                                    }
                                }.start();
                            }
                        }
                );

                cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        String itemid = entity.getId();

                        if (isChecked) {
                            if (!CheckedProductId.contains(itemid)) {
                                CheckedProductId.add(itemid);
                                orderedDatas.add(entity);
                                if(orderedDatas.size() == mDatas.size()){
                                    mCheckBox.setChecked(true);
                                }
                            }
                        } else {
                            if (CheckedProductId.contains(itemid)) {
                                CheckedProductId.remove(CheckedProductId.indexOf(itemid));
                                orderedDatas.remove(orderedDatas.indexOf(entity));
                                if(orderedDatas.size() == 0)
                                    mCheckBox.setChecked(false);
                            }

                        }
                        new CalTotalTask().execute();
                    }
                });

                if (CheckedProductId.contains(entity.getId())){
                    cb.setChecked(true);
                }
                else{
                    cb.setChecked(false);
                }
            }
        }
    }

}
