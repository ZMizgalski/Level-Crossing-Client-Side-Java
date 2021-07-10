package trainlookerclientside.clientside;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trainlookerclientside.clientside.models.LogsModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static trainlookerclientside.clientside.DataService.move4Motors;
import static trainlookerclientside.clientside.DataService.tmpFolderPath;

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
    @GetMapping(value = "/getStreamCover")
    public ResponseEntity<?> streamCover() {
        String fileName = "cameraCover";
        String format = "jpg";
        File file = new File("videos/tmp/" + fileName + "." + format);
        if (!file.exists()) {
            return ResponseEntity.badRequest().body("file not exists!");
        }
        byte[] bytes = Files.readAllBytes(file.toPath());
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "filename=\"" + fileName + "." + format + "\"");
        responseHeaders.set(HttpHeaders.CONTENT_RANGE, "" + (bytes.length - 1));
        responseHeaders.set(HttpHeaders.ACCEPT_RANGES, "bytes");
        responseHeaders.set(HttpHeaders.TRANSFER_ENCODING, "Binary");
        responseHeaders.set(HttpHeaders.ETAG, "W/\"" + fileName + "\"");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(bytes.length)
                .headers(responseHeaders)
                .body(bytes);
    }


    @SneakyThrows
    @GetMapping(value = "/streamCamera/{id}")
    public ResponseEntity<?> streamCamera(@PathVariable String id) {
        Process p0 = Runtime.getRuntime().exec("pkill raspivid");
        p0.waitFor();
        String format = "h264";
        String outFormat = "mp4";
        Process p1 = Runtime.getRuntime().exec("raspivid -w 640 -h 480 -n -t 5000 -o "+ tmpFolderPath + id + "." + format);
        p1.waitFor();
        Process p2 = Runtime.getRuntime().exec("MP4Box -add "+ tmpFolderPath + id + "." + format + " "+ tmpFolderPath + id + "." + outFormat);
        p2.waitFor();
        InputStream inputStream = new FileInputStream(tmpFolderPath + id + "." + outFormat);
        byte[] out = org.apache.commons.io.IOUtils.toByteArray(inputStream);
        inputStream.close();
        Process p3 = Runtime.getRuntime().exec("rm "+ tmpFolderPath + id + "." + format);
        p3.waitFor();
        Process p4 = Runtime.getRuntime().exec("rm "+ tmpFolderPath + id + "." + outFormat);
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

    @SneakyThrows
    @GetMapping(value = "/getFileByDate/{date}")
    public ResponseEntity<?> getFile(@PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd_HH-mm-ss") Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/HH-mm-ss");
        String formattedDate = dateFormat.format(date);
        HttpHeaders headers = new HttpHeaders();
        File file = new File("videos/" + formattedDate + ".mp4");
        if (!file.exists()) {
            return ResponseEntity.badRequest().body("file not exists!");
        }
        byte[] bytes = Files.readAllBytes(file.toPath());
        String type = FilenameUtils.getExtension(dateFormat + ".mp4");
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("video/" + type))
                .header(HttpHeaders.CONTENT_DISPOSITION, "filename=\"" + file.getName() + "\"")
                .body(bytes);
    }

    @SneakyThrows
    @GetMapping(value = "/downloadFileByDate/{date}")
    public ResponseEntity<?> downloadFile(@PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd_HH-mm-ss") Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/HH-mm-ss");
        String formattedDate = dateFormat.format(date);
        HttpHeaders headers = new HttpHeaders();
        File file = new File("videos/" + formattedDate + ".mp4");
        if (!file.exists()) {
            return ResponseEntity.badRequest().body("file not exists!");
        }
        byte[] bytes = Files.readAllBytes(file.toPath());
        String type = FilenameUtils.getExtension(dateFormat + ".mp4");

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("video/" + type))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + ".mp4" + "\"")
                .body(bytes);
    }

    @SneakyThrows
    @GetMapping(value = "/getFilesByDay/{date}/{id}")
    public ResponseEntity<?> getFiles(@PathVariable("id") String id, @PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String formattedDate = dateFormat.format(date);
        Set<String> fileNames = Stream.of(Objects.requireNonNull(new File("videos/" + formattedDate).listFiles()))
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toSet());
        List<LogsModel> logs = new ArrayList<>();
        fileNames.parallelStream().forEach( value -> logs.add(new LogsModel(id, new SimpleDateFormat("yyyy-MM-dd").format(date) + "_" + value.replaceAll(".mp4", ""))));
        return ResponseEntity.ok().body(logs);
    }
}
