package com.foco.boot.db.intercept.encrypt;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.foco.boot.db.properties.FieldEncryptProperties;

/**
 * @author lucoo
 * @version 1.0.0
 * @description TODO
 * @date 2021/08/04 18:06
 */
public class FiledEncryptUtil{
    private static byte[] key= FieldEncryptProperties.getConfig().getFieldEncryptKey().getBytes();;
    public static String encrypt(String value){
        AES aes = SecureUtil.aes(key);
        return aes.encryptBase64(value);
    }
    public static String decrypt(String value){
        AES aes = SecureUtil.aes(key);
        return aes.decryptStr(value);
    }
}
