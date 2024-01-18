package com.example.margevideoio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
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

    /**
     * 获取鸟瞰图
     */
    fun getVideBird(src: Mat, srcPoint: Mat, dstPoint: Mat, size: Size): Mat {
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2BGRA)
        val trapezoidImage = Mat(size, CvType.CV_8UC4)
        //映射到输出图像上的4个点
        val perspectiveTransform = Imgproc.getPerspectiveTransform(srcPoint, dstPoint)
        Imgproc.warpPerspective(
            src,
            trapezoidImage,
            perspectiveTransform,
            size
        )
        return trapezoidImage
    }

    //自由裁剪
    fun cropImageFree(mFileBitmap: Bitmap, points: List<Point>): Bitmap? {
        val resultingImage = Bitmap.createBitmap(
            mFileBitmap.getWidth(),
            mFileBitmap.getHeight(), mFileBitmap.getConfig()
        )
        val canvas = Canvas(resultingImage)
        val paint = Paint()
        paint.isAntiAlias = true
        val path = Path()
        for (i in 0 until points.size) {
            val x: Float = points.get(i).x.toFloat()
            val y: Float = points.get(i).y.toFloat()
            path.lineTo(x, y)
        }
        canvas.drawPath(path, paint)
        paint.xfermode =
            PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(mFileBitmap, 0.0f, 0.0f, paint)

        //保存裁剪后的bitmap
        //        new ScreenFiles(this).saveScreenshotToFile(bitmap);
        return resultingImage
    }
}
