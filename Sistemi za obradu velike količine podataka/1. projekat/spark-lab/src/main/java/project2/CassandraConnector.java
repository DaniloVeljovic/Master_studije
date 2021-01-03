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

    public static void main(String[] args) {
        CassandraConnector connector = new CassandraConnector();
        connector.connect();
    }
}
