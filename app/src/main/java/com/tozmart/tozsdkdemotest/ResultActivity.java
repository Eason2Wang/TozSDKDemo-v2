package com.tozmart.tozsdkdemotest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.tozmart.tozsdkdemotest.utils.PhotoHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ResultActivity extends AppCompatActivity {

    @BindView(R.id.front_view)
    ImageView frontView;
    @BindView(R.id.side_view)
    ImageView sideView;
    @BindView(R.id.info_view)
    TextView infoView;

    private String info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            info = bundle.getString("info");
        }

        frontView.setImageBitmap(PhotoHolder.getFrontBitmap());
        sideView.setImageBitmap(PhotoHolder.getSideBitmap());
        infoView.setText(info);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PhotoHolder.recycle();
    }
}
