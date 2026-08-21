package com.benchmark.db;

import redis.clients.jedis.Jedis;
import java.util.List;

public class FalkorDBAdapter implements DatabaseAdapter {
    private final String uri;
    private Jedis jedis;
    private static final String GRAPH_NAME = "wiki";

    public FalkorDBAdapter(String uri) {
        this.uri = uri;
    }

    @Override
    public String getName() {
        return "FalkorDB (Local Docker)";
    }

    @Override
    public void connect() throws Exception {
        this.jedis = new Jedis(uri);
        this.jedis.ping();
    }

    @Override
    public void clearDatabase() throws Exception {
        try {
            jedis.sendCommand(() -> "GRAPH.DELETE".getBytes(), GRAPH_NAME);
        } catch (Exception e) {
        }
    }

    @Override
    public void createIndexes() throws Exception {
        String query = "CREATE INDEX ON :User(id)";
        jedis.sendCommand(() -> "GRAPH.QUERY".getBytes(), GRAPH_NAME, query);
    }

    @Override
    public void loadBatch(List<String[]> batch) throws Exception {
        StringBuilder query = new StringBuilder();
        for (int i = 0; i < batch.size(); i++) {
            String[] edge = batch.get(i);
            String from = edge[0].replace("'", "\\'");
            String to = edge[1].replace("'", "\\'");
            query.append(String.format("MERGE (a%d:User {id: '%s'}) MERGE (b%d:User {id: '%s'}) CREATE (a%d)-[:VOTED_FOR]->(b%d) ",
                    i, from, i, to, i, i));
        }
        jedis.sendCommand(() -> "GRAPH.QUERY".getBytes(), GRAPH_NAME, query.toString());
    }

    @Override
    public int run1HopTraversal(String nodeId) throws Exception {
        String query = String.format("MATCH (n:User {id: '%s'})-[:VOTED_FOR]->(m) RETURN m.id", nodeId.replace("'", "\\'"));
        return runHopQuery(query);
    }

    @Override
    public int run2HopTraversal(String nodeId) throws Exception {
        String query = String.format("MATCH (n:User {id: '%s'})-[:VOTED_FOR]->()-[:VOTED_FOR]->(m) RETURN m.id", nodeId.replace("'", "\\'"));
        return runHopQuery(query);
    }

    @Override
    public int run3HopTraversal(String nodeId) throws Exception {
        String query = String.format("MATCH (n:User {id: '%s'})-[:VOTED_FOR]->()-[:VOTED_FOR]->()-[:VOTED_FOR]->(m) RETURN m.id", nodeId.replace("'", "\\'"));
        return runHopQuery(query);
    }

    private int runHopQuery(String query) {
        List<Object> response = (List<Object>) jedis.sendCommand(() -> "GRAPH.QUERY".getBytes(), GRAPH_NAME, query);
        if (response.size() > 1 && response.get(1) instanceof List) {
            return ((List) response.get(1)).size();
        }
        return 0;
    }

    @Override
    public String runPointLookup(String nodeId) throws Exception {
        String query = String.format("MATCH (n:User {id: '%s'}) RETURN n.id", nodeId.replace("'", "\\'"));
        return runLookupQuery(query);
    }

    @Override
    public String runIndexedLookup(String nodeId) throws Exception {
        String query = String.format("MATCH (n:User) WHERE n.id = '%s' RETURN n.id", nodeId.replace("'", "\\'"));
        return runLookupQuery(query);
    }

    private String runLookupQuery(String query) {
        List<Object> response = (List<Object>) jedis.sendCommand(() -> "GRAPH.QUERY".getBytes(), GRAPH_NAME, query);
        if (response.size() > 1 && response.get(1) instanceof List) {
            List rows = (List) response.get(1);
            if (!rows.isEmpty() && rows.get(0) instanceof List) {
                List row = (List) rows.get(0);
                if (!row.isEmpty()) {
                    return parseString(row.get(0));
                }
            }
        }
        return null;
    }

    @Override
    public double runAggregation() throws Exception {
        String query = "MATCH (n:User)-[:VOTED_FOR]->(m:User) " +
                "RETURN n.id, count(m) AS votes_cast " +
                "ORDER BY votes_cast DESC " +
                "LIMIT 1";
        List<Object> response = (List<Object>) jedis.sendCommand(() -> "GRAPH.QUERY".getBytes(), GRAPH_NAME, query);
        if (response.size() > 1 && response.get(1) instanceof List) {
            List rows = (List) response.get(1);
            if (!rows.isEmpty() && rows.get(0) instanceof List) {
                List row = (List) rows.get(0);
                if (row.size() > 1) {
                    Object val = row.get(1);
                    if (val instanceof Number) {
                        return ((Number) val).doubleValue();
                    } else {
                        return Double.parseDouble(parseString(val));
                    }
                }
            }
        }
        return 0.0;
    }

    @Override
    public void runWriteQuery(String fromNodeId, String toNodeId) throws Exception {
        String query = String.format("MERGE (a:User {id: '%s'}) MERGE (b:User {id: '%s'}) CREATE (a)-[:VOTED_FOR]->(b)",
                fromNodeId.replace("'", "\\'"), toNodeId.replace("'", "\\'"));
        jedis.sendCommand(() -> "GRAPH.QUERY".getBytes(), GRAPH_NAME, query);
    }

    @Override
    public void disconnect() throws Exception {
        if (jedis != null) {
            jedis.close();
        }
    }

    private String parseString(Object obj) {
        if (obj == null) return null;
        if (obj instanceof byte[]) {
            return new String((byte[]) obj);
        }
        return obj.toString();
    }
}
