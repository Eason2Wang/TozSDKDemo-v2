package com.tozmart.tozsdkdemotest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.tozmart.toz_sdk.beans.CustomerInfo;
import com.tozmart.toz_sdk.listener.OnProcessListener;
import com.tozmart.toz_sdk.listener.OnSensorListener;
import com.tozmart.toz_sdk.main.TozSDK;
import com.tozmart.toz_sdk.retrofit.beans.ImageProcessErrorWarnInfo;
import com.tozmart.toz_sdk.retrofit.beans.ImageProcessResponseBean;
import com.tozmart.toz_sdk.retrofit.imageprocess.ImageProcess;
import com.tozmart.toz_sdk.retrofit.imageprocess.LoadDataCallBack;
import com.tozmart.toz_sdk.widge.TozCameraView;
import com.tozmart.toz_sdk.widge.TozSensorTipView;
import com.tozmart.tozsdkdemotest.utils.DisplayUtil;
import com.tozmart.tozsdkdemotest.utils.PhotoHolder;
import com.tozmart.tozsdkdemotest.utils.PickPhotoFromGallery;
import com.tozmart.tozsdkdemotest.utils.ShowToast;
import com.tozmart.tozsdkdemotest.utils.StatusBarUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.camera)
    TozCameraView cameraView;
    @BindView(R.id.take_photo)
    Button takePhotoBtn;
    @BindView(R.id.bottom_layout)
    LinearLayout bottomLayout;
    @BindView(R.id.switch_btn)
    Switch switchBtn;
    @BindView(R.id.grid_off)
    Switch offGrid;
    @BindView(R.id.select_photo)
    Button selectPhotoBtn;
    @BindView(R.id.sensorView)
    TozSensorTipView sensorView;

    /**
     * 当前需要拍照量体的用户的信息
     */
    private CustomerInfo customerInfo = new CustomerInfo();

    private boolean isFront = true;
    private boolean showBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initUserInfo();
        TozSDK.init(this, customerInfo);
        initCameraView();
        // 拍照
        takePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.process();
            }
        });
        // 切换背景图像，可选择人像或者鞋子
        switchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showBody = isChecked;
                cameraView.switchTipMask(true, showBody);
            }
        });
        // 关闭或者打开边界线和背景图像
        offGrid.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!cameraView.isShowingLineView()) {
                    cameraView.showLineView();
                } else {
                    cameraView.hideLineView();
                }
            }
        });
        // 从相册中挑选照片，返回结果在onActivityResult中
        selectPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PickPhotoFromGallery.pickImage(MainActivity.this);
            }
        });
        // 监听传感器，判断手机的姿势是否符合拍照要求
        sensorView.setOnSensorListener(new OnSensorListener() {
            @Override
            public void onSensorOk() {
                takePhotoBtn.setEnabled(true);
                if (isFront) {
                    takePhotoBtn.setText("拍摄正面");
                } else {
                    takePhotoBtn.setText("拍摄侧面");
                }
            }

            @Override
            public void onSensorError() {
                takePhotoBtn.setEnabled(false);
                takePhotoBtn.setText("无法拍照");
            }
        });
    }

    /**
     * 初始化用户参数
     */
    private void initUserInfo(){
        customerInfo.setCorId("o4x04hy3zljghf7oa4");
        customerInfo.setUserId("10000");
        customerInfo.setUserName("test");
        customerInfo.setUserHeight(180.f);
        customerInfo.setUserWeight(75.f);
        customerInfo.setUserGender("1");
    }

    /**
     * 初始化相机界面参数
     */
    private void initCameraView() {
        ViewGroup.LayoutParams cameraLayoutParams = cameraView.getLayoutParams();
        cameraLayoutParams.height = DisplayUtil.getRealScreenMetrics(this).y;
        cameraView.setLayoutParams(cameraLayoutParams);
        // 设置边界线的上下比例
        cameraView.setLinePositionRatio(0.15f, 0.95f);
        bottomLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (!StatusBarUtils.isNavigationBarShow(MainActivity.this)){
                    cameraView.setTranslationY(-bottomLayout.getHeight());
                } else {
                    int barHeight = StatusBarUtils.getNavigationBarOffsetPx(MainActivity.this);
                    cameraView.setTranslationY(-bottomLayout.getHeight() - barHeight);
                }
            }
        });

        // 添加拍照监听
        cameraView.setOnProcessListener(new OnProcessListener() {
            @Override
            public void onPhotoTaken(Bitmap bitmap, boolean front) {
                // bitmap是拍照获得图片；front为true表示返回正面拍摄的照片，false表示返回侧面拍摄的照片
                if (front) {
                    PhotoHolder.setFrontBitmap(bitmap);
                } else {
                    PhotoHolder.setSideBitmap(bitmap);
                }
            }
            @Override
            public void onProcessSuccess(ImageProcessResponseBean response) {
                String info = "";
                if (response.getErrNum() > 0) {
                    for (ImageProcessErrorWarnInfo error: response.getErrInfo()) {
                        info += error.getIntro_cns() + "\n";
                    }
                } else {
                    info += "量体成功";
                }
                Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                intent.putExtra("info", info);
                startActivity(intent);
            }
            @Override
            public void onProcessFailed(String msg, int code) {
                ShowToast.showToast("量体失败");
                PhotoHolder.recycle();
            }
        });
    }

    /**
     * 开始处理图片
     * @param bitmap
     */
    private void startProcess(Bitmap bitmap) {
        if (isFront) {
            PhotoHolder.setFrontBitmap(bitmap);
            isFront = false;
            cameraView.switchTipMask(false, showBody);
        } else {
            PhotoHolder.setSideBitmap(bitmap);
            isFront = true;
            cameraView.switchTipMask(true, showBody);
            ImageProcess.process(PhotoHolder.getFrontBitmap(),
                    PhotoHolder.getSideBitmap(), new LoadDataCallBack() {
                        @Override
                        public void onResponseSuccess(ImageProcessResponseBean response) {
                            String info = "";
                            if (response.getErrNum() > 0) {
                                for (ImageProcessErrorWarnInfo error: response.getErrInfo()) {
                                    info += error.getIntro_cns() + "\n";
                                }
                            } else {
                                info += "量体成功";
                            }
                            Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                            intent.putExtra("info", info);
                            startActivity(intent);
                        }

                        @Override
                        public void onResponseFailed(String msg, int code) {
                            ShowToast.showToast(msg);
                            PhotoHolder.recycle();
                        }

                        @Override
                        public void onNullResponse() {
                        }
                    }, true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        if (requestCode == PickPhotoFromGallery.REQUEST_PICK && resultCode == RESULT_OK) {
            try {
                startProcess(MediaStore.Images.Media.getBitmap(this.getContentResolver(), result.getData()));
            } catch (Exception e) {
                e.printStackTrace();
                ShowToast.showToast(e.getMessage());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 打开相机
        cameraView.start();
        // 注册传感器监听
        sensorView.registerSensor();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 关闭相机
        cameraView.stop();
        // 取消传感器监听
        sensorView.unregisterSensor();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 回收内存
        cameraView.destroyView();
        PhotoHolder.recycle();
    }
}
