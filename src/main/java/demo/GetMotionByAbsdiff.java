package demo;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.CvType;

public class GetMotionByAbsdiff {
	   public static void main(String arg[]){
		String path = "C:\\Users\\HuiShe\\Downloads\\opencv\\build\\java\\x64\\opencv_java400.dll";
		System.load(path);
	     JFrame frame1 = new JFrame("Camera");  
	     frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
	     frame1.setSize(640,480);  
	     frame1.setBounds(0, 0, frame1.getWidth(), frame1.getHeight());  
	     Panel panel1 = new Panel();
	     frame1.setContentPane(panel1);  
	     frame1.setVisible(true);  
 
	     JFrame frame2 = new JFrame("Threshold");  
	     frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
	     frame2.setSize(640,480);  
	     frame2.setBounds(300,100, frame1.getWidth()+50, 50+frame1.getHeight());  
	     Panel panel2 = new Panel();  
	     frame2.setContentPane(panel2);      
	     frame2.setVisible(true);  
	     //-- 2. Read the video stream  
	     VideoCapture capture = new VideoCapture("C:\\Users\\HuiShe\\Downloads\\street.mov");
//	     capture.open(0);
	     Mat webcam_image=new Mat();  
	      capture.read(webcam_image);  
	      frame1.setSize(webcam_image.width()+40,webcam_image.height()+60);  
	      frame2.setSize(webcam_image.width()+40,webcam_image.height()+60);  

	      Mat image = null;
	      Mat prevImage = null;
	      Mat diff = null;
	      Mat mHierarchy = new Mat();
	      
	     if( capture.isOpened()) {
	      while( true )  
	      {
	      	// 读取当前帧到webcam_image
	        capture.read(webcam_image);
	        if( !webcam_image.empty() )
	         {
	         	// 高斯滤波，模糊平滑
	           Imgproc.GaussianBlur(webcam_image, webcam_image, new Size(9,9), 0, 0);

	           // 将当前帧图片赋值给image，将webcam赋值给prevImage
	           if(image==null){
	        	   image = new Mat(webcam_image.size(),CvType.CV_8U);
	        	   // 转化为灰度图
	        	   Imgproc.cvtColor(webcam_image, image, Imgproc.COLOR_RGB2GRAY);
	           }else{
	        	   prevImage = new Mat(webcam_image.size(),CvType.CV_8U);
	               
		           prevImage = image;
	               
		           image = new Mat(webcam_image.size(),CvType.CV_8U);
		           Imgproc.cvtColor(webcam_image, image, Imgproc.COLOR_RGB2GRAY);
		           
	           }
	           
	          if(diff==null){
	        	  diff = new Mat(webcam_image.size(),CvType.CV_8U);
	          }
	          
	           if (prevImage != null) {
	        	   // 计算不同，当前帧和前一帧图像的区别，结果放在diff
	        	   Core.absdiff(image, prevImage, diff);
	                
	        	   Imgproc.threshold(diff, diff, 64, 255, Imgproc.THRESH_BINARY);
//	        	   Core.meanStdDev(diff,);
	        	   List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
                    // 边缘提取
	                Imgproc.findContours(diff, contours, mHierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
	                for(int i=0;i<contours.size();i++)
	            	{
	            	Rect r = Imgproc.boundingRect(contours.get(i));
	            	int contourArea = r.height* r.width;
	            	if(contourArea > 500)
	            	{
	            	Imgproc.drawContours(webcam_image, contours,i,new Scalar(255,0,0,255),-1);
	            	//補捉到使用藍色表示
	            	Imgproc.rectangle(webcam_image, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height), new Scalar(0, 255, 0));
	            	}

	            	}
	               
	           }          
	          panel1.setimagewithMat(webcam_image);  
	          panel2.setimagewithMat(diff);
	          frame1.repaint();  
	          frame2.repaint();  
	         }  
	         else  
	         {  
	           System.out.println(" 無補抓任何畫面!");  
	           break;  
	         }  
	        }  
	       }  
	     return;  
	   } 
	 }   
