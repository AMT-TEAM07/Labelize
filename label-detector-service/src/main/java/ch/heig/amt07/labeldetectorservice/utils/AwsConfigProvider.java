package ch.heig.amt07.labeldetectorservice.utils;

import io.github.cdimascio.dotenv.Dotenv;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

public class AwsConfigProvider {
    private final AwsCredentialsProvider credentialsProvider;
    private final Region region;

    public AwsConfigProvider(String accessKeyVariable, String secretKeyVariable, String regionVariable) {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .systemProperties()
                .load();
        AwsBasicCredentials credentials = AwsBasicCredentials.create(dotenv.get(accessKeyVariable),
                dotenv.get(secretKeyVariable));
        credentialsProvider = StaticCredentialsProvider.create(credentials);
        region = Region.of(dotenv.get(regionVariable));
    }

    public AwsConfigProvider() {
        this("AWS_ACCESS_KEY_ID", "AWS_SECRET_ACCESS_KEY", "AWS_DEFAULT_REGION");
    }

    public AwsCredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    public Region getRegion() {
        return region;
    }
}
