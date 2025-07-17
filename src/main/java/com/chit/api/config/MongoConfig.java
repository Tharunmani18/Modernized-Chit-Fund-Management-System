package com.chit.api.config;

import com.chit.api.globalexceptions.ChitApiException;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.util.List;

@Configuration
public class MongoConfig {

    @Value("${mongo.database}")
    private String mongoDatabase;

    @Value("${mongo.host}")
    private String mongoHost;

    @Value("${mongo.port}")
    private int mongoPort;

    @Value("${mongo.client-cert.path:}")
    private String clientCertPath;

    @Value("${mongo.client-cert.password:}")
    private String clientCertPassword;

    @Value("${mongo.ca-cert.path:}")
    private String caCertPath;

    @Value("${mongo.user:}")
    private String userName;

    @Bean
    public MongoClient mongoClient() {
        try {
            if (!clientCertPath.isEmpty() && !caCertPath.isEmpty()) {

                // Initialize a KeyStore for the client certificate (PKCS12)
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                keyStore.load(new FileInputStream(clientCertPath), clientCertPassword.toCharArray());  // Provide password if any

                // Initialize a TrustStore for the CA certificate
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);  // Initialize the TrustStore
                trustStore.setCertificateEntry("ca-cert", CertificateFactory.getInstance("X.509").generateCertificate(new FileInputStream(caCertPath)));

                // Initialize the KeyManager for the client certificate (private key and certificate)
                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keyStore, clientCertPassword.toCharArray());  // Use the same password as for the PKCS12

                // Initialize the TrustManager for the CA certificate
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(trustStore);

                // Set up the SSL context
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

                // MongoCredential setup with X.509
                MongoCredential credential = MongoCredential.createMongoX509Credential(userName);

                // MongoDB settings with SSL and X.509 authentication
                MongoClientSettings settings = MongoClientSettings.builder()
                        .applyToSslSettings(builder -> builder.enabled(true).context(sslContext))
                        .credential(credential)
                        .applyToClusterSettings(builder -> builder.hosts(List.of(new ServerAddress(mongoHost, mongoPort))))
                        .build();

                // Create MongoDB client and connect
                return MongoClients.create(settings);
            } else {
                MongoClientSettings settings = MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString("mongodb://" + mongoHost + ":" + mongoPort + "/" + mongoDatabase))
                        .build();
                return MongoClients.create(settings);
            }
        } catch (Exception e) {
            throw new ChitApiException("Exception while connecting to mongoDB", e);
        }
    }

    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        // Create SimpleMongoClientDatabaseFactory by providing the MongoClient and database name
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoClient(), mongoDatabase));
    }
}