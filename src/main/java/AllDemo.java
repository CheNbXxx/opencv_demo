import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

import java.util.concurrent.TimeUnit;

/**
 * @author CheNbXxx
 * @description
 * @email chenbxxx@gmail.con
 * @date 2019/1/25 9:53
 */
@Slf4j
public class AllDemo {

    private static final int CACHE_FRAME_SIZE = 225;
    private static final LoopChain<Mat> frame_cache;
    private static int outNum = 1;

    public static void main(String[] args) throws Exception {
//        new AllDemo().handleVideo("D:\\\\Encode_1080P_4_7.mp4","D:\\Encode_1080P_4_7.mp4");
        new AllDemo().handleVideo("C:\\Users\\HuiShe\\Downloads\\street.mov","D:\\Encode_1080P_4_7.mp4");
    }

    static {
        // 加载dll文件
        System.load("C:\\Users\\HuiShe\\Downloads\\opencv\\build\\java\\x64\\opencv_java400.dll");
        frame_cache = new LoopChain<>(CACHE_FRAME_SIZE);
    }

    private void handleVideo(String srcFile,String desDir) throws Exception {
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
        VideoWriter videoWriter =
                new VideoWriter(desDir+"result-view-"+outNum++ + ".avi",
                        VideoWriter.fourcc('M','J','P','G'),
                        fps,
                        frameSize);
        // 对比帧 取视频第一张
        Mat modelFrame = null;
        boolean flag = false;
        int overFrameSize = 0;
        int size = 50;
        while (true){
            // 原始帧
            Mat srcFrame = new Mat();
            // 处理后用于对比的帧
            Mat comparFrame = new Mat();
            videoCapture.read(srcFrame);
            if(srcFrame.empty()){
                break;
            }
            // 循环50帧
            while(size-- > 0){
                videoCapture.grab();
            }
            // 先将Mat存在Cache中
            frame_cache.add(srcFrame);
            if(flag){
                // 如果flag表示当前帧可以直接压入视频
                videoWriter.write(srcFrame);
                overFrameSize = overFrameSize == CACHE_FRAME_SIZE - 1 ? 0 : overFrameSize+1;
                if(overFrameSize == CACHE_FRAME_SIZE){
                    flag = false;
                    break;
                }
                continue;
            }
            // 高斯滤波,尽量平滑，参数未知
            Imgproc.GaussianBlur(srcFrame, comparFrame, new Size(9, 9), 3, 3);
            if (modelFrame == null) {
                // 模板为空时，抽取第一幅为模板
                modelFrame = new Mat(comparFrame.size(), CvType.CV_8U);
                // 模板灰度化
                Imgproc.cvtColor(comparFrame, modelFrame, Imgproc.COLOR_RGB2GRAY);
            }
            // 比较帧灰度化
            Imgproc.cvtColor(comparFrame, comparFrame, Imgproc.COLOR_RGB2GRAY);
            // 获取差值，存在diffFrame
            Mat diffFrame = new Mat();
            Core.absdiff(comparFrame, modelFrame, diffFrame);
            Mat thresh = new Mat();
            // 阈值化还是啥的
            Imgproc.threshold(diffFrame, thresh, 64, 255, Imgproc.THRESH_BINARY);
            MatOfDouble matOfDouble = new MatOfDouble();
            Core.meanStdDev(diffFrame, new MatOfDouble(), matOfDouble);
            // 取差值大于25的,触发录制
            if (matOfDouble.toArray()[0] > 25) {
                // 先将cache的压入视频,带上了当前帧
                frame_cache.valuesAsList().forEach(videoWriter::write);
                // 清空
                frame_cache.clear();
                flag = true;
            }
        }
        videoCapture.release();
        videoWriter.release();
    }
}
