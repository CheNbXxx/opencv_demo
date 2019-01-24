import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.util.concurrent.TimeUnit;

/**
 * @author CheNbXxx
 * @description
 * @email chenbxxx@gmail.con
 * @date 2019/1/23 13:55
 */
@Slf4j
public class MyDemo {
    static {
        // 加载dll文件
        System.load("C:\\Users\\HuiShe\\Downloads\\opencv\\build\\java\\x64\\opencv_java400.dll");
    }

    public static void main(String[] args) throws InterruptedException {
        new MyDemo().run();
    }

    public void run() throws InterruptedException {
        // 指定高斯混合模型为基础的背景/前景分隔算法
//        BackgroundSubtractor backgroundSubtractor = Video.createBackgroundSubtractorMOG2();

        // 指定视频并验证是否能打开
//        VideoCapture videoCapture = new VideoCapture("C:\\Users\\HuiShe\\Downloads\\street.mov");
        VideoCapture videoCapture = new VideoCapture("D:\\Encode_1080P_4_7.mp4");
        try {
            if (!videoCapture.isOpened()) {
                log.info("视频无法打开");
                return;
            }
            Mat currentFrame = new Mat();
            Mat srcFrame = new Mat();
            Mat modelFrame = null;
            Mat diff = new Mat();
            // 无限循环 取第一帧为辨别模板
            while (true) {
                // 读取当前帧到frame
                videoCapture.read(srcFrame);
                if (srcFrame.empty()) {
                    log.info("未读取到帧");
                    break;
                }
                // 高斯滤波,尽量平滑，参数未知
                Imgproc.GaussianBlur(srcFrame, currentFrame, new Size(9, 9), 3, 3);
                if (modelFrame == null) {
                    // 模板为空时，抽取第一幅为模板
                    modelFrame = new Mat(currentFrame.size(), CvType.CV_8U);
                    // 模板灰度化
                    Imgproc.cvtColor(currentFrame, modelFrame, Imgproc.COLOR_RGB2GRAY);
                }
                // 当前帧灰度化
                Imgproc.cvtColor(currentFrame, currentFrame, Imgproc.COLOR_RGB2GRAY);
                // 获取差值
                Core.absdiff(currentFrame, modelFrame, diff);
                Mat thresh = new Mat();
                // 阈值化还是啥的
                Imgproc.threshold(diff, thresh, 64, 255, Imgproc.THRESH_BINARY);
                MatOfDouble matOfDouble = new MatOfDouble();
                Core.meanStdDev(diff, new MatOfDouble(), matOfDouble);

                // 取差值大于25的，25待调整，拉慢速度
                if (matOfDouble.toArray()[0] > 25) {
                    TimeUnit.SECONDS.sleep(1);
                    String frameNumberString = "someting moving, matOfDouble:" + matOfDouble.toArray()[0];
                    Imgproc.putText(srcFrame, frameNumberString, new Point(15, 200), 4, 0.5,
                            new Scalar(0, 0, 0));
                }

                // 显示当前帧数
                Imgproc.rectangle(srcFrame, new Point(10, 2), new Point(100, 20), new Scalar(255, 255, 255), -1);
                String frameNumberString = String.format("%d", (int) videoCapture.get(Videoio.CAP_PROP_POS_FRAMES));
                Imgproc.putText(srcFrame, frameNumberString, new Point(15, 15), 4, 0.5,
                        new Scalar(0, 0, 0));

                // 显示当前差值
                Imgproc.rectangle(srcFrame, new Point(10, 30), new Point(250, 62), new Scalar(255, 255, 255), -1);
                String string = "" + matOfDouble.toArray()[0];
                Imgproc.putText(srcFrame, string, new Point(15, 50), 4, 0.5, new Scalar(0, 0, 0));

                // show the current frame and the fg masks
                // 视频展示
                HighGui.imshow("原视频：", srcFrame);
//            HighGui.imshow("模板图片：", modelFrame);
//            HighGui.imshow("差值视频：", diff);
//            HighGui.imshow("差值视频阈值化：", thresh);
                // get the input from the keyboard
                int keyboard = HighGui.waitKey(1);
                if (keyboard == 'q' || keyboard == 27) {
                    break;
                }
            }
        }finally {
            videoCapture.release();
        }

        HighGui.waitKey();
    }
}
