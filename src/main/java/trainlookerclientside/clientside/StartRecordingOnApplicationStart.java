package trainlookerclientside.clientside;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import static trainlookerclientside.clientside.DataService.deleteOldVideos;


@Component
public class StartRecordingOnApplicationStart implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        deleteOldVideos(14);
    }
}
