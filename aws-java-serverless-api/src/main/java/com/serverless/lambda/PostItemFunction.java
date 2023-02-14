package com.serverless.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.model.Employee;
import com.serverless.utils.DependencyFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Collections;
import java.util.Map;

public class PostItemFunction implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    static final int STATUS_CODE_NO_CONTENT = 204;
    static final int STATUS_CODE_CREATED = 201;
    static final int STATUS_CODE_BAD_REQUEST = 400;
    private final DynamoDbEnhancedClient dbClient;
    private final String tableName;
    private final TableSchema<Employee> bookTableSchema;

    public PostItemFunction() {
        dbClient = DependencyFactory.dynamoDbEnhancedClient();
        tableName = DependencyFactory.tableName();
        bookTableSchema = TableSchema.fromBean(Employee.class);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        String body = request.getBody();
        // int statusCode = STATUS_CODE_NO_CONTENT;
        String responseBody="";
        if (body != null && !body.isEmpty()) {
            ObjectMapper objectMapper=new ObjectMapper();
            Employee item;
            try {
                item = objectMapper.readValue(body, Employee.class);
                int statusCode;
                if (item != null) {
                    //Map<String, String> pathParameters = request.getPathParameters();

                    DynamoDbTable<Employee> booksTable = dbClient.table(tableName, bookTableSchema);
                    Employee emp=booksTable.updateItem(item);
                    responseBody =objectMapper.writeValueAsString(emp);
                    // booksTable.putItem(item);
                    statusCode = STATUS_CODE_CREATED;
                }else {
                    statusCode = STATUS_CODE_BAD_REQUEST;
                }

            } catch (JsonProcessingException e) {
                context.getLogger().log("Failed to deserialize JSON: " + e);
            }

        }
        return new APIGatewayProxyResponseEvent().withStatusCode(200)
                .withIsBase64Encoded(Boolean.FALSE)
                .withHeaders(Collections.emptyMap())
                .withBody(responseBody);

    }

    private boolean arePathParametersValid(Map<String, String> pathParameters, Employee item) {
        if (pathParameters == null) {
            return false;
        }
        String itemPartitionKey = pathParameters.get(Employee.PARTITION_KEY);
        if (itemPartitionKey == null || itemPartitionKey.isEmpty()) {
            return false;
        }
        return itemPartitionKey.equals(item.getEmpid());
    }

}