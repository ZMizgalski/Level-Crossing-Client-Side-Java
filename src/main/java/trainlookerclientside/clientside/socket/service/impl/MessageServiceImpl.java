package trainlookerclientside.clientside.socket.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.integration.ip.tcp.connection.TcpConnectionEvent;
import org.springframework.integration.ip.tcp.connection.TcpConnectionExceptionEvent;
import org.springframework.integration.ip.tcp.connection.TcpConnectionOpenEvent;
import org.springframework.stereotype.Service;
import trainlookerclientside.clientside.socket.gateway.TcpClientGateway;
import trainlookerclientside.clientside.socket.service.MessageService;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageServiceImpl.class);

    private final TcpClientGateway tcpClientGateway;
    private boolean connectionEstablished = false;
    @Value("${server.ip}")
    private String ip;
    @Value("${server.port}")
    private int port;

    @Autowired
    public MessageServiceImpl(TcpClientGateway tcpClientGateway) {
        this.tcpClientGateway = tcpClientGateway;
    }

    @EventListener(TcpConnectionEvent.class)
    public void connectionEvent(TcpConnectionEvent event) {
        if (event instanceof TcpConnectionOpenEvent) {
            connectionEstablished = true;
        } else if (event instanceof TcpConnectionExceptionEvent) {
            connectionEstablished = false;
        }
    }

    @Override
    public void sendMessage() {
        if (!connectionEstablished) {
            String message = "http://" + ip + ":" + port;
            LOGGER.info("Send message: {}", message);
            tcpClientGateway.send(message.getBytes());
        }
    }

}
