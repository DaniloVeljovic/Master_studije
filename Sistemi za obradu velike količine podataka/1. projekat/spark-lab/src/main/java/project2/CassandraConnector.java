package project2;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class CassandraConnector {
    private Cluster cluster;

    private Session session;

    public void connect(String node, Integer port) {
        Cluster.Builder b = Cluster.builder().addContactPoint(node);
        if (port != null) {
            b.withPort(port);
        }
        cluster = b.build();

        session = cluster.connect();
    }

    public Session getSession() {
        return this.session;
    }

    public void close() {
        session.close();
        cluster.close();
    }

    public void connect() {
        CassandraConnector client = new CassandraConnector();
        client.connect("localhost", 9042);
        this.session = client.getSession();
        System.out.println("CONNECTED!");
    }

    public void createKeyspace(
            String keyspaceName, String replicationStrategy, int replicationFactor) {
        StringBuilder sb =
                new StringBuilder("CREATE KEYSPACE IF NOT EXISTS ")
                        .append(keyspaceName).append(" WITH replication = {")
                        .append("'class':'").append(replicationStrategy)
                        .append("','replication_factor':").append(replicationFactor)
                        .append("};");

        String query = sb.toString();
        session.execute(query);
    }

    public void createTable(String tableName) {
        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(tableName).append("(")
                .append("id uuid PRIMARY KEY, ")
                .append("title text,")
                .append("subject text);");

        String query = sb.toString();
        session.execute(query);
    }

    public void insertInto(String stringToWrite) {
        StringBuilder sb = new StringBuilder("INSERT INTO ")
                .append("citibike.citibike").append("(id, title) ")
                .append("VALUES (").append(" now() ,")
                .append(" '").append(stringToWrite).append("');");

        String query = sb.toString();
        session.execute(query);
    }
}
