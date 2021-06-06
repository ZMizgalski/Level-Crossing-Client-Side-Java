package trainlookerclientside.clientside;

import com.github.sarxos.webcam.Webcam;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
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

    private final Pin PIN = RaspiPin.GPIO_01;
    public GpioPinDigitalOutput pin;
    @LocalServerPort
    private int port;

    @Autowired
    private VideoService videoService;

    @SneakyThrows
    @PostMapping(value = "/streamCamera/{id}")
    public ResponseEntity<?> streamCamera(@PathVariable String id) {
        Webcam webcam = Webcam.getDefault();
        System.out.println(webcam);
        if (webcam == null) {
            return ResponseEntity.badRequest().body(String.format("Camera not found for id: %s", id));
        }
        videoService.recordVideo(webcam, id + ".mp4", null, null, 5, 5);
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
//        if (pin == null) {
//            GpioController gpioController = GpioFactory.getInstance();
//            pin = gpioController.provisionDigitalOutputPin(PIN, "Open", PinState.HIGH);
//        }
//        pin.high();
        return ResponseEntity.ok().body(String.format("Level crossing opened with id: %s", id));
    }

    @RequestMapping(value = "/closeLevelCrossing/{id}")
    public ResponseEntity<?> closeLevelCrossing(@PathVariable String id) {
//        if (pin == null) {
//            GpioController gpioController = GpioFactory.getInstance();
//            pin = gpioController.provisionDigitalOutputPin(PIN, "Close", PinState.LOW);
//        }
//        pin.low();
        return ResponseEntity.ok().body(String.format("Level crossing closed with id: %s", id));
    }
}
