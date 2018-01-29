package com.aixinwu.axw.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.aixinwu.axw.R;
import com.aixinwu.axw.adapter.ImageFolderAdapter;
import com.aixinwu.axw.adapter.PhotoPickAdapter;
import com.aixinwu.axw.model.FolderInfo;
import com.aixinwu.axw.model.PhotoInfo;
import com.aixinwu.axw.tools.DownloadTask;
import com.aixinwu.axw.tools.OnRecyclerItemClickListener;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by lionel on 2018/1/18.
 */

public class PhotoPicker extends AppCompatActivity {

    public static int SINGLE_SELECT = 0;
    public static int MULTI_SELECT = 1;
    public static int TAKE_PHOTO = 404;
    public static int PICK_PHOTO = 302;
    public static int CROP_PHOTO = 500;

    private LinearLayout ll_select, ll_loading, ll_overlay;
    private TextView tv_title, tv_done;
    private ImageView iv_arrow;
    private RecyclerView recyclerView;
    private ListPopupWindow popupWindow;
    private ImageFolderAdapter folderAdapter;
    private PhotoPickAdapter pickAdapter;
    private PhotoPickAdapter.OnCheckedChangeListener listener;

    private static final int LOADER_ALL = 0;
    private static final int LOADER_CATEGORY = 1;
    private boolean hasFolderScan = false;

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback;
    private List<FolderInfo> folders = new ArrayList<>();
    private ArrayList<String> photos;

    private Toolbar toolbar;
    private int mode =  SINGLE_SELECT;
    private int maxPopH;
    private String campath;
    private boolean flag = false;

    private OnRecyclerItemClickListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_picker);

        maxPopH = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 320, getResources().getDisplayMetrics());
        mode = getIntent().getIntExtra("mode", SINGLE_SELECT);
        if(getIntent().getStringArrayListExtra("selectImg") == null){
            photos = new ArrayList<>();
        }else photos = getIntent().getStringArrayListExtra("selectImg");

        toolbar = (Toolbar) findViewById(R.id.picker_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ll_select = (LinearLayout) findViewById(R.id.ll_select);
        ll_loading = (LinearLayout) findViewById(R.id.loading);
        ll_overlay = (LinearLayout) findViewById(R.id.overlay);
        tv_title = (TextView) findViewById(R.id.tv_title);
        iv_arrow = (ImageView) findViewById(R.id.iv_arrow);
        tv_done = (TextView) findViewById(R.id.tv_done);
        recyclerView = (RecyclerView)findViewById(R.id.photo_gallery);
        recyclerView.setLayoutManager(new GridLayoutManager(PhotoPicker.this, 3));
        pickAdapter = new PhotoPickAdapter(PhotoPicker.this, mode);
        listener = new PhotoPickAdapter.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean b, String path) {
                if(b)photos.add(path);
                else photos.remove(path);
                tv_done.setText("完成("+String.valueOf(photos.size())+"/8)");
                if(photos.size() < 8){
                    flag = false;
                }else{
                    flag = true;
                }
                pickAdapter.setFlag(flag);
                if(recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE && !recyclerView.isComputingLayout())
                    pickAdapter.notifyDataSetChanged();

            }
        };
        pickAdapter.setListener(listener);
        recyclerView.setAdapter(pickAdapter);
        if(mode == MULTI_SELECT){
            pickAdapter.setSelects(photos);
            tv_done.setVisibility(View.VISIBLE);
            tv_done.setText("完成("+String.valueOf(photos.size())+"/8)");
            tv_done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent data = new Intent();
                    data.putStringArrayListExtra("selected",photos);
                    setResult(RESULT_OK,data);
                    finish();
                    overridePendingTransition(R.anim.scale_fade_in,R.anim.slide_out_right);
                }
            });
        }
        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(PhotoPicker.this, WRITE_EXTERNAL_STORAGE)){
            readPhotos();
        } else {
            if(Build.VERSION.SDK_INT >= 23) {
                ActivityCompat.requestPermissions(PhotoPicker.this,new String[]{READ_EXTERNAL_STORAGE}, 5698);
            }
        }
        if(mode == SINGLE_SELECT){
            mListener = new OnRecyclerItemClickListener(recyclerView) {
                @Override
                public void onItemClick(RecyclerView.ViewHolder vh) {
                    if(vh instanceof PhotoPickAdapter.CamVHolder){
                        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(PhotoPicker.this, CAMERA)){
                            takeImg();
                        } else {
                            if(Build.VERSION.SDK_INT >= 23) {
                                ActivityCompat.requestPermissions(PhotoPicker.this,new String[]{CAMERA}, 5798);
                            }
                        }
                    }else{
                        campath = ((PhotoPickAdapter.ImgVHolder)vh).getPath();
                        cropImg(campath,100,100);
                    }
                }
            };
        }else{
            //TODO MULTISELECT LISTENER
            mListener = new OnRecyclerItemClickListener(recyclerView) {
                @Override
                public void onItemClick(RecyclerView.ViewHolder vh) {
                    if(vh instanceof PhotoPickAdapter.CamVHolder){
                        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(PhotoPicker.this, CAMERA)){
                            takeImg();
                        } else {
                            if(Build.VERSION.SDK_INT >= 23) {
                                ActivityCompat.requestPermissions(PhotoPicker.this,new String[]{CAMERA}, 5798);
                            }
                        }
                    }
                }
            };
        }
        recyclerView.addOnItemTouchListener(mListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
        case 5698:
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readPhotos();
            } else onBackPressed();
            break;
        case 5798:
            if (grantResults.length > 0){
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                takeImg();
                else Toast.makeText(PhotoPicker.this,"请授予拍照权限",Toast.LENGTH_SHORT).show();
            }
            break;
        default:break;
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
        finish();
        overridePendingTransition(R.anim.scale_fade_in,R.anim.slide_out_right);
    }

    private void initView(){
        pickAdapter.setList(folders.get(0).getPhotoInfoList());
        pickAdapter.notifyDataSetChanged();

        recyclerView.setVisibility(View.VISIBLE);
        ll_select.setVisibility(View.VISIBLE);
        ll_loading.setVisibility(View.GONE);
        folderAdapter = new ImageFolderAdapter(PhotoPicker.this, folders);
        popupWindow = new ListPopupWindow(PhotoPicker.this);
        popupWindow.setModal(true);
        popupWindow.setAnchorView(toolbar);
        popupWindow.setAdapter(folderAdapter);
        int h =(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 52, getResources().getDisplayMetrics());
        if(h*folderAdapter.getCount() <= maxPopH){
            popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        }else{
            popupWindow.setHeight(maxPopH);
        }
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                iv_arrow.setImageResource(R.drawable.ic_tool_arrow_down);
                ll_overlay.setVisibility(View.GONE);
            }
        });
        popupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                folderAdapter.setSelected(i);
                tv_title.setText(folders.get(i).getName());
                selectFolder(i);
                popupWindow.dismiss();
            }
        });

        ll_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_arrow.setImageResource(R.drawable.ic_tool_arrow_up);
                ll_overlay.setVisibility(View.VISIBLE);
                popupWindow.show();
            }
        });
    }

    private void selectFolder(int idx){
        pickAdapter.clear();
        pickAdapter.setList(folders.get(idx).getPhotoInfoList());
        pickAdapter.notifyDataSetChanged();
    }

    private int findFolder(String name){
        int size = folders.size();
        for(int i = 0; i < size; ++i){
            if(folders.get(i).getName().equals(name))return i;
        }
        return -1;
    }

    private void readPhotos() {
        final FolderInfo infoAll = new FolderInfo("全部照片","");
        folders.add(infoAll);
        mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

            private final String[] IMAGE_PROJECTION = {
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.SIZE
            };

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                if (id == LOADER_ALL) {
                    return new CursorLoader(PhotoPicker.this, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, null, null, IMAGE_PROJECTION[2] + " DESC");
                } else if (id == LOADER_CATEGORY) {
                    return new CursorLoader(PhotoPicker.this, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, IMAGE_PROJECTION[0] + " like '%" + args.getString("path") + "%'", null, IMAGE_PROJECTION[2] + " DESC");
                }

                return null;
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                if (data != null) {
                    int count = data.getCount();
                    if (count > 0) {
                        List<PhotoInfo> tempPhotoList = new ArrayList<>();
                        data.moveToFirst();
                        do {
                            String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                            String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                            int size = data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[4]));
                            boolean showFlag = size > 1024 * 5;                           //是否大于5K
                            PhotoInfo photoInfo = new PhotoInfo(path, name);
                            if (showFlag) {
                                tempPhotoList.add(photoInfo);
                            }
                            if (!hasFolderScan && showFlag) {
                                File photoFile = new File(path);
                                File folderFile = photoFile.getParentFile();
                                int idx = findFolder(folderFile.getName());
                                if (idx < 0) {
                                    List<PhotoInfo> photoInfoList = new ArrayList<>();
                                    photoInfoList.add(photoInfo);
                                    FolderInfo folderInfo = new FolderInfo(folderFile.getName(), folderFile.getAbsolutePath());
                                    folderInfo.photoInfoList = photoInfoList;
                                    folders.add(folderInfo);
                                } else {
                                    FolderInfo f = folders.get(idx);
                                    f.photoInfoList.add(photoInfo);
                                }
                            }

                        } while (data.moveToNext());
                        if(infoAll.getPhotoInfoList() == null){
                            infoAll.setPhotoInfoList(tempPhotoList);
                        }
                        hasFolderScan = true;
                    }
                }
                initView();
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        };
        getSupportLoaderManager().restartLoader(LOADER_ALL, null, mLoaderCallback);   // 扫描手机中的图片
    }

    private void takeImg() {
        if(flag) return;
        String subname = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File outDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        File file = new File(outDir,"aixinwu_"+subname+".jpg");
        campath = file.getAbsolutePath();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Uri uri;
        if(Build.VERSION.SDK_INT<24){
            uri = Uri.fromFile(file);
        }else{
            ContentValues contentValues = new ContentValues(1);
            contentValues.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
            uri = getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
        }
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, TAKE_PHOTO);
        overridePendingTransition(R.anim.slide_in_right,R.anim.scale_fade_out);
    }

    private void cropImg(String sourceFilePath, float aspectRatioX, float aspectRatioY) {
        Uri uri = Uri.fromFile(new File(sourceFilePath));
        UCrop uCrop = UCrop.of(uri, uri);
        UCrop.Options options = new UCrop.Options();
        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);
        options.setHideBottomControls(true);
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        options.setToolbarColor(getResources().getColor(typedValue.resourceId));
        options.setStatusBarColor(getResources().getColor(typedValue.resourceId));
        options.setFreeStyleCropEnabled(false);
        uCrop.withOptions(options);
        uCrop.withAspectRatio(aspectRatioX, aspectRatioY);
        uCrop.start(PhotoPicker.this, CROP_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == TAKE_PHOTO){
            if(resultCode == RESULT_OK){
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                File file = new File(campath);
                intent.setData(Uri.fromFile(file));
                PhotoPicker.this.sendBroadcast(intent);
                if(mode == SINGLE_SELECT){
                    cropImg(campath,100,100);
                }else{
                    Intent i = new Intent();
                    photos.add(campath);
                    i.putStringArrayListExtra("selected",photos);
                    setResult(RESULT_OK,i);
                    finish();
                    overridePendingTransition(R.anim.scale_fade_in,R.anim.slide_out_right);
                }
            }else if(resultCode == RESULT_CANCELED){
                File file = new File(campath);
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                if (file.exists()) {
                    file.delete();
                    Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    ContentResolver mContentResolver = PhotoPicker.this.getContentResolver();
                    String where = MediaStore.Images.Media.DATA + "='" + campath + "'";
                    mContentResolver.delete(uri, where, null);
                    intent.setData(Uri.fromFile(file));
                    PhotoPicker.this.sendBroadcast(intent);
                }
            }
        }else if(requestCode == CROP_PHOTO){
            if(resultCode == RESULT_OK){
                Intent i = new Intent();
                i.putExtra("path", campath);
                setResult(RESULT_OK, i);
                finish();
                overridePendingTransition(R.anim.scale_fade_in,R.anim.slide_out_right);
            }
        }
    }
}
