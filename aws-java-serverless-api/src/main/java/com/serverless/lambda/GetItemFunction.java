package com.serverless.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.model.Employee;
import com.serverless.utils.DependencyFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Collections;
import java.util.Map;

public class GetItemFunction implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final DynamoDbEnhancedClient dbClient;
    private final String tableName;
    private final TableSchema<Employee> bookTableSchema;

    public GetItemFunction() {
        dbClient = DependencyFactory.dynamoDbEnhancedClient();
        tableName = DependencyFactory.tableName();
        bookTableSchema = TableSchema.fromBean(Employee.class);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        String response = "";
        LambdaLogger logger = context.getLogger();
        DynamoDbTable<Employee> booksTable = dbClient.table(tableName, bookTableSchema);
        Map<String, String> pathParameters = input.getPathParameters();
        if (pathParameters != null) {
            String itemPartitionKey = pathParameters.get(Employee.PARTITION_KEY);
            Employee item = booksTable.getItem(Key.builder().partitionValue(itemPartitionKey).build());
            if (item != null) {
                try {
                    response = new ObjectMapper().writeValueAsString(item);
                } catch (JsonProcessingException e) {
                    logger.log("Failed create a JSON response: " + e);
                }
            }
        }

        return new APIGatewayProxyResponseEvent().withStatusCode(200)
                .withIsBase64Encoded(Boolean.FALSE)
                .withHeaders(Collections.emptyMap())
                .withBody(response);
    }
}
