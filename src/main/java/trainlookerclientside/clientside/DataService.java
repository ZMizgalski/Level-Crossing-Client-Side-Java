package trainlookerclientside.clientside;

import com.diozero.api.RuntimeIOException;
import com.diozero.devices.PCA9685;
import com.diozero.devices.PwmLed;
import com.diozero.internal.spi.PwmOutputDeviceFactoryInterface;
import com.diozero.util.SleepUtil;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class DataService {

    public static int coverCounter = 0;

    public static ResponseEntity<?> move4Motors(String levelCrossingId,
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

    @SneakyThrows
    public static void recordVideo(int durationInSec) {
        if (coverCounter % 5 == 0) {
            getCover();
        }
        coverCounter++;
        Calendar currentUtilCalendar = Calendar.getInstance();
        SimpleDateFormat workDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/HH-mm-ss");
        String date = dateFormat.format(currentUtilCalendar.getTime());
        String workDate = workDateFormat.format(currentUtilCalendar.getTime());
        String format = "h264";
        String outFormat = "mp4";
        int duration = durationInSec * 1000;
        Process p1 = Runtime.getRuntime().exec("raspivid -w 640 -h 480 -n -t " + duration + " -o " + workDate + "." + format);
        p1.waitFor();
        Process p2 = Runtime.getRuntime().exec("MP4Box -add " + workDate + "." + format + " " + workDate + "." + outFormat);
        p2.waitFor();
        InputStream inputStream = new FileInputStream(workDate + "." + outFormat);
        FileUtils.copyInputStreamToFile(inputStream, new File("videos/" + date + "." + outFormat));
        inputStream.close();
        Process p3 = Runtime.getRuntime().exec("rm " + workDate + "." + format);
        p3.waitFor();
        Process p4 = Runtime.getRuntime().exec("rm " + workDate + "." + outFormat);
        p4.waitFor();
    }

    public static void deleteOldVideos(long filesRemovePeriod) {
        long DAY_IN_MS = 1000 * 60 * 60 * 24;
        Date date = new Date(System.currentTimeMillis() - (filesRemovePeriod * DAY_IN_MS));
        SimpleDateFormat monthDate = new SimpleDateFormat("yyyy/MM");
        String formattedMonthDate = monthDate.format(date);
        try {
            Files.walk(Paths.get("videos/" + formattedMonthDate)).filter(file -> {
                String fileName = file + "";
                String[] splitPath = fileName.split(Pattern.quote(File.separator));
                if (splitPath.length == 4) {
                    int dayFromFile = Integer.parseInt(splitPath[3]);
                    int daysBefore = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getDayOfMonth();
                    return dayFromFile < daysBefore;
                }
                return false;
            }).forEach(file -> {
                try {
                    FileUtils.cleanDirectory(file.toFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (isEmpty(file)) {
                    file.toFile().delete();
                }
            });
        } catch (IOException e) {
            //
        }
    }

    @SneakyThrows
    public static boolean isEmpty(Path path) {
        if (Files.isDirectory(path)) {
            try (Stream<Path> entries = Files.list(path)) {
                return entries.findFirst().isEmpty();
            }
        }
        return false;
    }


    public static void getCover() throws IOException, InterruptedException {
        String format = "jpg";
        String fileName = "cameraCover";
        try {
            if (new File(fileName + "." + format).exists()) {
                Process p3 = Runtime.getRuntime().exec("rm " + fileName + "." + format);
                p3.waitFor();
            }
        } catch (Exception e) {
            //
        }
        Process p1 = Runtime.getRuntime().exec("raspistill -w 640 -h 480 -n -o " + fileName + "." + format);
        p1.waitFor();
    }

}
