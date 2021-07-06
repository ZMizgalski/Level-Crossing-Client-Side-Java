package trainlookerclientside.clientside;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static trainlookerclientside.clientside.DataService.getCover;
import static trainlookerclientside.clientside.DataService.recordVideo;

@Component
public class RecordScheduler {

    @Scheduled(fixedDelay = 60 * 1000L)
    @Transactional
    public void recordVideos() {
        recordVideo(120);
    }
}
