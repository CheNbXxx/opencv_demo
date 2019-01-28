import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author CheNbXxx
 * @description 修改方式，二次遍历保存视频
 * @email chenbxxx@gmail.con
 * @date 2019/1/28 10:40
 */
@Slf4j
public class ChangeWriteWayDemo {
    /** 视频截取线程池 */
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 4,
            20, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), r -> new Thread(r, "线程"));
    /** 输出视频个数 */
    private static AtomicInteger outNum = new AtomicInteger(0);
    /** 截取视频的总帧数*/
    private static final double VIDEO_FRAME_SIZE = 500;
    /** 帧处理间隔*/
    private static final int LEAPFROG_FRAME_SIZE = 50;

    private static final String FILE_NAME_PRE = "video";

    /**
     * 视频截取线程
     */
    static class VideoHandle implements Runnable{
        /** 需要保存的中间帧，截取前后 */
        private double frameIndex;
        private VideoCapture videoCapture;
        private VideoWriter videoWriter;

        VideoHandle(double frameIndex, String srcFile, String desDir, double fps, Size frameSize) {
            this.frameIndex = frameIndex;
            this.videoCapture = new VideoCapture(srcFile);
            this.videoWriter =  new VideoWriter(desDir+"\\"+FILE_NAME_PRE+"-"+outNum.incrementAndGet() + ".avi",
                    VideoWriter.fourcc('M','J','P','G'),
                    fps,
                    frameSize);
        }

        @Override
        public void run() {
            Thread.currentThread().setPriority(5);
            double value = frameIndex - VIDEO_FRAME_SIZE / 2;
            log.info("|| ======== 开启新线程，设置起始帧为:{}"+value);
            videoCapture.set(Videoio.CV_CAP_PROP_POS_FRAMES, value);
            Mat mat = new Mat();
            double sign = VIDEO_FRAME_SIZE;
            try {
                while (sign-- > 0) {
                    if (!videoCapture.read(mat)) {
                        return;
                    }
                    log.info("|| ======== 将第{}帧压入视频", videoCapture.get(Videoio.CAP_PROP_POS_FRAMES));
                    videoWriter.write(mat);
                    mat.release();
                }
            }finally {
                log.info("release VideoCapture & VideoWriter");
                videoCapture.release();
                videoWriter.release();
            }
        }
    }

    static {
        // 加载dll文件
        System.load("C:\\Users\\HuiShe\\Downloads\\opencv\\build\\java\\x64\\opencv_java400.dll");
    }

    public static void main(String[] args) throws Exception {
        new ChangeWriteWayDemo().handleVideo("D:\\Encode_1080P_4_7.mp4","D:");
        log.info("视频解析完毕");
    }

    private void handleVideo(final String srcFile,final String desDir) throws Exception {
        VideoCapture videoCapture = new VideoCapture(srcFile);
        if (!videoCapture.isOpened()) {
            log.info("视频无法打开");
            throw new RuntimeException("视频无法打开");
        }
        double fps = videoCapture.get(Videoio.CAP_PROP_FPS);
        Size frameSize = new Size(videoCapture.get(Videoio.CAP_PROP_FRAME_WIDTH), videoCapture.get(Videoio.CAP_PROP_FRAME_HEIGHT));
        double frameCount = videoCapture.get(Videoio.CAP_PROP_FRAME_COUNT);
        log.info("|| ==================   frameCount:[{}]",frameCount);
        log.info("|| ==================   fps:[{}]",fps);
        log.info("|| ==================   frameSize[ height:{},width:{}]",frameSize.height,frameSize.width);
        // 对比帧 取视频第一张
        Mat modelFrame = null;
        try {
            while (true) {
                // 原始帧
                Mat srcFrame = new Mat();
                // 处理后用于对比的帧
                Mat comparFrame = new Mat();
                // 二值化处理
                Mat thresh = new Mat();
                // 获取差值，存在diffFrame
                Mat diffFrame = new Mat();
                try {
                    if(! videoCapture.read(srcFrame)){
                        log.info("|| ============= 视频解析完毕");
                        return;
                    }
                    double frameIndex = videoCapture.get(Videoio.CAP_PROP_POS_FRAMES);
                    log.info("读取到:{}帧", frameIndex);
                    // 1. 高斯滤波,尽量平滑，参数未知
                    Imgproc.GaussianBlur(srcFrame, comparFrame, new Size(9, 9), 3, 3);
                    if (modelFrame == null) {
                        // 模板为空时，抽取第一幅为模板
                        modelFrame = new Mat(comparFrame.size(), CvType.CV_8U);
                        // 模板灰度化
                        Imgproc.cvtColor(comparFrame, modelFrame, Imgproc.COLOR_RGB2GRAY);
                    }
                    // 2. 比较帧灰度化
                    Imgproc.cvtColor(comparFrame, comparFrame, Imgproc.COLOR_RGB2GRAY);
                    Core.absdiff(comparFrame, modelFrame, diffFrame);
                    HighGui.imshow("差值视频阈值化：", diffFrame);
                    int keyboard = HighGui.waitKey(1);
                    if (keyboard == 'q' || keyboard == 27) {
                        break;
                    }
                    // 3. 二值化
                    Imgproc.threshold(diffFrame, thresh, 64, 255, Imgproc.THRESH_BINARY);
                    MatOfDouble matOfDouble = new MatOfDouble();
                    // 计算方差和平均差
                    Core.meanStdDev(diffFrame, new MatOfDouble(), matOfDouble);
                    // 取差值大于25的,触发录制
                    if (matOfDouble.toArray()[0] > 25) {
                        threadPoolExecutor.execute(new VideoHandle(frameIndex,srcFile,desDir,fps,frameSize));
                        for(int i = 0;i < VIDEO_FRAME_SIZE;i++){
                            if(!videoCapture.grab()){
                                return;
                            }
                        }
                    }
                    // 跳过50帧
                    for(int i = 0;i < LEAPFROG_FRAME_SIZE;i++){
                        if(!videoCapture.grab()){
                            return;
                        }
                    }
                }finally {
                    comparFrame.release();
                    diffFrame.release();
                    thresh.release();
                }
            }
        }finally {
            log.info("release VideoCapture");
            videoCapture.release();
        }
        HighGui.waitKey();
    }
}
