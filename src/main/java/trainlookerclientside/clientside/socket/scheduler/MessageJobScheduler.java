package trainlookerclientside.clientside.socket.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import trainlookerclientside.clientside.socket.service.MessageService;

@Component
public class MessageJobScheduler {

    private final MessageService messageService;

    @Autowired
    public MessageJobScheduler(MessageService messageService) {
        this.messageService = messageService;
    }

    @Scheduled(fixedDelay = 1000L)
    @Transactional
    public void sendMessageJob() {
        messageService.sendMessage();
    }

}
