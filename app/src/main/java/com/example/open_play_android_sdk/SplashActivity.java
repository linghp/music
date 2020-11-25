package com.example.open_play_android_sdk;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.hifive.sdk.hInterface.DataResponse;
import com.hifive.sdk.manager.HFLiveApi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SplashActivity extends AppCompatActivity {
    private EditText et_appid,et_secretkey;
    private EditText et_member_name,et_member_id,et_sociaty_name,et_sociaty_id;
    private AppCompatButton btn_initialize,btn_login;
    private String secretKey, appId ;
    private String memberName,memberId,sociatyName,sociatyId;
    private boolean flag;//是否初始化
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        initView();
    }

    private void initView() {
        et_appid = findViewById(R.id.et_appid);
        appId = (String) SPUtils.get(this,SPUtils.appId,"1998ca60c18a42b38fa03b80cce1832a");
        et_appid.setText(appId);

        et_secretkey = findViewById(R.id.et_secretkey);
        secretKey = (String) SPUtils.get(this,SPUtils.secretKey,"259e23ea0c684bd7be");
        et_secretkey.setText(secretKey);

        et_member_name = findViewById(R.id.et_member_name);
        memberName = (String) SPUtils.get(this,SPUtils.memberName,"");
        et_member_name.setText(memberName);

        et_member_id = findViewById(R.id.et_member_id);
        memberId = (String) SPUtils.get(this,SPUtils.memberId,"");
        et_member_id.setText(memberId);

        et_sociaty_name = findViewById(R.id.et_sociaty_name);
       /* sociatyName = (String) SPUtils.get(this,SPUtils.sociatyName,"");
        et_sociaty_name.setText(sociatyName);*/

        et_sociaty_id = findViewById(R.id.et_sociaty_id);
       /* sociatyId = (String) SPUtils.get(this,SPUtils.sociatyId,"");
        et_sociaty_id.setText(sociatyId);*/

        btn_initialize = findViewById(R.id.btn_initialize);
        btn_initialize.setOnClickListener(view -> {
            secretKey = et_secretkey.getText().toString().trim();
            appId = et_appid.getText().toString().trim();
            if(TextUtils.isEmpty(secretKey)){
                Toast.makeText(SplashActivity.this,"请输入secretKey",Toast.LENGTH_SHORT).show();
                return;
            }
            if(TextUtils.isEmpty(appId)){
                Toast.makeText(SplashActivity.this,"请输入appId",Toast.LENGTH_SHORT).show();
                return;
            }
            HFLiveApi.Companion.registerApp(getApplication(), appId, secretKey);
            Toast.makeText(SplashActivity.this,"初始化SDK成功",Toast.LENGTH_SHORT).show();
            SPUtils.put(this,SPUtils.appId,appId);
            SPUtils.put(this,SPUtils.secretKey,secretKey);
            flag = true;
        });
        btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(view -> {
            if(!flag){
                Toast.makeText(SplashActivity.this,"请先初始化SDK",Toast.LENGTH_SHORT).show();
            }else{
                Login();
            }
        });
    }
    private void Login() {
        memberName = et_member_name.getText().toString().trim();
        memberId = et_member_id.getText().toString().trim();
        sociatyName = et_sociaty_name.getText().toString().trim();
        sociatyId = et_sociaty_id.getText().toString().trim();
        if(TextUtils.isEmpty(memberName)){
            Toast.makeText(SplashActivity.this,"请输入会员名称",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(memberId)){
            Toast.makeText(SplashActivity.this,"请输入会员id",Toast.LENGTH_SHORT).show();
            return;
        }
        if(!TextUtils.isEmpty(sociatyName) && !TextUtils.isEmpty(sociatyId)){
            HFLiveApi.Companion.getInstance().societyLogin(this, sociatyName, sociatyId, new DataResponse() {
                @Override
                public void errorMsg(String string, Integer code) {
                    SplashActivity.this.runOnUiThread(() -> Toast.makeText(SplashActivity.this, string, Toast.LENGTH_SHORT).show());
                }

                @Override
                public void data(Object any) {
                    SplashActivity.this.runOnUiThread(() -> {
                        SPUtils.put(SplashActivity.this,SPUtils.sociatyName,sociatyName);
                        SPUtils.put(SplashActivity.this,SPUtils.sociatyId,sociatyId);
                        Toast.makeText(SplashActivity.this, "公会登录成功", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SplashActivity.this,MainActivity.class));
                    });
                }
            });
        }else{
            HFLiveApi.Companion.getInstance().memberLogin(this, memberName, memberId, "", "",
                    null , null, null, null, null, null, new DataResponse() {
                        @Override
                        public void errorMsg(String string, Integer code) {
                            SplashActivity.this.runOnUiThread(() -> Toast.makeText(SplashActivity.this, string, Toast.LENGTH_SHORT).show());
                        }

                        @Override
                        public void data(Object any) {
                            SplashActivity.this.runOnUiThread(() -> {
                                SPUtils.put(SplashActivity.this,SPUtils.memberName,memberName);
                                SPUtils.put(SplashActivity.this,SPUtils.memberId,memberId);
                                Toast.makeText(SplashActivity.this, "会员登录成功", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SplashActivity.this,MainActivity.class));
                                finish();
                            });
                        }
                    });
        }


    }
}
