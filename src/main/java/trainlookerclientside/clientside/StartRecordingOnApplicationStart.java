package trainlookerclientside.clientside;

import lombok.SneakyThrows;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static trainlookerclientside.clientside.DataService.*;


@Component
public class StartRecordingOnApplicationStart implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        deleteOldVideos(14);
    }
}
