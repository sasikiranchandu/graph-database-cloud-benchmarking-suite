package com.benchmark.db;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.Protocol;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.entity.CollectionType;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.OverwriteMode;
import com.arangodb.model.PersistentIndexOptions;

public class ArangoDBAdapter implements DatabaseAdapter {

    private final String url;

    private ArangoDB arangoDB;
    private ArangoDatabase db;

    private static final String DB_NAME = "benchmark_db";
    private static final String NODE_COLL = "User";
    private static final String EDGE_COLL = "User_Voted_For";

    public ArangoDBAdapter(String url) {
        this.url = url;
    }

    @Override
    public String getName() {
        return "ArangoDB (Local Docker)";
    }

    @Override
    public void connect() throws Exception {

        String host = "localhost";
        int port = 8529;

        try {
            URI uri = new URI(url);

            if (uri.getHost() != null) {
                host = uri.getHost();
            }

            if (uri.getPort() != -1) {
                port = uri.getPort();
            }

        } catch (Exception e) {
            System.out.println("Invalid ArangoDB URL: " + url);
            throw e;
        }

        System.out.println(
                "Connecting to ArangoDB at "
                        + host + ":" + port + "..."
        );

        /*
         * ArangoDB Java Driver 7.x
         *
         * Use host(host, port).
         * Do NOT use Host.of().
         * Do NOT use connectionTtl().
         */
        this.arangoDB = new ArangoDB.Builder()
                .protocol(Protocol.HTTP_JSON)
                .host(host, port)
                .build();

        /*
         * Create benchmark database if it does not exist.
         */
        if (!arangoDB.db(DB_NAME).exists()) {
            arangoDB.createDatabase(DB_NAME);
        }

        this.db = arangoDB.db(DB_NAME);

        System.out.println(
                "Connected to ArangoDB successfully."
        );
    }

    @Override
    public void clearDatabase() throws Exception {

        /*
         * Drop edge collection first.
         */
        if (db.collection(EDGE_COLL).exists()) {
            db.collection(EDGE_COLL).drop();
        }

        /*
         * Drop node collection.
         */
        if (db.collection(NODE_COLL).exists()) {
            db.collection(NODE_COLL).drop();
        }

        /*
         * Create node collection.
         */
        db.createCollection(
                NODE_COLL,
                new CollectionCreateOptions()
                        .type(CollectionType.DOCUMENT)
        );

        /*
         * Create edge collection.
         */
        db.createCollection(
                EDGE_COLL,
                new CollectionCreateOptions()
                        .type(CollectionType.EDGES)
        );
    }

    @Override
    public void createIndexes() throws Exception {

        /*
         * Create an index on the id property.
         */
        db.collection(NODE_COLL)
                .ensurePersistentIndex(
                        Collections.singletonList("id"),
                        new PersistentIndexOptions()
                                .unique(true)
                );
    }

    @Override
    public void loadBatch(List<String[]> batch)
            throws Exception {

        Map<String, BaseDocument> nodeMap =
                new HashMap<>();

        List<BaseEdgeDocument> edges =
                new ArrayList<>();

        for (String[] edge : batch) {

            String from = edge[0];
            String to = edge[1];

            /*
             * Create source node.
             */
            if (!nodeMap.containsKey(from)) {

                BaseDocument doc =
                        new BaseDocument();

                doc.setKey(from);
                doc.addAttribute("id", from);

                nodeMap.put(from, doc);
            }

            /*
             * Create destination node.
             */
            if (!nodeMap.containsKey(to)) {

                BaseDocument doc =
                        new BaseDocument();

                doc.setKey(to);
                doc.addAttribute("id", to);

                nodeMap.put(to, doc);
            }

            /*
             * Create relationship.
             */
            BaseEdgeDocument edgeDoc =
                    new BaseEdgeDocument();

            edgeDoc.setKey(from + "_" + to);

            edgeDoc.setFrom(
                    NODE_COLL + "/" + from
            );

            edgeDoc.setTo(
                    NODE_COLL + "/" + to
            );

            edges.add(edgeDoc);
        }

        /*
         * Ignore duplicate documents.
         */
        DocumentCreateOptions options =
                new DocumentCreateOptions()
                        .overwriteMode(
                                OverwriteMode.ignore
                        );

        /*
         * Insert nodes.
         */
        if (!nodeMap.isEmpty()) {

            db.collection(NODE_COLL)
                    .insertDocuments(
                            nodeMap.values(),
                            options
                    );
        }

        /*
         * Insert relationships.
         */
        if (!edges.isEmpty()) {

            db.collection(EDGE_COLL)
                    .insertDocuments(
                            edges,
                            options
                    );
        }
    }

    @Override
    public int run1HopTraversal(String nodeId)
            throws Exception {

        String aql =
                "FOR v, e IN 1..1 OUTBOUND "
                        + "@startNode User_Voted_For "
                        + "RETURN v._key";

        return runAqlTraversal(aql, nodeId);
    }

    @Override
    public int run2HopTraversal(String nodeId)
            throws Exception {

        String aql =
                "FOR v, e IN 2..2 OUTBOUND "
                        + "@startNode User_Voted_For "
                        + "RETURN v._key";

        return runAqlTraversal(aql, nodeId);
    }

    @Override
    public int run3HopTraversal(String nodeId)
            throws Exception {

        String aql =
                "FOR v, e IN 3..3 OUTBOUND "
                        + "@startNode User_Voted_For "
                        + "RETURN v._key";

        return runAqlTraversal(aql, nodeId);
    }

    /*
     * Common traversal method.
     *
     * Bind variables are passed directly
     * to db.query().
     */
    private int runAqlTraversal(
            String aql,
            String nodeId
    ) throws Exception {

        Map<String, Object> bindVars =
                new HashMap<>();

        bindVars.put(
                "startNode",
                NODE_COLL + "/" + nodeId
        );

        ArangoCursor<String> cursor =
                db.query(
                        aql,
                        String.class,
                        bindVars
                );
        int count = 0;

        while (cursor.hasNext()) {
            cursor.next();
            count++;
        }

        return count;
    }

    @Override
    public String runPointLookup(String nodeId)
            throws Exception {

        BaseDocument doc =
                db.collection(NODE_COLL)
                        .getDocument(
                                nodeId,
                                BaseDocument.class
                        );

        return doc != null
                ? doc.getKey()
                : null;
    }

    @Override
    public String runIndexedLookup(String nodeId)
            throws Exception {

        String aql =
                "FOR u IN User "
                        + "FILTER u.id == @id "
                        + "RETURN u.id";

        Map<String, Object> bindVars =
                new HashMap<>();

        bindVars.put("id", nodeId);

        ArangoCursor<String> cursor =
                db.query(
                        aql,
                        String.class,
                        bindVars
                );

        return cursor.hasNext()
                ? cursor.next()
                : null;
    }
    @Override
    public double runAggregation()
            throws Exception {

        String aql =
                "FOR e IN User_Voted_For "
                        + "COLLECT fromNode = e._from "
                        + "WITH COUNT INTO votes_cast "
                        + "SORT votes_cast DESC "
                        + "LIMIT 1 "
                        + "RETURN votes_cast";

        ArangoCursor<Long> cursor =
                db.query(
                        aql,
                        Long.class
                );

        return cursor.hasNext()
                ? cursor.next().doubleValue()
                : 0.0;
    }

    @Override
    public void runWriteQuery(
            String fromNodeId,
            String toNodeId
    ) throws Exception {

        String aql =
                "UPSERT { _key: @fromId } "
                        + "INSERT { "
                        + "_key: @fromId, "
                        + "id: @fromId "
                        + "} "
                        + "UPDATE {} IN User "

                        + "UPSERT { _key: @toId } "
                        + "INSERT { "
                        + "_key: @toId, "
                        + "id: @toId "
                        + "} "
                        + "UPDATE {} IN User "

                        + "INSERT { "
                        + "_from: CONCAT('User/', @fromId), "
                        + "_to: CONCAT('User/', @toId) "
                        + "} "
                        + "IN User_Voted_For";

        Map<String, Object> bindVars =
                new HashMap<>();

        bindVars.put(
                "fromId",
                fromNodeId
        );

        bindVars.put(
                "toId",
                toNodeId
        );

        db.query(
                aql,
                Void.class,
                bindVars
        );
    }

    @Override
    public void disconnect()
            throws Exception {

        if (arangoDB != null) {

            arangoDB.shutdown();

            arangoDB = null;
            db = null;
        }
    }
}