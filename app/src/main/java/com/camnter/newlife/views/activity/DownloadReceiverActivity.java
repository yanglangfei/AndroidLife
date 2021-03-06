package com.camnter.newlife.views.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.camnter.newlife.R;
import com.camnter.newlife.service.DownloadIntentService;
import com.camnter.newlife.utils.ImageUtil;

/**
 * Description：DownloadReceiverActivity
 * Created by：CaMnter
 * Time：2015-11-22 22:54
 */
public class DownloadReceiverActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String OBJECT_IMAGE_URL = "http://img.blog.csdn.net/20150913233900119";

    private Button downloadBT;
    private ImageView downloadIV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_download_broadcast_receiver);
        this.registerReceiver();
        this.initViews();
        this.initListeners();
    }

    private void registerReceiver() {
        DownloadReceiver downloadReceiver = new DownloadReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadReceiver.INTENT_ACTION);
        this.registerReceiver(downloadReceiver, intentFilter);
    }

    private void initViews() {
        TextView downloadTV = (TextView) this.findViewById(R.id.down_broadcast_image_tv);
        downloadTV.setText(OBJECT_IMAGE_URL);
        this.downloadBT = (Button) this.findViewById(R.id.down_broadcast_start_bt);
        this.downloadIV = (ImageView) this.findViewById(R.id.down_broadcast_image_iv);
    }

    private void initListeners() {
        this.downloadBT.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.down_broadcast_start_bt:
                v.setEnabled(false);
                Intent intent = new Intent(DownloadReceiver.INTENT_ACTION);
                intent.putExtra(DownloadReceiver.INTENT_DATA_IMAGE_URL, OBJECT_IMAGE_URL);
                intent.putExtra(DownloadReceiver.INTENT_TYPE, DownloadReceiver.TYPE_DOWNLOAD_START);
                this.sendBroadcast(intent);
                break;
        }
    }

    /**
     * 下载广播
     */
    public class DownloadReceiver extends BroadcastReceiver {

        public static final String INTENT_ACTION = "com.camnter.android.intent.download";
        public static final String INTENT_TYPE = "type";
        public static final String INTENT_DATA_IMAGE_URL = "image_url";
        public static final String INTENT_DATA_IMAGE_PATH = "image_path";
        public static final int TYPE_DOWNLOAD_START = 2061;
        public static final int TYPE_DOWNLOAD_SUCCESS = 2062;
        public static final int TYPE_DOWNLOAD_FAILURE = 2063;

        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra(INTENT_TYPE, -1);
            if (type == -1) return;
            switch (type) {
                case TYPE_DOWNLOAD_START: {
                    String url = intent.getStringExtra(INTENT_DATA_IMAGE_URL);
                    DownloadIntentService.startActionDownload(context, url);
                    break;
                }
                case TYPE_DOWNLOAD_SUCCESS: {
                    String imageFilePath = intent.getStringExtra(INTENT_DATA_IMAGE_PATH);
                    /**
                     * 设置按钮可用，并隐藏Dialog
                     */
                    DownloadReceiverActivity.this.downloadBT.setEnabled(true);
                    DisplayMetrics metrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(metrics);
                    int screenWidth = metrics.widthPixels;
                    int screenHeight = metrics.heightPixels;
                    /**
                     * ImageUtil.decodeScaleImage 解析图片
                     */
                    Bitmap bitmap = ImageUtil.decodeScaleImage(imageFilePath, screenWidth, screenHeight);
                    DownloadReceiverActivity.this.downloadIV.setImageBitmap(bitmap);

                    /**
                     * 保存图片到相册
                     */
                    String imageName = System.currentTimeMillis() + ".jpg";
                    MediaStore.Images.Media.insertImage(DownloadReceiverActivity.this.getApplicationContext().getContentResolver(), bitmap, imageName, "camnter");
                    Toast.makeText(DownloadReceiverActivity.this, "已保存：" + imageName, Toast.LENGTH_LONG).show();
                    break;
                }
                case TYPE_DOWNLOAD_FAILURE: {
                    DownloadReceiverActivity.this.downloadBT.setEnabled(true);
                    break;
                }
            }
        }

    }

}
