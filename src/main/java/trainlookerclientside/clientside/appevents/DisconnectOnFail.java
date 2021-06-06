package trainlookerclientside.clientside.appevents;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DisconnectOnFail implements ApplicationListener<ApplicationFailedEvent> {

    @Override
    public void onApplicationEvent(@NotNull ApplicationFailedEvent event) {
        log.error("Can't connect to main server:");
        log.error("=> check if main server is online");
        log.error("=> check if endpoint is correct");
        log.error("=> check if host address is available");
        log.error("=> check if you are connected to lan");
    }
}
