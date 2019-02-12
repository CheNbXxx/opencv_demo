package achieve;

/**
 * @author CheNbXxx
 * @description
 * @email chenbxxx@gmail.con
 * @date 2019/1/29 14:58
 */
public class Params {
    /** 截取视频的总大小*/
    Double frameSizeBefore = 300d;
    /** 截取视频的前后帧*/
    Double frameSizeRear = 200d;
    /** 间隔帧大小 */
    Double leapfrogSize= 50d;
    /** 处理后的文件前缀名称 */
    String fileNamePre = "vide";
    /** 原文件地址,可以是目录*/
    String srcFile = " ";
    /** 结果文件保存地址 */
    String desFile = "D:/";
    /** 筛选的阈值*/
    Integer threshold = 25;
    /** 是否播放跳帧视频 1播放 0不播放**/
    Integer showVideo = 0;
}
