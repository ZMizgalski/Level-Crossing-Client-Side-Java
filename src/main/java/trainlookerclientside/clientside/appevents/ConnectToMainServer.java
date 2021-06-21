package trainlookerclientside.clientside.appevents;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import si.trina.socket.live.SocketConnection;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Component
public class ConnectToMainServer implements ApplicationListener<ApplicationReadyEvent> {

    @Getter
    private final ExecutorService pool = Executors.newFixedThreadPool(10);
    @Value("${server.port}")
    private int localPort;
    @Value("${main-server-socket.port}")
    private int socketPort;
    @Value("${main-server.ip}")
    private String ip;
    @Value("${main-server.port}")
    private int mainPort;
    private boolean ipSent = false;

    @SneakyThrows
    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        log.info("<=====================>");
        log.info("Waiting for remote host...");
        log.info("<=====================>");
        SocketInitialized socketInitialized = new SocketInitialized("Camera socket", ip, socketPort, localPort);
        new Thread(socketInitialized).start();

//        Future<SocketConnection> socketInitializedFuture = pool.submit(socketInitialized);
//        Socket socket = socketInitializedFuture.get().socket;
//        System.out.println(socket);
    }
}
