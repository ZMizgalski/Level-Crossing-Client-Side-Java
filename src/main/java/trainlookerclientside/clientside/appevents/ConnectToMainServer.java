package trainlookerclientside.clientside.appevents;

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

    private boolean sendCurrentIpAddress() {
        Socket s;
        try {
            s = new Socket(ip, socketPort);
        } catch (IOException e) {
            return false;
        }
        PrintWriter out;
        try {
            out = new PrintWriter(s.getOutputStream(), true);
        } catch (IOException e) {
            return false;
        }
        out.write("http://" + s.getLocalAddress().getHostAddress() + ":" + localPort);
        out.close();
        return true;
    }

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        sendCurrentIpAddress();
        TimerTask task = new TimerTask() {
            public void run() {
                Socket s = null;
                try {
                    s = new Socket(ip, socketPort);
                } catch (IOException e) {
                    while (!sendCurrentIpAddress()) {
                        log.warn("connecting to server...");
                    }
                }
                if (s != null) {
                    PrintWriter out = null;
                    try {
                        out = new PrintWriter(s.getOutputStream(), true);
                    } catch (IOException ignored) { }
                    if (out != null) {
                        out.write("");
                        out.close();
                    }
                }
            }
        };
        new Timer().scheduleAtFixedRate(task, 1, 2000);
    }
}
