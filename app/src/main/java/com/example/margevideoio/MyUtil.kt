package com.example.margevideoio

import android.content.Context
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException


class MyUtil {
    // private Mat readMatFromFile(String filePath) {
    //        Mat mat = null;
    //
    //        try {
    //            BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
    //
    //            // 读取行数和列数
    //            int rows = Integer.parseInt(bufferedReader.readLine());
    //            int cols = Integer.parseInt(bufferedReader.readLine());
    //
    //            // 创建Mat对象
    //            mat = new Mat(rows, cols, CvType.CV_64F);
    //
    //            // 读取数据并填充Mat对象
    //            for (int i = 0; i < rows; i++) {
    ////                String[] values = bufferedReader.readLine().split(" ");
    //                for (int j = 0; j < cols; j++) {
    //                    double value = Double.parseDouble(bufferedReader.readLine().trim());
    //                    mat.put(i, j, value);
    //                }
    //            }
    //
    //            bufferedReader.close();
    //        } catch (IOException e) {
    //            e.printStackTrace();
    //        }
    //
    //        return mat;
    //    }
    fun readMatFromPath(filePath: String): Mat {
        var mat: Mat? = null
        try {
            val bufferReader = BufferedReader(FileReader(filePath))
            val rows = bufferReader.readLine().toInt()
            val cols = bufferReader.readLine().toInt()
            mat = Mat(rows, cols, CvType.CV_64F)
            for (i in (0..<rows)) {
                for (j in (0..<cols)) {
                    val value = bufferReader.readLine().trim().toDouble()
                    mat.put(i, j, value)
                }
            }
            bufferReader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return mat!!
    }


    fun saveScreenshotToTemp(mat: Mat?, context: Context) {
        val galleryPath =
            context.cacheDir.absolutePath + File.separator + "zyCache" + File.separator + "captured"
        val file = File(galleryPath)
        if (!file.exists()) {
            file.mkdirs()
        }
        val file1 = File(file.absolutePath, System.currentTimeMillis().toString() + ".png")
        Imgcodecs.imwrite(file1.absolutePath, mat)
    }

    fun trapezoidTransform(src: Mat, dstPoints: MatOfPoint2f): Mat {
        // 获取图像的高度和宽度
        val height = src.rows()
        val width = src.cols()

        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2BGRA)
        // 定义梯形的四个顶点
        val srcPoints = MatOfPoint2f(
            Point(0.0, height.toDouble()),           // 左下
            Point(width.toDouble(), height.toDouble()),  // 右下
            Point(width.toDouble(), 0.0),              // 右上
            Point(0.0, 0.0)                     // 左上
        )
        // 计算透视变换矩阵
        val perspectiveMatrix = Imgproc.getPerspectiveTransform(srcPoints, dstPoints)

        // 应用透视变换
        val trapezoidImage = Mat(src.size(), CvType.CV_8UC4)
        Imgproc.warpPerspective(
            src,
            trapezoidImage,
            perspectiveMatrix,
            Size(width.toDouble(), height.toDouble())
        )

        return trapezoidImage
    }


    fun getBirdView(src: Mat): Mat {
        // 获取图像的高度和宽度
        val height = src.rows()
        val width = src.cols()
        val shiftW: Double = 300.0
        val shiftH: Double = 300.0


        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2BGRA)
        // 定义梯形的四个顶点
        val srcPoints = MatOfPoint2f(
//            Point(0.0, height.toDouble()),           // 左下
//            Point(width.toDouble(), height.toDouble()),  // 右下
//            Point(width.toDouble(), height.toDouble() / 2),              // 右上
//            Point(0.0, height.toDouble() / 2)                     // 左上
            Point(300.0, 300.0),
            Point(600.0, 300.0),
            Point(800.0, 600.0),
            Point(100.0, 600.0)
        )
//val beforePoints = MatOfPoint(
//                //            Point(0.0, height.toDouble()),           // 左下
////            Point(width.toDouble(), height.toDouble()),  // 右下
////            Point(width.toDouble(), 0.0),              // 右上
////            Point(0.0, 0.0)                           // 左上
//                Point(frame1.width().toDouble() * 0.35, frame1.height().toDouble()),
//                Point(frame1.width().toDouble() * 0.65, frame1.height().toDouble()),
//                Point(frame1.width().toDouble(), 0.0),
//                Point(0.0, 0.0)
//            )
        val dstPoints = MatOfPoint2f(
            Point(shiftW + 120, shiftH),//左上
            Point(shiftW + 480, shiftH),//右上
            Point(shiftW + 480, shiftH + 160),                     // 右下
            Point(shiftW + 120, shiftH + 160)              // 左下
        )
        // 计算透视变换矩阵
        val perspectiveMatrix = Imgproc.getPerspectiveTransform(srcPoints, dstPoints)


        // 应用透视变换
        val trapezoidImage = Mat(src.size(), CvType.CV_8UC4)
        Imgproc.warpPerspective(
            src,
            trapezoidImage,
            perspectiveMatrix,
            Size(width.toDouble(), height.toDouble())
        )

        return trapezoidImage

    }

    fun getVideBird(src: Mat, srcPoint: Mat, dstPoint: Mat, size: Size): Mat {
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2BGRA)
        val trapezoidImage = Mat(size, CvType.CV_8UC4)
        val perspectiveTransform = Imgproc.getPerspectiveTransform(srcPoint, dstPoint)
        Imgproc.warpPerspective(
            src,
            trapezoidImage,
            perspectiveTransform,
            size
        )

        return trapezoidImage
    }

}
