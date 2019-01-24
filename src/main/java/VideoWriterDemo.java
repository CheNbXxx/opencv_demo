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
    private static final int CACHE_FRAME_SIZE =100;
    static {
        // 加载dll文件
        System.load("C:\\Users\\HuiShe\\Downloads\\opencv\\build\\java\\x64\\opencv_java400.dll");
    }

    /**
     * 计划构建一个环链保存数据前n帧的内容
     */
    static class MatNode{
        Mat mat;
        MatNode next;
        MatNode(Mat mat,MatNode next){
            this.mat = mat;
            this.next = next;
        }
    }
    public static void main(String[] args) {
        MatNode head = null;
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

        int nodeSize = 0;
        MatNode tail = null;
        while (true){
            Mat temp = new Mat();
            videoCapture.read(temp);
            if(temp.empty() || videoCapture.get(Videoio.CAP_PROP_POS_FRAMES) == 1000){
                break;
            }
            HighGui.imshow("拷贝",temp);
            videoWriter.write(temp);
            // 加入到MatNode的链表中
            if(nodeSize < CACHE_FRAME_SIZE){
                nodeSize++;
                if(tail == null){
                    tail = new MatNode(temp,null);
                    head = tail;
                }else{
                    tail.next = new MatNode(temp,null);
                    tail = tail.next;
                }
            }else if(nodeSize == CACHE_FRAME_SIZE){
                // 形成一个闭环
                tail.next = head;
            }else{
                // 头节点标记后移,覆盖当前节点
                head = new MatNode(temp,head.next);
                head = head.next;
            }
            int keyboard = HighGui.waitKey(20);
            if (keyboard == 'q' || keyboard == 27) {
                break;
            }
        }
        HighGui.waitKey(10);
    }
}
