package com.chit.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "encryption")
public class EncryptionConfig {

  private String algorithm;
  private String transformation;
  private String key;
}
