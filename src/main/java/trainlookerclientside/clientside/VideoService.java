package trainlookerclientside.clientside;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import io.humble.video.*;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;

@Service
public class VideoService {

    public static BufferedImage convertToType(BufferedImage sourceImage, int targetType) {
        BufferedImage image;
        if (sourceImage.getType() == targetType)
            image = sourceImage;
        else {
            image = new BufferedImage(
                    sourceImage.getWidth(),
                    sourceImage.getHeight(), targetType);
            image.getGraphics().drawImage(sourceImage, 0, 0, null);
        }
        return image;
    }

    @SneakyThrows
    public void recordVideo(Webcam webcam, String filename, String formatName, String codecName, int duration, int snapsPerSecond) {
        webcam.setViewSize(WebcamResolution.VGA.getSize());
        final Rectangle size = new Rectangle(webcam.getViewSize());
        final Rational frameRate = Rational.make(1, snapsPerSecond);
        final Muxer muxer = Muxer.make(filename, null, formatName);
        final MuxerFormat format = muxer.getFormat();
        final Codec codec;
        if (codecName != null) {
            codec = Codec.findEncodingCodecByName(codecName);
        } else {
            codec = Codec.findEncodingCodec(format.getDefaultVideoCodecId());
        }
        Encoder encoder = Encoder.make(codec);
        encoder.setWidth(size.width);
        encoder.setHeight(size.height);
        final PixelFormat.Type pixelFormat = PixelFormat.Type.PIX_FMT_YUV420P;
        encoder.setPixelFormat(pixelFormat);
        encoder.setTimeBase(frameRate);
        if (format.getFlag(MuxerFormat.Flag.GLOBAL_HEADER))
            encoder.setFlag(Encoder.Flag.FLAG_GLOBAL_HEADER, true);
        encoder.open(null, null);
        muxer.addNewStream(encoder);
        muxer.open(null, null);
        MediaPictureConverter converter = null;
        final MediaPicture picture = MediaPicture
                .make(
                        encoder.getWidth(),
                        encoder.getHeight(),
                        pixelFormat);
        picture.setTimeBase(frameRate);
        webcam.open();
        final MediaPacket packet = MediaPacket.make();
        for (int i = 0; i < duration / frameRate.getDouble(); i++) {
            final BufferedImage image = webcam.getImage();
            final BufferedImage frame = convertToType(image, BufferedImage.TYPE_3BYTE_BGR);
            if (converter == null) {
                converter = MediaPictureConverterFactory.createConverter(frame, picture);
            }
            converter.toPicture(picture, frame, i);
            do {
                encoder.encode(packet, picture);
                if (packet.isComplete()) {
                    muxer.write(packet, false);
                }
            } while (packet.isComplete());
            Thread.sleep((long) (1000 * frameRate.getDouble()));
        }
        do {
            encoder.encode(packet, null);
            if (packet.isComplete()) {
                muxer.write(packet, false);
            }
        } while (packet.isComplete());
        webcam.close();
        muxer.close();
    }
}
