package com.benchmark.db;

import java.util.List;

public interface DatabaseAdapter {
    String getName();

    void connect() throws Exception;

    void clearDatabase() throws Exception;

    void createIndexes() throws Exception;

    void loadBatch(List<String[]> batch) throws Exception;

    int run1HopTraversal(String nodeId) throws Exception;

    int run2HopTraversal(String nodeId) throws Exception;

    int run3HopTraversal(String nodeId) throws Exception;

    String runPointLookup(String nodeId) throws Exception;

    String runIndexedLookup(String nodeId) throws Exception;

    double runAggregation() throws Exception;

    void runWriteQuery(String fromNodeId, String toNodeId) throws Exception;

    void disconnect() throws Exception;
}
