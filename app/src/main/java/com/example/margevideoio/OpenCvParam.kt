package com.example.margevideoio

import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point


class OpenCvParam {

    val cameraNames = listOf("front", "back", "left", "right")


    //水平和垂直方向的校准模式
    //鸟瞰外面有多远
    val shiftW = 300.0
    val shiftH = 300.0

    //校准图案和汽车之间的间隙大小
    val innShiftW = 20.0
    val innShiftH = 50.0

    //拼接图像的总宽度/高度
    val totalW = 600 + 2 * shiftW //1200
    val totalH = 1000 + 2 * shiftH //1600

    //汽车所占矩形区域的四个角
    val xl = shiftW + 180 + innShiftW
    val xr = totalW - xl
    val yt = shiftH + 200 + innShiftH//550
    val yb = totalH - yt

    val projectKeyPoints = mapOf(
        "front" to MatOfPoint2f(
            Point(shiftW + 120, shiftH),
            Point(shiftW + 480, shiftH),
            Point(shiftW + 120, shiftH + 160),
            Point(shiftW + 480, shiftH + 160)
        ),
        "back" to MatOfPoint2f(
            Point(shiftW + 120, shiftH),
            Point(shiftW + 480, shiftH),
            Point(shiftW + 120, shiftH + 160),
            Point(shiftW + 480, shiftH + 160)
        ),
        "left" to MatOfPoint2f(
            Point(shiftH + 280, shiftW),
            Point(shiftH + 840, shiftW),
            Point(shiftH + 280, shiftW + 160),
            Point(shiftH + 840, shiftW + 160)
        ),
//                listOf(
//            (shiftH + 280) to (shiftW),
//            (shiftH + 840) to (shiftW),
//            (shiftH + 280) to (shiftW + 160),
//            (shiftH + 840) to (shiftW + 160)
//        ),
        //"right": [(shift_h + 160, shift_w),
        //              (shift_h + 720, shift_w),
        //              (shift_h + 160, shift_w + 160),
        //              (shift_h + 720, shift_w + 160)]
        "right" to MatOfPoint2f(
            Point(shiftH + 160, shiftW),
            Point(shiftH + 720, shiftW),
            Point(shiftH + 160, shiftW + 160),
            Point(shiftH + 720, shiftW + 160)
        )
//                listOf(
//            (shiftH + 160) to (shiftW),
//            (shiftH + 720) to (shiftW),
//            (shiftH + 160) to (shiftW + 160),
//            (shiftH + 720) to (shiftW + 160)
//      )
    )


    val projectSelectPoint = mapOf(
        "front" to MatOfPoint2f(
            //765 393
            //1267 435
            //603 663
            //1461 703

            //523 414
            //1235 390
            //413 611
            //1342 596
            Point(523.0, 414.0),
            Point(1235.0, 390.0),
            Point(413.0, 611.0),
            Point(1342.0, 596.0)
        ),

        "back" to MatOfPoint2f(
            Point(765.0, 393.0),
            Point(1267.0, 435.0),
            Point(603.0, 663.0),
            Point(1461.0, 703.0)
        ),
        "left" to MatOfPoint2f(
            //731 640
            //1323 636
            //665 851
            //1439 838
            Point(731.0, 640.0),
            Point(1323.0, 636.0),
            Point(665.0, 851.0),
            Point(1439.0, 838.0)
        ),
        "right" to MatOfPoint2f(
            //583 546
            //1161 556
            //281 908
            //1440 895
            Point(731.0, 640.0),
            Point(1323.0, 636.0),
            Point(665.0, 851.0),
            Point(1439.0, 838.0)
        )
    )
    var image =
        Mat.zeros(totalH.toInt(), totalW.toInt(), CvType.CV_8U)

    constructor() {

    }

    val F: Mat
        get() = image.submat(0, yt.toInt(), xl.toInt(), xr.toInt())

    fun FM(frontImage: Mat): Mat {
        // 假设 xl 和 xr 是合适的整数值
        return frontImage.submat(0, frontImage.rows(), xl.toInt(), xr.toInt())
    }


    fun stitchAllParts(front: Mat): Mat {
        FM(front).copyTo(F)
        return image
    }

    fun flip(image: Mat, name: String): Mat {
        return when (name) {
            "front" -> image.clone()
            "back" -> image.clone().also {
                Core.flip(it, it, -1)
                }
            "left" -> {
                val transposed = Mat()
                Core.transpose(image, transposed)
                Core.flip(transposed, transposed, 0)
                transposed
            }
            else -> {
                val transposed = Mat()
                Core.transpose(image, transposed)
                Core.flip(transposed, transposed, 1)
                transposed
            }
        }
    }


}