package demo;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

/**
 * @author CheNbXxx
 * @description
 * @email chenbxxx@gmail.con
 * @date 2019/1/24 14:41
 */
@Slf4j
public class VideoWriterDemo {
    static {
        // 加载dll文件
        System.load("C:\\Users\\HuiShe\\Downloads\\opencv\\build\\java\\x64\\opencv_java400.dll");
    }

    public static void main(String[] args) {
        VideoCapture videoCapture = new VideoCapture("D:\\Encode_1080P_4_7.mp4");
        if (!videoCapture.isOpened()) {
            log.info("视频无法打开");
            return;
        }
        double fps = videoCapture.get(Videoio.CAP_PROP_FPS);
        Size frameSize = new Size(videoCapture.get(Videoio.CAP_PROP_FRAME_WIDTH), videoCapture.get(Videoio.CAP_PROP_FRAME_HEIGHT));
        double v = videoCapture.get(Videoio.CAP_PROP_FRAME_COUNT);
        log.info("frameCount:"+v);
        log.info("fps:"+fps);
        log.info("frameSize[ height:"+frameSize.height+"width:"+frameSize.width+"]");
        VideoWriter videoWriter =
                new VideoWriter("D:\\Encode_1080P_4_7_copy1.avi",
                        VideoWriter.fourcc('M','J','P','G'),
                        fps,
                        frameSize);

        while (true){
            Mat temp = new Mat();
            videoCapture.read(temp);
            if(temp.empty() || videoCapture.get(Videoio.CAP_PROP_POS_FRAMES) == 1000){
                break;
            }
            HighGui.imshow("拷贝",temp);
            videoWriter.write(temp);
            int keyboard = HighGui.waitKey(20);
            if (keyboard == 'q' || keyboard == 27) {
                break;
            }
        }
        HighGui.waitKey(10);
    }
}
