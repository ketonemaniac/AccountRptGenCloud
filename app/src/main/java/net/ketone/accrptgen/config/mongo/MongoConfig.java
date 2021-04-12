package net.ketone.accrptgen.config.mongo;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.connection.SslSettings;
import net.ketone.accrptgen.service.credentials.SettingsService;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

import java.util.List;
import java.util.Properties;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Configuration
@Profile("!test")
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Autowired
    private SettingsService credentialsService;


    @Value("${mongo.database.name}")
    private String mongoDbName;

    @Override
    public String getDatabaseName() {
        return mongoDbName;
    }

    @Override
    protected void configureClientSettings(MongoClientSettings.Builder builder) {
        Properties p = credentialsService.getSettings();
        builder
                .credential(MongoCredential.createCredential("root", "admin",
                        p.get(SettingsService.MONGODB_PASS).toString().toCharArray()))
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
                // https://www.mongodb.com/blog/post/rest-apis-with-java-spring-boot-and-mongodb
                .codecRegistry(fromRegistries(
                        MongoClientSettings.getDefaultCodecRegistry(),
                        CodecRegistries.fromCodecs(new ClassCodec()),
                        fromProviders(
                                new CustomCodecProvider(),
                                PojoCodecProvider.builder().automatic(true).build())

                        ))
                ;
    }
}
