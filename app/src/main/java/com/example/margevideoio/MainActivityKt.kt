package com.example.margevideoio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.scale
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.calib3d.Calib3d
import org.opencv.core.Core
import org.opencv.core.Core.addWeighted
import org.opencv.core.Core.flip
import org.opencv.core.Core.transpose
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.cvtColor
import org.opencv.imgproc.Imgproc.fillPoly
import org.opencv.imgproc.Imgproc.resize
import org.opencv.photo.Photo
import org.opencv.photo.Photo.NORMAL_CLONE
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
        var video3 = VideoCapture("${rootPath}position_2.mp4")
        var video4 = VideoCapture("${rootPath}position_1.mp4")
        if (!video1.isOpened || !video2.isOpened || !video3.isOpened || !video4.isOpened) {
            Log.e(TAG, "OpenVideo Filed")
            return
        }
        var videoW: Int? = null
        var videoH: Int? = null
        while (true) {
            var frame1 = Mat()
            var frame2 = Mat()
            var frame3 = Mat()
            var frame4 = Mat()
            val src1 = Mat()
            val src2 = Mat()
            val src3 = Mat()
            val src4 = Mat()


            video1.read(src1)
            video2.read(src2)
            video3.read(src3)
            video4.read(src4)
            if (src1.empty() || src2.empty() || src3.empty() || src4.empty()) {
                Log.e(TAG, "loadVideoIo: isEmpty")
                break
            }
            if (null == videoW) {
                videoW = src1.width()
                videoH = src1.height()
            }

            Calib3d.undistort(src1, frame1, cameraMatrix, distCoeffs)
            Calib3d.undistort(src2, frame2, cameraMatrix, distCoeffs)
            Calib3d.undistort(src3, frame3, cameraMatrix, distCoeffs)
            Calib3d.undistort(src4, frame4, cameraMatrix, distCoeffs)

            //前方
//            transpose(frame1,frame1)
            val beforePoints = MatOfPoint2f(
                //            Point(0.0, height.toDouble()),           // 左下
//            Point(width.toDouble(), height.toDouble()),  // 右下
//            Point(width.toDouble(), 0.0),              // 右上
//            Point(0.0, 0.0)                           // 左上
                Point(frame1.width().toDouble() * 0.35, frame1.height().toDouble()),
                Point(frame1.width().toDouble() * 0.65, frame1.height().toDouble()),
                Point(frame1.width().toDouble(), 0.0),
                Point(0.0, 0.0)
            )
            frame1 = mUtil!!.trapezoidTransform(frame1, beforePoints)
            transpose(frame1, frame1)
            flip(frame1, frame1, 0)

            val bitmapFront = Bitmap.createBitmap(
                frame1.width(), frame1.height(), Bitmap.Config.ARGB_8888
            )
            Utils.matToBitmap(frame1, bitmapFront, true)
            //右方
            frame2 = mUtil!!.trapezoidTransform(frame2, beforePoints)
            val bitmapRight = Bitmap.createBitmap(
                frame2.width(), frame2.height(), Bitmap.Config.ARGB_8888
            )
            Utils.matToBitmap(frame2, bitmapRight, true)
            //左方
            frame3 = mUtil!!.trapezoidTransform(frame3, beforePoints)
            //水平翻转
            flip(frame3, frame3, 0)
            val bitmapLeft = Bitmap.createBitmap(
                frame3.width(), frame3.height(), Bitmap.Config.ARGB_8888
            )
            Utils.matToBitmap(frame3, bitmapLeft, true)
            //后方
            frame4 = mUtil!!.trapezoidTransform(frame4, beforePoints)
            transpose(frame4, frame4)
            flip(frame4, frame4, 1)
            val bitmapBack = Bitmap.createBitmap(
                frame4.width(), frame4.height(), Bitmap.Config.ARGB_8888
            )
            Utils.matToBitmap(frame4, bitmapBack, true)

            val showBitmap = Bitmap.createBitmap(videoW, videoH!!, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(showBitmap)

            //前
            canvas.drawBitmap(
                bitmapFront.scale(
                    (showBitmap.width * 0.35).toInt(),
                    showBitmap.height
                ), 0.0f, 0.0f, null
            )

            canvas.drawBitmap(
                bitmapRight.scale(
                    bitmapRight.width,
                    (bitmapRight.height * 0.35).toInt()
                ), 0.0f, 0.0f, null
            )

            val drawH = showBitmap.height - showBitmap.height * 0.7
            canvas.drawBitmap(
                bitmapLeft.scale(
                    bitmapRight.width,
                    (bitmapRight.height * 0.35).toInt()
                ), 0.0f, (bitmapRight.height * 0.35 + drawH).toFloat(), null
            )
            //后
            val drawW = showBitmap.width - showBitmap.width * 0.7
            canvas.drawBitmap(
                bitmapBack.scale(
                    (showBitmap.width * 0.35).toInt(),
                    showBitmap.height
                ), (showBitmap.width * 0.35 + drawW).toFloat(), 0.0f, null
            )


            runOnUiThread {
                val canvas = mSv!!.holder.lockCanvas()
                val finalBitmap = showBitmap.scale(mSv!!.width, mSv!!.height)
                canvas.drawBitmap(finalBitmap, 0f, 0f, null)
                mSv!!.holder.unlockCanvasAndPost(canvas)
            }
//            mUtil!!.saveScreenshotToTemp(frame4, baseContext)
//            break


            //合并前和右
//            val allMat = Mat(1080, 1920, CvType.CV_8UC4)
//
//            val srcMask = Mat(frame1.size(), CvType.CV_8UC1)
//            Imgproc.cvtColor(frame1, frame1, Imgproc.COLOR_BGR2BGRA)
//
//
//            val maskPoints = arrayOf(
//                Point(0.0, frame1.height().toDouble()),
//                Point(frame1.width().toDouble(), frame1.height().toDouble() * 0.55),
//                Point(frame1.width().toDouble(), frame1.height().toDouble() * 0.45),
//                Point(0.0, 0.0)
//            )
//            fillPoly(srcMask, listOf(MatOfPoint(*maskPoints)), Scalar(255.0,255.0,255.0,0.0))
//            val result = Mat()
//            frame1.copyTo(result,srcMask)
////            int newRow = static_cast<int>(inputImage.rows * scaleRow);
//            mUtil!!.saveScreenshotToTemp(result,baseContext)
//
//
//            result.copyTo(Mat(allMat,Rect(0,allMat.rows(),0,(allMat.cols().toDouble()*0.35).toInt())))

//            val point = Point(0.0, 0.0)
//            val outPut = Mat()
//            boundingRect()

//            val subMatBefore = frame1.submat()

//            Imgproc.rectangle()


//            var mergedImage = Mat()
//            addWeighted(frame1, 1.0, frame2, 1.0, 0.0, mergedImage, 0)
//
//
//            val showBitmap = Bitmap.createBitmap(
//                frame1.width(), frame1.height(), Bitmap.Config.ARGB_8888
//            )
//            Utils.matToBitmap(mergedImage, showBitmap)
//            runOnUiThread {
//                val canvas = mSv!!.holder.lockCanvas()
//                val finalBitmap = showBitmap.scale(mSv!!.width, mSv!!.height)
//                canvas.drawBitmap(finalBitmap, 0f, 0f, null)
//                mSv!!.holder.unlockCanvasAndPost(canvas)
//            }


        }
    }

    private fun initView() {
        mSv = findViewById(R.id.sv)
    }
}

