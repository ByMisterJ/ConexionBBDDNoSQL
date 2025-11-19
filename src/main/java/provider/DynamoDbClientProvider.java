package provider;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

/**
 * Provider for DynamoDB clients supporting both local (DynamoDB Local) and AWS modes.
 * Configuration can be provided via environment variables or application.properties file.
 * 
 * Environment variables:
 * - DYNAMODB_ENDPOINT: Endpoint URL for DynamoDB (if not set, uses AWS)
 * - AWS_REGION: AWS region (default: us-east-1)
 * - AWS_ACCESS_KEY_ID: AWS access key
 * - AWS_SECRET_ACCESS_KEY: AWS secret key
 * - AWS_SESSION_TOKEN: AWS session token (optional)
 */
public class DynamoDbClientProvider {
    private static DynamoDbClient dynamoDbClient;
    private static DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private static final String PROPERTIES_FILE = "application.properties";

    /**
     * Initializes the DynamoDB clients based on configuration.
     * Supports two modes:
     * 1. Local mode: Uses DynamoDB Local (when DYNAMODB_ENDPOINT env var is set)
     * 2. AWS mode: Uses AWS DynamoDB (when DYNAMODB_ENDPOINT is not set)
     */
    public static void initializeClients() {
        String endpoint = getConfigValue("DYNAMODB_ENDPOINT", "dynamodb.endpoint");
        String region = getConfigValue("AWS_REGION", "aws.region", "us-east-1");
        
        var clientBuilder = DynamoDbClient.builder()
                .region(Region.of(region));

        // Configure for local or AWS mode
        if (endpoint != null && !endpoint.isEmpty()) {
            // Local mode (DynamoDB Local)
            System.out.println("Initializing DynamoDB client in LOCAL mode with endpoint: " + endpoint);
            clientBuilder.endpointOverride(URI.create(endpoint));
            
            // For local, use dummy credentials if not provided
            String accessKey = getConfigValue("AWS_ACCESS_KEY_ID", "aws.access.key.id", "dummy");
            String secretKey = getConfigValue("AWS_SECRET_ACCESS_KEY", "aws.secret.access.key", "dummy");
            
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            clientBuilder.credentialsProvider(StaticCredentialsProvider.create(credentials));
        } else {
            // AWS mode
            System.out.println("Initializing DynamoDB client in AWS mode");
            
            // Try to get credentials from environment or properties file
            String accessKey = getConfigValue("AWS_ACCESS_KEY_ID", "aws.access.key.id");
            String secretKey = getConfigValue("AWS_SECRET_ACCESS_KEY", "aws.secret.access.key");
            String sessionToken = getConfigValue("AWS_SESSION_TOKEN", "aws.session.token");
            
            if (accessKey != null && secretKey != null) {
                AwsCredentials credentials;
                if (sessionToken != null && !sessionToken.isEmpty()) {
                    credentials = AwsSessionCredentials.create(accessKey, secretKey, sessionToken);
                } else {
                    credentials = AwsBasicCredentials.create(accessKey, secretKey);
                }
                clientBuilder.credentialsProvider(StaticCredentialsProvider.create(credentials));
            } else {
                // Use default credentials provider chain (IAM role, env vars, etc.)
                clientBuilder.credentialsProvider(DefaultCredentialsProvider.create());
            }
        }

        dynamoDbClient = clientBuilder.build();
        dynamoDbEnhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    /**
     * Gets the DynamoDB client, initializing it if necessary.
     * @return the DynamoDB client
     */
    public static DynamoDbClient getDynamoDbClient() {
        if (dynamoDbClient == null) {
            initializeClients();
        }
        return dynamoDbClient;
    }

    /**
     * Gets the DynamoDB Enhanced client, initializing it if necessary.
     * @return the DynamoDB Enhanced client
     */
    public static DynamoDbEnhancedClient getEnhancedClient() {
        if (dynamoDbEnhancedClient == null) {
            initializeClients();
        }
        return dynamoDbEnhancedClient;
    }

    /**
     * Gets a configuration value from environment variable or properties file.
     * @param envVar environment variable name
     * @param propertyKey property file key
     * @return the configuration value or null if not found
     */
    private static String getConfigValue(String envVar, String propertyKey) {
        return getConfigValue(envVar, propertyKey, null);
    }

    /**
     * Gets a configuration value from environment variable or properties file with a default.
     * @param envVar environment variable name
     * @param propertyKey property file key
     * @param defaultValue default value if not found
     * @return the configuration value or default
     */
    private static String getConfigValue(String envVar, String propertyKey, String defaultValue) {
        // First check environment variable
        String value = System.getenv(envVar);
        if (value != null && !value.isEmpty()) {
            return value;
        }

        // Then check properties file
        try (InputStream input = DynamoDbClientProvider.class.getClassLoader()
                .getResourceAsStream(PROPERTIES_FILE)) {
            if (input != null) {
                Properties properties = new Properties();
                properties.load(input);
                value = properties.getProperty(propertyKey);
                if (value != null && !value.isEmpty()) {
                    return value;
                }
            }
        } catch (IOException e) {
            // Properties file not found or error reading, ignore
        }

        return defaultValue;
    }

    /**
     * Closes the DynamoDB clients.
     */
    public static void close() {
        if (dynamoDbClient != null) {
            dynamoDbClient.close();
            dynamoDbClient = null;
        }
        if (dynamoDbEnhancedClient != null) {
            dynamoDbEnhancedClient = null;
        }
    }
}
