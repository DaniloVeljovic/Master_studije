package wordcount;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import utils.RabbitMQManager;

import java.util.Map;

/*
   To better visualize the results,
   we include an auxiliary operator
   that exports results on a message queue,
   implemented with rabbitMQ.
 */

public class RabbitMQExporterBolt extends BaseRichBolt {

	private static final long serialVersionUID = 1L;
	private OutputCollector collector;

	private RabbitMQManager rabbitmq;
	private String rabbitMqHost;
	private String rabbitMqUsername;
	private String rabbitMqPassword;
	private String defaultQueue;

    public RabbitMQExporterBolt(String rabbitMqHost, String rabbitMqUsername, String rabbitMqPassword,
                                String defaultQueue) {
		super();
		this.rabbitMqHost = rabbitMqHost;
		this.rabbitMqUsername = rabbitMqUsername;
		this.rabbitMqPassword = rabbitMqPassword;
		this.defaultQueue = defaultQueue;
	}

	@Override
    public void prepare(@SuppressWarnings("rawtypes") Map map, TopologyContext topologyContext, OutputCollector outputCollector) {

        this.collector=outputCollector;
        this.rabbitmq = new RabbitMQManager(rabbitMqHost, rabbitMqUsername, rabbitMqPassword, defaultQueue);

    }
    
    @Override
	public void execute(Tuple tuple) {

		String word = tuple.getString(0);
		Integer count = tuple.getInteger(1);

		String output = word + " " + count;
        rabbitmq.send(output);

    }
    
    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
    	
        outputFieldsDeclarer.declare(new Fields("word"));
    
    }

}