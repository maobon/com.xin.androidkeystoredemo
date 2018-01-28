package com.xin.androidkeystoredemo;

import android.os.Bundle;
import android.security.keystore.KeyProperties;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etUserInput;
    private Button btnEncryptAfterAuthFp, btnDecryptAfterAuthFp;
    private KeyStoreHandler keyStoreHandler;
    private FingerprintHelper fingerprintHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        // 只需KeyStore生成一次SecretKey 首次启动时执行
        if (TextUtils.isEmpty(SharedPreferencesUtil.getInstance(this).getData("IV"))) {
            keyStoreHandler = new KeyStoreHandler();
            keyStoreHandler.generateKey();
        }

        // 指纹帮助类
        fingerprintHelper = new FingerprintHelper(this);
        fingerprintHelper.setAuthResultCallback(new FingerprintHelper.IAuthResult() {
            @Override
            public void authSuccess(String decryptData) {
                Toast.makeText(MainActivity.this, decryptData, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void authFailed() {
                Toast.makeText(MainActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        etUserInput = findViewById(R.id.et_user_input);
        btnEncryptAfterAuthFp = findViewById(R.id.btn_auth_fp_aes_encrypt);
        btnEncryptAfterAuthFp.setOnClickListener(this);
        btnDecryptAfterAuthFp = findViewById(R.id.btn_auth_fp_aes_decrypt);
        btnDecryptAfterAuthFp.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_auth_fp_aes_encrypt:
                // ## 认证指纹 加密数据 ##
                Toast.makeText(this, "Fingerprint authenticating...", Toast.LENGTH_SHORT).show();
                fingerprintHelper.setContent(etUserInput.getText().toString());
                fingerprintHelper.setPurpose(KeyProperties.PURPOSE_ENCRYPT);
                fingerprintHelper.doAuth(null); //
                break;

            case R.id.btn_auth_fp_aes_decrypt:
                // ## 认证指纹 解密数据 ##
                Toast.makeText(this, "Fingerprint authenticating...", Toast.LENGTH_SHORT).show();
                String IV = SharedPreferencesUtil.getInstance(this).getData("IV");
                fingerprintHelper.setPurpose(KeyProperties.PURPOSE_DECRYPT);
                fingerprintHelper.doAuth(ConvertUtil.StringToBytes(IV));
                break;
        }
    }
}
