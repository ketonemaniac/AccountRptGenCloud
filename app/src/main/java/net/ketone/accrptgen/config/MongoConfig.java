package net.ketone.accrptgen.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.connection.SslSettings;
import net.ketone.accrptgen.admin.CredentialsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

import java.util.List;
import java.util.Properties;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Autowired
    private CredentialsService credentialsService;


    @Override
    public String getDatabaseName() {
        return "testing";
    }

    @Override
    protected void configureClientSettings(MongoClientSettings.Builder builder) {
        Properties p = credentialsService.getCredentials();
        builder
                .credential(MongoCredential.createCredential("root", "admin",
                        p.get(CredentialsService.MONGODB_PASS).toString().toCharArray()))
                .applyToClusterSettings(settings  -> {
                    settings.hosts(List.of(new ServerAddress("cluster0-shard-00-00.yztpq.mongodb.net", 27017),
                            new ServerAddress("cluster0-shard-00-01.yztpq.mongodb.net", 27017),
                            new ServerAddress("cluster0-shard-00-02.yztpq.mongodb.net", 27017)
                    ))
                    ;
                })
                .applyToSslSettings((block) -> block.applySettings(SslSettings.builder()
                        .enabled(true)
                        .invalidHostNameAllowed(true)
                        .build()))
                ;
    }
}
