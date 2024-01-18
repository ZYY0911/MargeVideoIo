package com.example.margevideoio

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
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
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.videoio.VideoCapture
import java.io.File


class MargeVideo : AppCompatActivity() {

    private var mOpenCvParam: OpenCvParam? = null
    private var mUtil: MyUtil? = null
    private var rootPath: String? = null
    private val TAG = "MargeVideo"
    private var cameraMatrix: Mat? = null
    private var distCoeffs: Mat? = null
    private var mSv: SurfaceView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mSv = findViewById(R.id.sv)
        if (OpenCVLoader.initLocal()) {
            Log.i(TAG, "OpenCV loaded successful")
            Toast.makeText(this, "OpenCV loaded successful", Toast.LENGTH_SHORT).show()
        } else {
            Log.e(TAG, "OpenCV initialization failed!")
            Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG).show()
            return
        }
        mOpenCvParam = OpenCvParam()
        mUtil = MyUtil()
        rootPath = cacheDir.absolutePath + File.separator
        Thread {
            cameraMatrix = mUtil!!.readMatFromPath("${rootPath}cameraMatrix.txt")
            distCoeffs = mUtil!!.readMatFromPath("${rootPath}distCoeffs.txt")
            loadVideo()
        }.start()


    }

    private fun loadVideo() {
        val videoFile = rootPath + "Video" + File.separator
        var video1 = VideoCapture("${videoFile}front.mp4")
        var video2 = VideoCapture("${videoFile}back.mp4")
        var video3 = VideoCapture("${videoFile}left.mp4")
        var video4 = VideoCapture("${videoFile}right.mp4")
        if (!video1.isOpened || !video2.isOpened || !video3.isOpened || !video4.isOpened) {
            Log.e(TAG, "OpenVideo Filed")
            return
        }
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

            Calib3d.undistort(src1, frame1, cameraMatrix, distCoeffs)
            Calib3d.undistort(src2, frame2, cameraMatrix, distCoeffs)
            Calib3d.undistort(src3, frame3, cameraMatrix, distCoeffs)
            Calib3d.undistort(src4, frame4, cameraMatrix, distCoeffs)



            mUtil!!.saveScreenshotToTemp(frame1, baseContext)
            mUtil!!.saveScreenshotToTemp(frame2, baseContext)
            mUtil!!.saveScreenshotToTemp(frame3, baseContext)
            mUtil!!.saveScreenshotToTemp(frame4, baseContext)
            //front
            frame1 = mUtil!!.getVideBird(
                frame1,
                mOpenCvParam!!.projectSelectPoint["front"]!!,
                mOpenCvParam!!.projectKeyPoints["front"]!!,
                Size(mOpenCvParam!!.totalW, mOpenCvParam!!.yt)
            )
            val front = mOpenCvParam!!.flip(frame1, "front")
//            mUtil!!.saveScreenshotToTemp(front, baseContext)
            //back
            frame2 = mUtil!!.getVideBird(
                frame2,
                mOpenCvParam!!.projectSelectPoint["back"]!!,
                mOpenCvParam!!.projectKeyPoints["back"]!!,
                Size(mOpenCvParam!!.totalW, mOpenCvParam!!.yt)
            )
            val back = mOpenCvParam!!.flip(frame2, "back")
//            mUtil!!.saveScreenshotToTemp(back, baseContext)
            //left
            frame3 = mUtil!!.getVideBird(
                frame3,
                mOpenCvParam!!.projectSelectPoint["left"]!!,
                mOpenCvParam!!.projectKeyPoints["left"]!!,
                Size(mOpenCvParam!!.totalH, mOpenCvParam!!.xl)
            )
            val left = mOpenCvParam!!.flip(frame3, "left")
//            mUtil!!.saveScreenshotToTemp(left, baseContext)
            //right
            frame4 = mUtil!!.getVideBird(
                frame4,
                mOpenCvParam!!.projectSelectPoint["right"]!!,
                mOpenCvParam!!.projectKeyPoints["right"]!!,
                Size(mOpenCvParam!!.totalH, mOpenCvParam!!.xl)
            )
            val right = mOpenCvParam!!.flip(frame4, "right")
//            mUtil!!.saveScreenshotToTemp(right, baseContext)


            val showBitmap = Bitmap.createBitmap(
                mOpenCvParam!!.totalW.toInt(),
                mOpenCvParam!!.totalH.toInt(),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(showBitmap)

            //右
            val bitmapRight = Bitmap.createBitmap(
                right.width(), right.height(), Bitmap.Config.ARGB_8888
            )
            Utils.matToBitmap(right, bitmapRight, true)
            canvas.drawBitmap(
                bitmapRight, mOpenCvParam!!.xr.toFloat() + 70, 0.0f, null
            )
            //左
            val bitmapLeft = Bitmap.createBitmap(
                left.width(), left.height(), Bitmap.Config.ARGB_8888
            )
            Utils.matToBitmap(left, bitmapLeft, true)
            canvas.drawBitmap(
                bitmapLeft, -160.0f, 0.0f, null
            )
            //前
            val bitmapFront = Bitmap.createBitmap(
                front.width(), front.height(), Bitmap.Config.ARGB_8888
            )
            Utils.matToBitmap(front, bitmapFront, true)
            canvas.drawBitmap(
                bitmapFront, 0.0f, 0.0f, null
            )
            //后
            val bitmapBack = Bitmap.createBitmap(
                back.width(), back.height(), Bitmap.Config.ARGB_8888
            )
            Utils.matToBitmap(back, bitmapBack, true)
            canvas.drawBitmap(
                bitmapBack, 0.0f, mOpenCvParam!!.yb.toFloat(), null
            )


            val matrix = Matrix()
            //car
            val carBitmap = BitmapFactory.decodeResource(resources, R.mipmap.car)
            val desRect = RectF(
                mOpenCvParam!!.xl.toFloat() - 150,
                mOpenCvParam!!.yt.toFloat(),
                mOpenCvParam!!.xr.toFloat() + 60,
                mOpenCvParam!!.yb.toFloat()
            )
            canvas.drawBitmap(

                carBitmap, Rect(0, 0, carBitmap.width, carBitmap.height), desRect, null
            )
            matrix.postRotate(90f)
            val finalBitmap = Bitmap.createBitmap(
                showBitmap,
                0,
                0,
                showBitmap.width,
                showBitmap.height,
                matrix,
                true
            )

            val pointList: MutableList<Point> = ArrayList()
            pointList.add(Point(0.0, 0.0))
            pointList.add(Point(1000.0, 0.0))
            pointList.add(Point(1000.0, 500.0))
            pointList.add (Point(800.0, 600.0))
            pointList.add (Point(700.0, 400.0))


            val cropImageFree = mUtil!!.cropImageFree(finalBitmap, pointList)
            Log.i(TAG, "loadVideo: ")
            runOnUiThread {
                val canvas = mSv!!.holder.lockCanvas()
                val finalBitmap1 = finalBitmap.scale(mSv!!.width, mSv!!.height)
                canvas.drawBitmap(finalBitmap1, 0f, 0f, null)
                mSv!!.holder.unlockCanvasAndPost(canvas)
            }

        }
    }
}