package trainlookerclientside.clientside;

import com.diozero.api.RuntimeIOException;
import com.diozero.devices.PCA9685;
import com.diozero.devices.PwmLed;
import com.diozero.internal.spi.PwmOutputDeviceFactoryInterface;
import com.diozero.util.SleepUtil;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.ds.v4l4j.V4l4jDriver;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Slf4j
@RestController
@CrossOrigin(value = "*", maxAge = 3600)
public class WebController {

    @LocalServerPort
    private int port;

    @Autowired
    private VideoService videoService;

    @SneakyThrows
    @PostMapping(value = "/streamCamera/{id}")
    public ResponseEntity<?> streamCamera(@PathVariable String id) {
        Webcam webcam1 = Webcam.getDefault();
        if (webcam1 == null) {
            return ResponseEntity.badRequest().body(String.format("Camera not found for id: %s", id));
        }
        if (videoService.checkIfRasp()) {
            Webcam.setDriver(new V4l4jDriver());
        }
        Webcam webcam2 = Webcam.getDefault();
        System.out.println(webcam2);
        videoService.recordVideo(webcam2, id + ".mp4", null, null, 5, 5);
        File file = new File(id + ".mp4");
        String type = FilenameUtils.getExtension(id + ".mp4");
        InputStream inputStream = new FileInputStream(id + ".mp4");
        byte[] out = org.apache.commons.io.IOUtils.toByteArray(inputStream);
        inputStream.close();
        file.delete();
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("video/" + type))
                .header(HttpHeaders.CONTENT_DISPOSITION, "filename=\"" + id + ".mp4" + "\"")
                .body(out);
    }

    @RequestMapping(value = "/openLevelCrossing/{id}")
    public ResponseEntity<?> openLevelCrossing(@PathVariable String id) {
        float delay = 0.5f;
        try (PwmOutputDeviceFactoryInterface df = new PCA9685(400);
             PwmLed led1 = new PwmLed(df, 0);
             PwmLed led2 = new PwmLed(df, 2);
             PwmLed led3 = new PwmLed(df, 5)
        ) {
            led1.setValue(.25f);
            led2.setValue(.25f);
            led3.setValue(.25f);
            SleepUtil.sleepSeconds(delay);
        } catch (RuntimeIOException e) {
            return ResponseEntity.badRequest().body(String.format("Something wrong with motors when opening levelCrossing with id: %s", id));

        }
        return ResponseEntity.ok().body(String.format("Level crossing opened with id: %s", id));
    }

    @RequestMapping(value = "/closeLevelCrossing/{id}")
    public ResponseEntity<?> closeLevelCrossing(@PathVariable String id) {
        float delay = 0.5f;
        try (PwmOutputDeviceFactoryInterface df = new PCA9685(400);
             PwmLed led1 = new PwmLed(df, 0);
             PwmLed led2 = new PwmLed(df, 2);
             PwmLed led3 = new PwmLed(df, 5)
        ) {
            led1.setValue(.5f);
            led2.setValue(.5f);
            led3.setValue(.5f);
            SleepUtil.sleepSeconds(delay);
        } catch (RuntimeIOException e) {
            return ResponseEntity.badRequest().body(String.format("Something wrong with motors when closing levelCrossing with id: %s", id));
        }
        return ResponseEntity.ok().body(String.format("Level crossing closed with id: %s", id));
    }
}
