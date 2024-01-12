package com.example.margevideoio

import android.content.Context
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
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
                    mat.put(i,j,value)
                }
            }
            bufferReader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return mat!!
    }


     fun saveScreenshotToTemp(mat: Mat?,context:Context) {
        val galleryPath =
            context.cacheDir.absolutePath + File.separator + "zyCache" + File.separator + "captured"
        val file = File(galleryPath)
        if (!file.exists()) {
            file.mkdirs()
        }
        val file1 = File(file.absolutePath, System.currentTimeMillis().toString() + ".jpg")
        Imgcodecs.imwrite(file1.absolutePath, mat)
    }
}