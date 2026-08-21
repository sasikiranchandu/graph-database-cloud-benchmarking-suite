package com.benchmark.db;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.exceptions.ClientException;

import java.sql.DriverAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.neo4j.driver.Values.parameters;

public class Neo4jLikeAdapter implements DatabaseAdapter {
    private final String dbName;
    private final String uri;
    private final String username;
    private final String password;
    private Driver driver;

    public Neo4jLikeAdapter(String dbName, String uri, String username, String password) {
        this.dbName = dbName;
        this.uri = uri;
        this.username = username;
        this.password = password;
    }

    @Override
    public String getName() {
        return dbName;
    }

    @Override
    public void connect() throws Exception {
        AuthToken authToken;
        if (username == null || username.trim().isEmpty()) {
            authToken = AuthTokens.none();
        } else {
            authToken = AuthTokens.basic(username, password);
        }
        org.neo4j.driver.Config config = org.neo4j.driver.Config.builder()
                .withConnectionTimeout(15, TimeUnit.SECONDS)
                .build();
        this.driver = GraphDatabase.driver(uri, authToken, config);
        this.driver.verifyConnectivity();
    }

    @Override
    public void clearDatabase() throws Exception {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("MATCH (n) DETACH DELETE n");
                return null;
            });
        }
    }

    @Override
    public void createIndexes() throws Exception {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                try {
                    tx.run("CREATE INDEX user_id_idx IF NOT EXISTS FOR (n:User) ON (n.id)");
                } catch (ClientException e) {
                    try {
                        tx.run("CREATE INDEX ON :User(id)");
                    } catch (Exception ex) {
                        System.err.println("Warning: Index creation failed on " + dbName + ". Continuing without index: " + ex.getMessage());
                    }
                }
                return null;
            });
        }
    }

    @Override
    public void loadBatch(List<String[]> batch) throws Exception {
        String query = "UNWIND $batch AS edge " +
                "MERGE (a:User {id: edge.from}) " +
                "MERGE (b:User {id: edge.to}) " +
                "CREATE (a)-[:VOTED_FOR]->(b)";

        List<Map<String, Object>> batchList = new ArrayList<>();
        for (String[] edge : batch) {
            Map<String, Object> map = new HashMap<>();
            map.put("from", edge[0]);
            map.put("to", edge[1]);
            batchList.add(map);
        }

        try (Session session = driver.session()) {
            session.executeWrite(tx -> tx.run(query, parameters("batch", batchList)).consume());
        }
    }

    @Override
    public int run1HopTraversal(String nodeId) throws Exception {
        String query = "MATCH (n:User {id: $id})-[:VOTED_FOR]->(m) RETURN m.id";
        return runHopQuery(query, nodeId);
    }

    @Override
    public int run2HopTraversal(String nodeId) throws Exception {
        String query = "MATCH (n:User {id: $id})-[:VOTED_FOR]->()-[:VOTED_FOR]->(m) RETURN m.id";
        return runHopQuery(query, nodeId);
    }

    @Override
    public int run3HopTraversal(String nodeId) throws Exception {
        String query = "MATCH (n:User {id: $id})-[:VOTED_FOR]->()-[:VOTED_FOR]->()-[:VOTED_FOR]->(m) RETURN m.id";
        return runHopQuery(query, nodeId);
    }

    private int runHopQuery(String query, String nodeId) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, parameters("id", nodeId));
                int count = 0;
                while (result.hasNext()) {
                    result.next();
                    count++;
                }
                return count;
            });
        }
    }

    @Override
    public String runPointLookup(String nodeId) throws Exception {
        String query = "MATCH (n:User {id: $id}) RETURN n.id";
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, parameters("id", nodeId));
                if (result.hasNext()) {
                    return result.next().get("n.id").asString();
                }
                return null;
            });
        }
    }

    @Override
    public String runIndexedLookup(String nodeId) throws Exception {
        String query = "MATCH (n:User) WHERE n.id = $id RETURN n.id";
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, parameters("id", nodeId));
                if (result.hasNext()) {
                    return result.next().get("n.id").asString();
                }
                return null;
            });
        }
    }

    @Override
    public double runAggregation() throws Exception {
        String query = "MATCH (n:User)-[:VOTED_FOR]->(m:User) " +
                "RETURN n.id, count(m) AS votes_cast " +
                "ORDER BY votes_cast DESC " +
                "LIMIT 1";
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query);
                if (result.hasNext()) {
                    Record record = result.next();
                    return (double) record.get("votes_cast").asLong();
                }
                return 0.0;
            });
        }
    }

    @Override
    public void runWriteQuery(String fromNodeId, String toNodeId) throws Exception {
        String query = "MERGE (a:User {id: $fromId}) " +
                "MERGE (b:User {id: $toId}) " +
                "CREATE (a)-[:VOTED_FOR]->(b)";
        try (Session session = driver.session()) {
            session.executeWrite(tx -> tx.run(query, parameters("fromId", fromNodeId, "toId", toNodeId)).consume());
        }
    }

    @Override
    public void disconnect() throws Exception {
        if (driver != null) {
            driver.close();
        }
    }
}
