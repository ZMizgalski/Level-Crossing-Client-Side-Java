package trainlookerclientside.clientside.appevents;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.Socket;
import java.util.UUID;

@Slf4j
@Component
public class ConnectToMainServer implements ApplicationListener<ApplicationReadyEvent> {

    @LocalServerPort
    private int port;

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        try {
            Socket s = new Socket("192.168.1.212", 8080);
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>("{\"levelCrossingIP\":\"" + "http://" + s.getLocalAddress().getHostAddress() + ":" + port + "\", "+ "\"id\":\""+ UUID.randomUUID().toString() +"\"}", headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "http://localhost:8080/api/server/registerNewLevelCrossing",
                    request,
                    String.class);
            log.warn(response.getBody());
        } catch (Exception e) {
            log.error("Can't connect to main server:");
            log.error("=> check if main server is online");
            log.error("=> check if endpoint is correct");
            log.error("=> check if host address is available");
            log.error("=> check if you are connected to lan");
        }
    }
}
