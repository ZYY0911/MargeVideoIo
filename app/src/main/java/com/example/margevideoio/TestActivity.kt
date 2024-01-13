package com.example.margevideoio

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.scale
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.calib3d.Calib3d
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.opencv.videoio.VideoCapture
import java.io.File

class TestActivity : AppCompatActivity() {
    private val TAG = "TestVideo"
    private var mSv: SurfaceView? = null
    private var mUtil: MyUtil? = null
    private var cameraMatrix: Mat? = null
    private var rootPath: String? = null
    private var distCoeffs: Mat? = null
    private var context: Context? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        context = this
        mUtil = MyUtil()
        rootPath = cacheDir.absolutePath + File.separator
        if (OpenCVLoader.initLocal()) {
            Log.i(TAG, "OpenCV loaded successful")
            Toast.makeText(this, "OpenCV loaded successful", Toast.LENGTH_SHORT).show()
        } else {
            Log.e(TAG, "OpenCV initialization failed!")
            Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG).show()
            return
        }
        cameraMatrix = mUtil!!.readMatFromPath("${rootPath}cameraMatrix.txt")
        distCoeffs = mUtil!!.readMatFromPath("${rootPath}distCoeffs.txt")
        Thread {
            loadVideoIo()
        }.start()
    }

    private fun loadVideoIo() {
        val rootVideo =
            cacheDir.absolutePath + File.separator + "Video" + File.separator
        var video1 = VideoCapture("${rootVideo}video1.mp4")
        if (!video1.isOpened) {
            Log.e(TAG, "loadVideoIo: Error")
            return
        }
        while (true) {
            val frame1 = Mat()
            var src1 = Mat()
            video1.read(src1)
            if (src1.empty()) {
                Log.i(TAG, "loadVideoIo: Video is Empty")
                break
            }
            //去畸
            Calib3d.undistort(src1, frame1, cameraMatrix, distCoeffs)
            // 定义输出梯形的四个顶点
            val dstPoints = MatOfPoint2f(
//            Point(0.0, height.toDouble()),           // 左下
//            Point(width.toDouble(), height.toDouble()),  // 右下
//            Point(width.toDouble(), 0.0),              // 右上
//            Point(0.0, 0.0)                           // 左上
                Point(0.0, frame1.height().toDouble()),  // 左下角
                Point(frame1.width().toDouble(), frame1.height().toDouble()),  // 右下角
                Point(frame1.width().toDouble() * 0.65, frame1.height().toDouble() * 0.55),  // 右上角
                Point(frame1.width().toDouble() * 0.35, frame1.height().toDouble() * 0.55)  // 左上角
            )
            val trapezoidTransform = trapezoidTransform(frame1,dstPoints)
            mUtil!!.saveScreenshotToTemp(trapezoidTransform, context!!)
            val showBitmap = Bitmap.createBitmap(
                frame1.width(), frame1.height(), Bitmap.Config.ARGB_8888
            )
            Utils.matToBitmap(frame1, showBitmap)
            runOnUiThread {
                val canvas = mSv!!.holder.lockCanvas()
                val finalBitmap = showBitmap.scale(mSv!!.width, mSv!!.height)
                canvas.drawBitmap(finalBitmap, 0f, 0f, null)
                mSv!!.holder.unlockCanvasAndPost(canvas)
            }
        }

    }


    fun trapezoidTransform(src: Mat,dstPoints: MatOfPoint2f ): Mat {

        // 获取图像的高度和宽度
        val height = src.rows()
        val width = src.cols()

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
        val trapezoidImage = Mat()
        Imgproc.warpPerspective(
            src,
            trapezoidImage,
            perspectiveMatrix,
            Size(width.toDouble(), height.toDouble())
        )

        // 保存结果图像
//        Imgcodecs.imwrite(outputPath, trapezoidImage)
        return trapezoidImage
    }

    private fun initView() {
        mSv = findViewById(R.id.sv)
    }

}