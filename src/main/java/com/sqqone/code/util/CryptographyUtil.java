package com.sqqone.code.util;

import org.apache.shiro.crypto.hash.Md5Hash;

/**
 * @author sqq
 * @version 1.0
 * @date 2020/6/10 9:18
 */
public class CryptographyUtil {

    public final static String SALT = "sqqone";

    public static String md5(String str,String salt){
        return new Md5Hash(str,salt).toString();
    }

    public static void main(String[] args) {
        System.out.println(md5("admin",SALT));
    }
}
