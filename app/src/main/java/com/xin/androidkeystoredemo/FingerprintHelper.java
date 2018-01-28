package com.xin.androidkeystoredemo;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.security.keystore.KeyProperties;
import android.widget.Toast;

import javax.crypto.Cipher;

/**
 * Created by xin on 28/01/2018.
 * 指纹认证帮助类
 */

public class FingerprintHelper extends FingerprintManager.AuthenticationCallback {

    private Context context;
    private FingerprintManager fingerprintManager;
    private KeyStoreHandler keyStoreHandler;

    // 目的
    private int purpose;
    // 指纹认证成功后的callback
    private IAuthResult authResultCallback;

    // 待加密内容
    private String content;

    // Constructor
    public FingerprintHelper(Context context) {
        this.context = context;
        this.fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        this.keyStoreHandler = new KeyStoreHandler();
    }

    public void setPurpose(int purpose) {
        this.purpose = purpose;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAuthResultCallback(IAuthResult authResultCallback) {
        this.authResultCallback = authResultCallback;
    }

    /**
     * 执行指纹认证
     *
     * @param IV 解密时所需的初始化向量 加密时传null
     */
    public void doAuth(byte[] IV) {
        // 首先需要构造 FingerprintManager.CryptoObject
        FingerprintManager.CryptoObject cryptoObj = null;

        switch (purpose) {
            case KeyProperties.PURPOSE_ENCRYPT:
                // 用于加密
                cryptoObj = keyStoreHandler.createCryptoObj(Cipher.ENCRYPT_MODE, null);
                break;

            case KeyProperties.PURPOSE_DECRYPT:
                // 用于解密
                cryptoObj = keyStoreHandler.createCryptoObj(Cipher.DECRYPT_MODE, IV);
                break;
        }

        // 进行指纹认证操作时传入秘钥模组
        fingerprintManager.authenticate(cryptoObj, new CancellationSignal(), 0, this, null);
    }

    /**
     * 指纹认证成功
     *
     * @param result FingerprintManager.AuthenticationResult
     */
    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        // 获取cipher实例
        final Cipher cipher = result.getCryptoObject().getCipher();

        try {
            switch (purpose) {
                case KeyProperties.PURPOSE_ENCRYPT:
                    // >> 加密数据 >>
                    byte[] encrypted = cipher.doFinal(content.getBytes("UTF-8"));
                    // 获得初始化向量
                    byte[] IV = cipher.getIV();

                    // byte[]转为Hex字符串进行存储
                    String hexStrEncrypted = ConvertUtil.bytesToHexString(encrypted);
                    String hexStrIV = ConvertUtil.bytesToHexString(IV);

                    // 存储加密后的密文和IV
                    SharedPreferencesUtil.getInstance(context).saveData("encryptedData", hexStrEncrypted);
                    SharedPreferencesUtil.getInstance(context).saveData("IV", hexStrIV);

                    Toast.makeText(context, "IV and encrypted data save success", Toast.LENGTH_SHORT).show();
                    break;

                case KeyProperties.PURPOSE_DECRYPT:
                    // >> 解密数据 >>
                    String encryptedData = SharedPreferencesUtil.getInstance(context).getData("encryptedData");
                    // 字符串转为byte[]
                    byte[] en = ConvertUtil.StringToBytes(encryptedData);
                    // 解密
                    byte[] decrypted = cipher.doFinal(en);

                    authResultCallback.authSuccess(new String(decrypted));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAuthenticationFailed() {
        authResultCallback.authFailed();
    }


    // interface
    interface IAuthResult {
        /**
         * 成功
         *
         * @param decryptData 解密数据后的明文
         */
        void authSuccess(String decryptData);

        /**
         * 失败
         */
        void authFailed();
    }
}
