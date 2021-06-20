package trainlookerclientside.clientside.appevents;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

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

    private void sendCurrentIpAddress() {
        Socket s;
        try {
            s = new Socket(ip, socketPort);
        } catch (IOException ignored) {
            return;
        }
        PrintWriter out = null;
        try {
            out = new PrintWriter(s.getOutputStream(), true);
        } catch (IOException ignored) { }
        assert out != null;
        out.write("http://" + s.getLocalAddress().getHostAddress() + ":" + localPort);
        out.close();
    }

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        try {
            sendCurrentIpAddress();
            TimerTask task = new TimerTask() {
                public void run() {
                    Socket s = null;
                    try {
                        s = new Socket(ip, socketPort);
                    } catch (IOException e) {
                        log.warn("Disconnected from server");
                        sendCurrentIpAddress();
                        return;
                    }
                    PrintWriter out = null;
                    try {
                        out = new PrintWriter(s.getOutputStream(), true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    assert out != null;
                    out.write("");
                    out.close();
                }
            };
            new Timer().scheduleAtFixedRate(task, 1, 2000);

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
