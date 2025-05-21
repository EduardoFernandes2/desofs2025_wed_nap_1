package com.gmail.merikbest2015.ecommerce.service.graphql;

import com.gmail.merikbest2015.ecommerce.service.OrderService;
import com.gmail.merikbest2015.ecommerce.service.PerfumeService;
import com.gmail.merikbest2015.ecommerce.service.UserService;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class GraphQLProvider {

    private final PerfumeService perfumeService;
    private final OrderService orderService;
    private final UserService userService;

    @Value("classpath:graphql/schemas.graphql")
    private Resource resource;

    @Getter
    private GraphQL graphQL;

    @PostConstruct
    public void loadSchema() throws IOException {
        // Use InputStream instead of File
        try (InputStream inputStream = resource.getInputStream()) {
            // Read the InputStream into a String manually
            StringBuilder schemaStringBuilder = new StringBuilder();
            byte[] buffer = new byte[1024];
            int bytesRead;

            // Read bytes from the InputStream into the buffer
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                schemaStringBuilder.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
            }

            // Convert StringBuilder to String
            String schemaString = schemaStringBuilder.toString();

            // Parse the schema string
            TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(schemaString);
            RuntimeWiring wiring = buildRuntimeWiring();
            GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring);
            graphQL = GraphQL.newGraphQL(schema).build();
        }
    }

    private RuntimeWiring buildRuntimeWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type("Query", typeWiring -> typeWiring
                        .dataFetcher("perfumes", perfumeService.getAllPerfumesByQuery())
                        .dataFetcher("perfumesIds", perfumeService.getAllPerfumesByIdsQuery())
                        .dataFetcher("perfume", perfumeService.getPerfumeByQuery())
                        .dataFetcher("orders", orderService.getAllOrdersByQuery())
                        .dataFetcher("ordersByEmail", orderService.getUserOrdersByEmailQuery())
                        .dataFetcher("users", userService.getAllUsersByQuery())
                        .dataFetcher("user", userService.getUserByQuery()))
                .build();
    }
}
