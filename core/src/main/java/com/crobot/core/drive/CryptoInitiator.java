package com.crobot.core.drive;

import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoInitiator implements Initiator {
    @Override
    public void execute(ContextProxy context) {
        context.setObjApt("Crypto", new CryptoApt());
    }

    public static class CryptoApt extends ObjApt {

        /**
         * 计算消息摘要
         *
         * @param message   要计算摘要的数据
         * @param algorithm 算法名称，如 "MD5", "SHA-1", "SHA-256", "SHA-512"
         * @return 摘要的十六进制字符串，失败返回null
         */
        public String digest(byte[] message, String algorithm) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithm);
                byte[] digest = md.digest(message);
                return bytesToHex(digest);
            } catch (NoSuchAlgorithmException e) {
                return null;
            }
        }

        /**
         * MD5 摘要
         *
         * @param message 要计算摘要的数据
         * @return MD5 摘要的十六进制字符串
         */
        @Caller("md5")
        public String md5(byte[] message) {
            return digest(message, "MD5");
        }

        /**
         * SHA-1 摘要
         *
         * @param message 要计算摘要的数据
         * @return SHA-1 摘要的十六进制字符串
         */
        @Caller("sha1")
        public String sha1(byte[] message) {
            return digest(message, "SHA-1");
        }

        /**
         * SHA-256 摘要
         *
         * @param message 要计算摘要的数据
         * @return SHA-256 摘要的十六进制字符串
         */
        @Caller("sha256")
        public String sha256(byte[] message) {
            return digest(message, "SHA-256");
        }

        /**
         * SHA-512 摘要
         *
         * @param message 要计算摘要的数据
         * @return SHA-512 摘要的十六进制字符串
         */
        @Caller("sha512")
        public String sha512(byte[] message) {
            return digest(message, "SHA-512");
        }

        /**
         * AES 加密 (CBC 模式, PKCS5Padding)
         *
         * @param data 要加密的数据
         * @param key  密钥 (16/24/32字节对应AES-128/192/256)
         * @param iv   初始化向量 (16字节)
         * @return 加密后的数据，失败返回null
         */
        @Caller("aesEncrypt")
        public byte[] aesEncrypt(byte[] data, byte[] key, byte[] iv) {
            try {
                SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
                IvParameterSpec ivSpec = new IvParameterSpec(iv);
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
                return cipher.doFinal(data);
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * AES 解密 (CBC 模式, PKCS5Padding)
         *
         * @param encryptedData 加密的数据
         * @param key           密钥 (16/24/32字节对应AES-128/192/256)
         * @param iv            初始化向量 (16字节)
         * @return 解密后的数据，失败返回null
         */
        @Caller("aesDecrypt")
        public byte[] aesDecrypt(byte[] encryptedData, byte[] key, byte[] iv) {
            try {
                SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
                IvParameterSpec ivSpec = new IvParameterSpec(iv);
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
                return cipher.doFinal(encryptedData);
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * AES 加密 (ECB 模式, PKCS5Padding) - 不需要IV
         *
         * @param data 要加密的数据
         * @param key  密钥 (16/24/32字节对应AES-128/192/256)
         * @return 加密后的数据，失败返回null
         */
        @Caller("aesEncryptECB")
        public byte[] aesEncryptECB(byte[] data, byte[] key) {
            try {
                SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                return cipher.doFinal(data);
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * AES 解密 (ECB 模式, PKCS5Padding) - 不需要IV
         *
         * @param encryptedData 加密的数据
         * @param key           密钥 (16/24/32字节对应AES-128/192/256)
         * @return 解密后的数据，失败返回null
         */
        @Caller("aesDecryptECB")
        public byte[] aesDecryptECB(byte[] encryptedData, byte[] key) {
            try {
                SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                return cipher.doFinal(encryptedData);
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * 生成随机密钥
         *
         * @param keySize 密钥长度，128/192/256
         * @return 生成的密钥，失败返回null
         */
        @Caller("generateKey")
        public byte[] generateKey(Number keySize) {
            try {
                KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
                keyGenerator.init(keySize.intValue());
                SecretKey secretKey = keyGenerator.generateKey();
                return secretKey.getEncoded();
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * 生成随机IV (16字节)
         *
         * @return 生成的IV
         */
        @Caller("generateIV")
        public byte[] generateIV() {
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            return iv;
        }

        /**
         * 从密码生成密钥 (使用PBKDF2)
         *
         * @param password   密码
         * @param salt       盐值
         * @param keyLength  密钥长度 (128/192/256)
         * @param iterations 迭代次数
         * @return 生成的密钥，失败返回null
         */
        @Caller("deriveKey")
        public byte[] deriveKey(String password, byte[] salt, Number keyLength, Number iterations) {
            try {
                javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(password.toCharArray(), salt, iterations.intValue(), keyLength.intValue());
                javax.crypto.SecretKeyFactory factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                return factory.generateSecret(spec).getEncoded();
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * 字节数组转十六进制字符串
         */
        private String bytesToHex(byte[] bytes) {
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        }
    }
}
