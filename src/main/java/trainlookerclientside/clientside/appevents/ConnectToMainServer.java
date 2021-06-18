package trainlookerclientside.clientside.appevents;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

@Slf4j
@Component
public class ConnectToMainServer implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${server.port}")
    private int localPort;

    @Value("${main-server-socket.port}")
    private int socketPort;

    @Value("${main-server.ip}")
    private String ip;

    @Value("${main-server.port}")
    private int mainPort;

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        try {
            Socket s = new Socket(ip, socketPort);
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            out.write("http://" + s.getLocalAddress().getHostAddress() + ":" + localPort);
            out.close();

//            DataOutputStream dout = new DataOutputStream(s.getOutputStream());
//            dout.writeUTF("http://" + s.getLocalAddress().getHostAddress() + ":" + localPort);
//            dout.flush();
//            dout.close();
//            RestTemplate restTemplate = new RestTemplate();
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            HttpEntity<String> request = new HttpEntity<>("{\"levelCrossingIP\":\"" + "http://" + s.getLocalAddress().getHostAddress() + ":" + localPort + "\", " + "\"id\":\"" + UUID.randomUUID().toString() + "\"}", headers);
//            ResponseEntity<String> response = restTemplate.postForEntity(
//                    "http://" + ip + ":" + mainPort + "/api/server/registerNewLevelCrossing",
//                    request,
//                    String.class);
//            log.warn(response.getBody());
        } catch (Exception e) {
            log.error("Can't connect to main server:");
            log.error("=> check if main server is online");
            log.error("=> check if endpoint is correct");
            log.error("=> check if host address is available");
            log.error("=> check if you are connected to lan");
        }
    }
}
