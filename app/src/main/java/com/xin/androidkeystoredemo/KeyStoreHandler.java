package com.xin.androidkeystoredemo;

import android.hardware.fingerprint.FingerprintManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import java.security.KeyStore;
import java.security.KeyStoreException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * Created by xin on 28/01/2018.
 * Android KeyStore Handler
 */

public class KeyStoreHandler {

    private static final String TAG = "KeyStoreHandler";

    private static final String KEY_ALIAS_NAME = "KEYSTORE_DEMO";

    private KeyStore mKeyStore;

    // constructors
    public KeyStoreHandler() {
        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成SecretKey
     * AES对称加密算法
     */
    public void generateKey() {
        int purpose = KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT;

        if (mKeyStore == null) {
            throw new RuntimeException("not get KeyStore instance");
        }

        try {
            mKeyStore.load(null);

            // Key生成器 使用AES算法
            KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            // Key生成器生成参数配置
            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(KEY_ALIAS_NAME, purpose);
            // 需要用户认证
            builder.setUserAuthenticationRequired(true);
            // 块模式BLOCK_MODE_CBC
            builder.setBlockModes(KeyProperties.BLOCK_MODE_CBC);
            // 填充模式PKCS7
            builder.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);

            // 初始化Key生成器
            generator.init(builder.build());
            // 生成key
            generator.generateKey();

            Log.wtf(TAG, "Android KeyStore secretKey generate complete");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 构造与OS指纹关联的加密模块
     *
     * @param purpose 用于加密还是解密
     * @param IV      初始化向量
     * @return FingerprintManager.CryptoObj
     */
    public FingerprintManager.CryptoObject createCryptoObj(int purpose, byte[] IV) {

        if (mKeyStore == null) {
            throw new RuntimeException("not get KeyStore instance");
        }

        // 解密 需要初始化向量
        if (purpose == KeyProperties.PURPOSE_DECRYPT && IV == null) {
            throw new RuntimeException("IV is null");
        }

        try {
            mKeyStore.load(null);

            // 从KeyStore中根据秘钥别名提取秘钥 AES的secretKey
            final SecretKey secretKey = (SecretKey) mKeyStore.getKey(KEY_ALIAS_NAME, null);
            if (secretKey == null)
                return null;

            // 创建cipher
            // AES + CBC + PKCS7
            final Cipher cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7
            );

            // 初始化cipher
            switch (purpose) {
                case KeyProperties.PURPOSE_ENCRYPT:
                    // 加密
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                    break;

                case KeyProperties.PURPOSE_DECRYPT:
                    // 解密
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(IV));
                    break;
            }

            // 构造FingerprintManager.CryptoObj 系统指纹加密模块
            return new FingerprintManager.CryptoObject(cipher);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
