package trainlookerclientside.clientside;

import com.diozero.api.RuntimeIOException;
import com.diozero.devices.PCA9685;
import com.diozero.devices.PwmLed;
import com.diozero.internal.spi.PwmOutputDeviceFactoryInterface;
import com.diozero.util.SleepUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.io.InputStream;

@Slf4j
@RestController
@CrossOrigin(value = "*", maxAge = 3600)
public class WebController {

    @LocalServerPort
    private int port;

    @SneakyThrows
    @PostMapping(value = "/streamCamera/{id}")
    public ResponseEntity<?> streamCamera(@PathVariable String id) {
        String format = "h264";
        String outFormat = "mp4";
        Process p1 = Runtime.getRuntime().exec("raspivid -n -t 5000 -o " + id + "." + format);
        p1.waitFor();
        Process p2 = Runtime.getRuntime().exec("MP4Box -add " + id + "." + format + " " + id + "." + outFormat);
        p2.waitFor();
        InputStream inputStream = new FileInputStream(id + "." + outFormat);
        byte[] out = org.apache.commons.io.IOUtils.toByteArray(inputStream);
        inputStream.close();
        Process p3 = Runtime.getRuntime().exec("rm " + id + "." + format);
        p3.waitFor();
        Process p4 = Runtime.getRuntime().exec("rm " + id + "." + outFormat);
        p4.waitFor();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "filename=\"" + id + "." + outFormat + "\"");
        responseHeaders.set(HttpHeaders.CONTENT_RANGE, "" + (out.length - 1));
        responseHeaders.set(HttpHeaders.ACCEPT_RANGES, "bytes");
        responseHeaders.set(HttpHeaders.TRANSFER_ENCODING, "Binary");
        responseHeaders.set(HttpHeaders.ETAG, "W/\"" + id + "\"");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(out.length)
                .headers(responseHeaders)
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
