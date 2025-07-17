package com.chit.api.utilities;

import com.chit.api.config.EncryptionConfig;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class EncryptionUtils {

  private final EncryptionConfig encryptionConfig;

  public EncryptionUtils(EncryptionConfig encryptionConfig) {
    this.encryptionConfig = encryptionConfig;
  }

  public String encrypt(String data) throws Exception {
    SecretKeySpec secretKey = new SecretKeySpec(encryptionConfig.getKey().getBytes(),
        encryptionConfig.getAlgorithm());
    Cipher cipher = Cipher.getInstance(encryptionConfig.getTransformation());
    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
    byte[] encryptedData = cipher.doFinal(data.getBytes());
    return Base64.getEncoder().encodeToString(encryptedData);
  }

  public String decrypt(String encryptedData) throws Exception {
    SecretKeySpec secretKey = new SecretKeySpec(encryptionConfig.getKey().getBytes(),
        encryptionConfig.getAlgorithm());
    Cipher cipher = Cipher.getInstance(encryptionConfig.getTransformation());
    cipher.init(Cipher.DECRYPT_MODE, secretKey);
    byte[] decodedData = Base64.getDecoder().decode(encryptedData);
    byte[] decryptedData = cipher.doFinal(decodedData);
    return new String(decryptedData);
  }
}
