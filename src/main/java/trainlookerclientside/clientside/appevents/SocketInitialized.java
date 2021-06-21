package trainlookerclientside.clientside.appevents;

import lombok.SneakyThrows;
import si.trina.socket.live.SocketConnection;

import java.io.PrintWriter;

public class SocketInitialized implements Runnable {

    private final String ip;
    private final int port;
    private final String name;
    private final int localPort;
    private boolean connectionEstablished = false;

    public SocketInitialized(String name, String ip, int port, int localPort) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.localPort = localPort;
    }

    @SneakyThrows
    @Override
    public void run() {
        SocketConnection socketInitialized = new SocketConnection(name, ip, port);
        new Thread(socketInitialized).start();

        while (true) {
            if (socketInitialized.connected) {
                if (!connectionEstablished) {
                    PrintWriter out = new PrintWriter(socketInitialized.socket.getOutputStream(), true);
                    out.write("http://" + socketInitialized.socket.getLocalAddress().getHostAddress() + ":" + localPort);
                    out.close();
                    connectionEstablished = true;
                }
            } else {
                connectionEstablished = false;
            }
            Thread.sleep(2000);
        }
    }
}
