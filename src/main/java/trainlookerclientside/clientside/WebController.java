package trainlookerclientside.clientside;

import com.diozero.api.RuntimeIOException;
import com.diozero.devices.PCA9685;
import com.diozero.devices.PwmLed;
import com.diozero.internal.spi.PwmOutputDeviceFactoryInterface;
import com.diozero.util.SleepUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${motor1.pin}")
    private String motor1Port;
    @Value("${motor2.pin}")
    private String motor2Port;
    @Value("${motor3.pin}")
    private String motor3Port;
    @Value("${motor4.pin}")
    private String motor4Port;
    @Value("#{T(Float).parseFloat('${motor.delay}')}")
    private float motorDelay;
    @Value("#{T(Float).parseFloat('${open.pwm}')}")
    private float openPwm;
    @Value("#{T(Float).parseFloat('${close.pwm}')}")
    private float closePwm;
    @Value("${pwm.frequency}")
    private String pwmFrequency;

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
        return move4Motors(id,
                "Level crossing opened with id: %s",
                "Something wrong with motors when opening levelCrossing with id: %s",
                motor1Port,
                motor2Port,
                motor3Port,
                motor4Port,
                motorDelay,
                openPwm,
                pwmFrequency,
                false);
    }

    @RequestMapping(value = "/closeLevelCrossing/{id}")
    public ResponseEntity<?> closeLevelCrossing(@PathVariable String id) {
        return move4Motors(id,
                "Level crossing closed with id: %s",
                "Something wrong with motors when closing levelCrossing with id: %s",
                motor1Port,
                motor2Port,
                motor3Port,
                motor4Port,
                motorDelay,
                closePwm,
                pwmFrequency,
                true);
    }

    private ResponseEntity<?> move4Motors(String levelCrossingId,
                                          String okResponseMessage,
                                          String badResponseMessage,
                                          String motor1Port,
                                          String motor2Port,
                                          String motor3Port,
                                          String motor4Port,
                                          float delay,
                                          float pwmValue,
                                          String pwmFrequency,
                                          boolean close) {
        int mot1 = motor1Port == null ? 0 : Integer.parseInt(motor1Port);
        int mot2 = motor2Port == null ? 1 : Integer.parseInt(motor2Port);
        int mot3 = motor3Port == null ? 2 : Integer.parseInt(motor3Port);
        int mot4 = motor4Port == null ? 3 : Integer.parseInt(motor4Port);
        int pwmFreq = pwmFrequency == null ? 50 : Integer.parseInt(pwmFrequency);
        float fPwm = pwmValue == 0 ? 0.2f : pwmValue;
        float fDelay = delay == 0 ? 0.5f : delay;
        try (PwmOutputDeviceFactoryInterface df = new PCA9685(pwmFreq);
             PwmLed led1 = new PwmLed(df, mot1);
             PwmLed led2 = new PwmLed(df, mot2);
             PwmLed led3 = new PwmLed(df, mot3);
             PwmLed led4 = new PwmLed(df, mot4)
        ) {
            led1.setValue(fPwm);
            led2.setValue(fPwm);
            if (!close) {
                led2.toggle();
            }
            led3.setValue(fPwm);
            if (!close) {
                led3.toggle();
            }
            led4.setValue(fPwm);
            SleepUtil.sleepSeconds(fDelay);
        } catch (RuntimeIOException e) {
            return ResponseEntity.badRequest().body(String.format(badResponseMessage, levelCrossingId));
        }
        return ResponseEntity.ok().body(String.format(okResponseMessage, levelCrossingId));
    }
}
