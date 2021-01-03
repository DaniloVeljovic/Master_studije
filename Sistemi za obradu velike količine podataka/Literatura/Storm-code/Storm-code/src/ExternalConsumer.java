import utils.RabbitMQManager;

public class ExternalConsumer {

	public static void main(String[] args) throws InterruptedException {

		String rabbitMQ = "localhost";
		String rabbitMQUsername = "rabbitmq";
		String rabbitMQPassword = "rabbitmq";
		String rabbitMQQueue = "wordcount";

		if (args.length > 0) { rabbitMQ = args[0]; }
		if (args.length > 1) { rabbitMQUsername	 = args[1]; }
		if (args.length > 2) { rabbitMQPassword = args[2]; }
		if (args.length > 3) { rabbitMQQueue = args[3]; }

	    RabbitMQManager rmq = new RabbitMQManager(rabbitMQ, rabbitMQUsername, rabbitMQPassword, rabbitMQQueue);

		rmq.createDetachedReader(rabbitMQQueue);	//RabbitMQ reader

		while(true){
			Thread.sleep(500);
		}

	}

}
