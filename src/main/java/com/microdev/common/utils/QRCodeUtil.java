package com.microdev.common.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.microdev.common.oss.ObjectStoreService;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.util.Hashtable;



public class QRCodeUtil {
    public static File createQRCode(String text) throws Exception {
        int width = 100;
        int height = 100;
        String format = "png";
        Hashtable hints = new Hashtable ( );
        hints.put (EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put (EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put (EncodeHintType.MARGIN, 2);
        File file = null;
        try {
            BitMatrix bitMatrix = new MultiFormatWriter ( ).encode (text, BarcodeFormat.QR_CODE, width, height, hints);
            /*String  path = text.getClass().getResource("/").getFile();
            path = URLDecoder.decode(path,  "utf-8");
            file = new File( path, File.separator + "static" + File.separator +  "QRCode."+format);*/
            file = new File("/home/micro-worker/wgb/static/QRCode."+format);
            MatrixToImageWriter.writeToFile (bitMatrix, format, file);

        } catch (WriterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }
        return file;
    }
        //解析二维码：
        /*public static void main(String[] args) throws NotFoundException {
            MultiFormatReader formatReader=new MultiFormatReader();
            File file =new File("D:/new.png");
            BufferedImage image=null;
            try {
                image = ImageIO.read(file);
            } catch (IOException e) {
        // TODO Auto-generated catch block
            }
                e.printStackTrace();
            }
            BinaryBitmap binaryBitmap =new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
            Hashtable hints=new Hashtable();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            Result result=formatReader.decode(binaryBitmap,hints);
            System.err.println("解析结果："+result.toString());
            System.out.println(result.getBarcodeFormat());
            System.out.println(result.getText());
        }*/
}
