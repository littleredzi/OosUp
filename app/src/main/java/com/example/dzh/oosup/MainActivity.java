package com.example.dzh.oosup;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.bumptech.glide.Glide;
import com.yuyh.library.imgsel.ISNav;
import com.yuyh.library.imgsel.common.ImageLoader;
import com.yuyh.library.imgsel.config.ISListConfig;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn;
    private ImageView img;
    private static final int POSITIVE = 100;
    private Button shangchuan;
    private static final String endpoint = "http://oss-cn-zhangjiakou.aliyuncs.com";
    private static final String callbackAddress = "http://oss-demo.aliyuncs.com:23450";
    private static final String bucket = "";
    private OssService ossService;
    private String picturePath = "";
    private ArrayList<String> pathList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();


        //显示手机照片
        ISNav.getInstance().init(new ImageLoader() {
            @Override
            public void displayImage(Context context, String path, ImageView imageView) {
                Glide.with(context).load(path).into(imageView);
            }
        });

        ossService = initOSS(endpoint, bucket);
        //设置上传的callback地址，目前暂时只支持putObject的回调
        ossService.setCallbackAddress(callbackAddress);
    }

    private void initView() {
        btn = (Button) findViewById(R.id.btn);
        img = (ImageView) findViewById(R.id.img);

        btn.setOnClickListener(this);
        shangchuan = (Button) findViewById(R.id.shangchuan);
        shangchuan.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn:
                ISListConfig config = new ISListConfig.Builder()
                        // 是否多选
                        .multiSelect(true)
                        .btnText("Confirm")
                        // 确定按钮背景色
                        //.btnBgColor(Color.parseColor(""))
                        // 确定按钮文字颜色
                        .btnTextColor(Color.WHITE)
                        // 使用沉浸式状态栏
                        .statusBarColor(Color.parseColor("#3F51B5"))
                        // 返回图标ResId
                        .backResId(R.mipmap.ic_launcher)
                        .title("图片")
                        .titleColor(Color.WHITE)
                        .titleBgColor(Color.parseColor("#3F51B5"))
                        .allImagesText("All Images")
                        .cropSize(1, 1, 200, 200)
                        // 第一个是否显示相机
                        .needCamera(true)
                        // 最大选择图片数量
                        .maxNum(5)
                        .build();

                ISNav.getInstance().toListActivity(this, config, POSITIVE);
                break;
            case R.id.shangchuan:
            //循环
                if (pathList.size()>0){
                    for (int i = 0; i < pathList.size(); i++) {
                        String s = pathList.get(i);
                        ossService.asyncPutImage("小宏子"+i,s);
                    }
                }


                break;
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //回传过来的值 设置到控件上
        if (requestCode == POSITIVE && resultCode == RESULT_OK && data != null) {
            pathList = data.getStringArrayListExtra("result");
            picturePath = pathList.get(0);
            img.setImageURI(Uri.parse(picturePath));


        }
    }

    //初始化一个OssService用来上传下载
    public OssService initOSS(String endpoint, String bucket) {
        //如果希望直接使用accessKey来访问的时候，可以直接使用OSSPlainTextAKSKCredentialProvider来鉴权。
        //OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(accessKeyId, accessKeySecret);

        OSSCredentialProvider credentialProvider;
        //使用自己的获取STSToken的类
        //server地址
        String stsServer = "http://192.168.0.112:7080";
        if (stsServer .equals("")) {
            credentialProvider = new STSGetter();
        }else {
            credentialProvider = new STSGetter(stsServer);
        }

//        bucket = "secretxczf";
        bucket = "picturexczf";
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        OSS oss = new OSSClient(getApplicationContext(), endpoint, credentialProvider, conf);
        return new OssService(oss, bucket,this);

    }

}
