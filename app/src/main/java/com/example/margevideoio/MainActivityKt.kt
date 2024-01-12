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
import org.opencv.core.Core.addWeighted
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.opencv.videoio.VideoCapture
import java.io.File

class MainActivityKt : AppCompatActivity() {
    private val TAG = "VideoMarge"
    private var mSv: SurfaceView? = null
    private var mUtil: MyUtil? = null
    private var cameraMatrix: Mat? = null
    private var rootPath: String? = null
    private var distCoeffs: Mat? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        rootPath = cacheDir.absolutePath + File.separator
        mUtil = MyUtil()
        if (OpenCVLoader.initLocal()) {
            Log.i(TAG, "OpenCV loaded successful")
            Toast.makeText(this, "OpenCV loaded successful", Toast.LENGTH_SHORT).show()
        } else {
            Log.e(TAG, "OpenCV initialization failed!")
            Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG).show()
            return
        }
        Thread {
            cameraMatrix = mUtil!!.readMatFromPath("${rootPath}cameraMatrix.txt")
            distCoeffs = mUtil!!.readMatFromPath("${rootPath}distCoeffs.txt")
            loadVideoIo()
        }.start()
    }

    private fun loadVideoIo() {
        val rootPath = cacheDir.absolutePath + File.separator + "Video" + File.separator
        var video1 = VideoCapture("${rootPath}position_1.mp4")
        var video2 = VideoCapture("${rootPath}position_2.mp4")
        var video3 = VideoCapture("${rootPath}video3.mp4")
        var video4 = VideoCapture("${rootPath}video4.mp4")
        if (!video1.isOpened || !video2.isOpened || !video3.isOpened || !video4.isOpened) {
            Log.e(TAG, "OpenVideo Filed")
            return
        }
        while (true) {
            val frame1 = Mat()
            val frame2 = Mat()
            val frame3 = Mat()
            val frame4 = Mat()
            val src1 = Mat()
            val src2 = Mat()
            val src3 = Mat()
            val src4 = Mat()


            video1.read(src1)
            video2.read(src2)
            video3.read(src3)
            video4.read(src4)
            if (src1.empty() || src2.empty() ||src3.empty()||src4.empty()) {
                Log.e(TAG, "loadVideoIo: isEmpty")
                break
            }

            Calib3d.undistort(src1, frame1, cameraMatrix, distCoeffs)
            Calib3d.undistort(src2, frame2, cameraMatrix, distCoeffs)
            Calib3d.undistort(src3, frame3, cameraMatrix, distCoeffs)
            Calib3d.undistort(src4, frame4, cameraMatrix, distCoeffs)

            //下️
            var mask = Mat(frame1.size(), CvType.CV_8UC1, Scalar(0.0))
            var maskPoints = arrayOf(
                Point(0.0, frame1.height().toDouble()),  // 左下角
                Point(frame1.width().toDouble(), frame1.height().toDouble()),  // 右下角
                Point(frame1.width().toDouble() * 0.65, frame1.height().toDouble() * 0.55),  // 右上角
                Point(frame1.width().toDouble() * 0.35, frame1.height().toDouble() * 0.55)  // 左上角
            )
            Imgproc.fillPoly(mask, listOf(MatOfPoint(*maskPoints)), Scalar(255.0, 0.0, 255.0))
            val croppedImage1 = Mat()
            frame1.copyTo(croppedImage1, mask)


            //上
            mask = Mat(frame2.size(), CvType.CV_8UC1, Scalar(0.0))
            maskPoints = arrayOf(
                Point(frame1.width().toDouble() * 0.35, frame1.height().toDouble() * 0.45),
                Point(frame1.width().toDouble() * 0.65, frame1.height().toDouble() * 0.45),
                Point(frame1.width().toDouble(), 0.0),
                Point(0.0, 0.0)
            )
            Imgproc.fillPoly(mask, listOf(MatOfPoint(*maskPoints)), Scalar(255.0, 0.0, 255.0))
            val croppedImage2 = Mat()
            frame2.copyTo(croppedImage2, mask)


            //左
            mask = Mat(frame3.size(), CvType.CV_8UC1, Scalar(0.0))
            maskPoints = arrayOf(
                Point(0.0, frame1.height().toDouble()),
                Point(frame1.width().toDouble() * 0.35, frame1.height().toDouble() * 0.55),
                Point(frame1.width().toDouble() * 0.35, frame1.height().toDouble() * 0.45),
                Point(0.0, 0.0)
            )
            Imgproc.fillPoly(mask, listOf(MatOfPoint(*maskPoints)), Scalar(255.0, 0.0, 255.0))
            val croppedImage3 = Mat()
            frame3.copyTo(croppedImage3, mask)
//
//
//            //右
            mask = Mat(frame4.size(), CvType.CV_8UC1, Scalar(0.0))
            maskPoints = arrayOf(
                Point(frame1.width().toDouble() * 0.65, frame1.height().toDouble() * 0.55),
                Point(frame1.width().toDouble(), frame1.height().toDouble()),
                Point(frame1.width().toDouble(), 0.0),
                Point(frame1.width().toDouble() * 0.65, frame1.height().toDouble() * 0.45)
            )
            Imgproc.fillPoly(mask, listOf(MatOfPoint(*maskPoints)), Scalar(255.0, 0.0, 255.0))
            val croppedImage4 = Mat()
            frame4.copyTo(croppedImage4, mask)

            var mergedImage = Mat()
            addWeighted(croppedImage1, 1.0, croppedImage2, 1.0, 0.0, mergedImage, 0)
            addWeighted(mergedImage, 1.0, croppedImage3, 1.0, 0.0, mergedImage, 0)
            addWeighted(mergedImage, 1.0, croppedImage4, 1.0, 0.0, mergedImage, 0)


            val showBitmap = Bitmap.createBitmap(
                frame1.width(), frame1.height(), Bitmap.Config.ARGB_8888
            )
            Utils.matToBitmap(mergedImage, showBitmap)
            runOnUiThread {
                val canvas = mSv!!.holder.lockCanvas()
                val finalBitmap = showBitmap.scale(mSv!!.width, mSv!!.height)
                canvas.drawBitmap(finalBitmap, 0f, 0f, null)
                mSv!!.holder.unlockCanvasAndPost(canvas)
            }
        }

    }


    private fun initView() {
        mSv = findViewById(R.id.sv)
    }

}

