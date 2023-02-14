package com.serverless.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.model.Employee;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import com.serverless.utils.DependencyFactory;

import java.util.Collections;
import java.util.Map;

public class DeleteItemFunction implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final DynamoDbEnhancedClient dbClient;
    private final String tableName;
    private final TableSchema<Employee> bookTableSchema;

    public DeleteItemFunction() {
        dbClient = DependencyFactory.dynamoDbEnhancedClient();
        tableName = DependencyFactory.tableName();
        bookTableSchema = TableSchema.fromBean(Employee.class);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        String responseBody = "";
        DynamoDbTable<Employee> employeeTable = dbClient.table(tableName, bookTableSchema);
        Map<String, String> pathParameters = request.getPathParameters();
        if (pathParameters != null) {
            final String isbn = pathParameters.get(Employee.PARTITION_KEY);
            if (isbn != null && !isbn.isEmpty()) {
                Employee deletedBook = employeeTable.deleteItem(Key.builder().partitionValue(isbn).build());
                try {
                    responseBody = new ObjectMapper().writeValueAsString(deletedBook);
                } catch (JsonProcessingException e) {
                    context.getLogger().log("Failed create a JSON response: " + e);
                }
            }
        }
        return new APIGatewayProxyResponseEvent().withStatusCode(200)
                .withIsBase64Encoded(Boolean.FALSE)
                .withHeaders(Collections.emptyMap())
                .withBody(responseBody);
    }

}
