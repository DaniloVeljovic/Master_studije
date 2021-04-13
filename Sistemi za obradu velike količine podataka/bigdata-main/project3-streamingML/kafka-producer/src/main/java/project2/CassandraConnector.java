package project2;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import org.apache.spark.sql.sources.In;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class CassandraConnector {

    public static final String cassandraConnectionString = System.getenv("ES_CASSANDRA_CONNECTION_STRING");


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
        client.connect(cassandraConnectionString, 9042);
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
                .append("location varchar,")
                .append("min int,")
                .append("max int,")
                .append("avg double,")
                .append("message varchar);");

        String query = sb.toString();
        session.execute(query);
    }

    public void emptyTable() {
        StringBuilder sb = new StringBuilder("TRUNCATE TABLE ")
                .append("citibike.citibike");

        String query = sb.toString();
        session.execute(query);
    }

    public void insertInto(String location, Integer min, Integer max, Double avg, String message) {
        StringBuilder sb = new StringBuilder("INSERT INTO ")
                .append("citibike.citibike").append("(id, location, min, max, avg, message) ")
                .append("VALUES (").append(" now() ,")
                .append(" '").append(location).append("',")
                .append(" ").append(min).append(", ")
                .append(" ").append(max).append(", ")
                .append(" ").append(avg).append(", ")
                .append(" '").append(message).append("');");

        String query = sb.toString();
        session.execute(query);
    }

    public void deleteTable(String tableName) {
        StringBuilder sb = new StringBuilder("DROP TABLE IF EXISTS " + tableName);
        session.execute(sb.toString());
    }
}
