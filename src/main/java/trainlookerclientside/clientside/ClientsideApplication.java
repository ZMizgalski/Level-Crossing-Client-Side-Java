package trainlookerclientside.clientside;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ClientsideApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientsideApplication.class, args);
    }
}