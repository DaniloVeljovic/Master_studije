package elfak.masterrad.devicesservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DevicesserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevicesserviceApplication.class, args);
	}

}
