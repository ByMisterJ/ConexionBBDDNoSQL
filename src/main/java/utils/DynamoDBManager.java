package utils;

import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DynamoDBManager {
    private static DynamoDbClient dynamoDbClient;
    private static DynamoDbEnhancedClient dynamoDbEnhancedClient;

    public static void initializeClients() {
        Properties properties = new Properties();
        try (InputStream input = DynamoDBManager.class.getClassLoader().getResourceAsStream("db.properties")) {
            properties.load(input);

            String accessKeyId = properties.getProperty("aws_access_key_id");
            String secretAccessKey = properties.getProperty("aws_secret_access_key");
            String sessionToken = properties.getProperty("aws_session_token");

            AwsCredentials awsCredentials = AwsSessionCredentials.create(accessKeyId, secretAccessKey, sessionToken);

            dynamoDbClient = DynamoDbClient.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                    .region(Region.US_EAST_1)
                    .build();

            dynamoDbEnhancedClient = DynamoDbEnhancedClient.builder()
                    .dynamoDbClient(dynamoDbClient)
                    .build();

        } catch (IOException ex) {
            System.err.println("Error al cargar el archivo de propiedades: " + ex.getMessage());
        }
    }

    // Métodos para obtener los clientes, inicializándolos si es necesario
    public static DynamoDbClient getDynamoDbClient() {
        if (dynamoDbClient == null) {
            initializeClients();
        }
        return dynamoDbClient;
    }

    public static DynamoDbEnhancedClient getEnhancedClient() {
        if (dynamoDbEnhancedClient == null) {
            initializeClients();
        }
        return dynamoDbEnhancedClient;
    }
}
