package achieve;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

import java.io.File;
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
public class DoVideo {
    /** 视频截取线程池 */
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 4,
            20, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), r -> new Thread(r, "线程"));
    /** 输出视频个数 */
    private static AtomicInteger outNum = new AtomicInteger(0);
    /** 参数实例*/
    private final Params params;

    /**
     * 视频截取线程
     */
    private class VideoHandleRunnable implements Runnable{
        /** 需要保存的中间帧，截取前后 */
        private double frameIndex;
        private VideoCapture videoCapture;
        private VideoWriter videoWriter;

        VideoHandleRunnable(double frameIndex,String srcFile, String desDir, double fps, Size frameSize) {
            this.frameIndex = frameIndex;
            this.videoCapture = new VideoCapture(srcFile);
            this.videoWriter =  new VideoWriter(params.desFile+"\\"+params.fileNamePre+"-"+outNum.incrementAndGet()+"-"+videoCapture.get(0)+ ".avi",
                    VideoWriter.fourcc('M','J','P','G'),
                    fps,
                    frameSize);
        }

        @Override
        public void run() {
            Thread.currentThread().setPriority(5);
            double value = frameIndex - params.frameSizeBefore;
            log.info("|| ======== 开启新线程，设置起始帧为:{}"+value);
            videoCapture.set(Videoio.CV_CAP_PROP_POS_FRAMES, value);
            Mat mat = new Mat();
            double sign = params.frameSizeBefore+params.frameSizeRear;
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

    DoVideo(Params params){
        this.params = params;
        // 加载dll文件
        String property = System.getProperty("user.dir");
        log.info(new File(property).getAbsolutePath());
//        System.load(new File(property).getAbsolutePath()+"\\src\\main\\java\\lib\\opencv_java400.dll");
        System.load(new File(property).getAbsolutePath()+"\\classes\\opencv_java400.dll");

    }

    public void handleVideo(final String srcFile,final String desDir) throws Exception {
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
                    // 1. 均值模糊
                    Imgproc.blur(srcFrame, comparFrame, new Size(9, 9));
                    HighGui.imshow("均值模糊后：", comparFrame);
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
                    if (matOfDouble.toArray()[0] > params.threshold) {
                        threadPoolExecutor.execute(new VideoHandleRunnable(frameIndex,srcFile,desDir,fps,frameSize));
                        for(int i = 0;i < params.frameSizeBefore+params.frameSizeRear;i++){
                            if(!videoCapture.grab()){
                                return;
                            }
                        }
                    }
                    // 跳过50帧
                    for(int i = 0;i < params.leapfrogSize;i++){
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
