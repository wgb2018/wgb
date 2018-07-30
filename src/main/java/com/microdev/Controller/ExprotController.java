package com.microdev.Controller;

import com.microdev.service.CompanyService;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class ExprotController {
    private static final Logger logger = LoggerFactory.getLogger(ExprotController.class);
    @Autowired
    CompanyService companyService;
    @GetMapping ("/hotels/hrcompanies/export")
    public void exportHrcompanies( HttpServletResponse response) {
        //List<Map<String, Object>> list = (List<Map<String, Object>>)companyService.hotelHrCompanies(paging.getPaginator(),paging.getSelector()).getData ();
        List<String> li = new ArrayList <> ();
        List<List> l1 = new ArrayList <> ();
        li.add ("1");
        li.add ("2");
        li.add ("3");
        li.add ("4");
        li.add ("5");
        l1.add (li);
        l1.add (li);
        l1.add (li);
        l1.add (li);
        l1.add (li);
        XSSFWorkbook wb = null;
        try {
            // excel模板路径
            String  path = getClass().getResource("/").getFile();
            path = URLDecoder.decode(path,  "utf-8");
            path = path + File.separator + "static" + File.separator;
            String excel = path + "test.xlsx";
            File fi = new File (excel);
            // 读取excel模板
            wb = new XSSFWorkbook (new FileInputStream (fi));
            // 读取了模板内所有sheet内容
            XSSFSheet sheet = wb.getSheetAt (0);
            //添加标题
            int rowIndex = 0;
            XSSFRow row = sheet.getRow (rowIndex);
            if (null == row) {
                row = sheet.createRow (rowIndex);
            }
            XSSFCell cell0 = row.getCell (0);
            if (null == cell0) {
                cell0 = row.createCell (0);
            }
            cell0.setCellValue ("标识");// 标识

            XSSFCell cell1 = row.getCell (1);
            if (null == cell1) {
                cell1 = row.createCell (1);
            }
            cell1.setCellValue ("用户名");// 用户名

            XSSFCell cell2 = row.getCell (2);
            if (null == cell2) {
                cell2 = row.createCell (2);
            }
            cell2.setCellValue ("头像");// 头像

            XSSFCell cell3 = row.getCell (3);
            if (null == cell3) {
                cell3 = row.createCell (3);
            }
            cell3.setCellValue ("性别");// 性别

            XSSFCell cell4 = row.getCell (4);
            if (null == cell4) {
                cell4 = row.createCell (4);
            }
            cell4.setCellValue ("手机");// 手机
            // 在相应的单元格进行赋值
            rowIndex = 1;
            int j = 1;
            for (List<String>  ls : l1) {
                row = sheet.getRow (rowIndex);
                if (null == row) {
                    row = sheet.createRow (rowIndex);
                }
                cell0 = row.getCell (0);
                if (null == cell0) {
                    cell0 = row.createCell (0);
                }
                cell0.setCellValue (ls.get(0));// 标识

                cell1 = row.getCell (1);
                if (null == cell1) {
                    cell1 = row.createCell (1);
                }
                cell1.setCellValue (ls.get(1));// 用户名

                cell2 = row.getCell (2);
                if (null == cell2) {
                    cell2 = row.createCell (2);
                }
                cell2.setCellValue (ls.get(2));// 头像

                cell3 = row.getCell (3);
                if (null == cell3) {
                    cell3 = row.createCell (3);
                }
                cell3.setCellValue (ls.get(3));// 性别

                cell4 = row.getCell (4);
                if (null == cell4) {
                    cell4 = row.createCell (4);
                }
                cell4.setCellValue (ls.get(4));// 手机
                rowIndex++;
            }

            String fileName = "用户信息";
            ByteArrayOutputStream os = new ByteArrayOutputStream ( );
            wb.write (os);
            byte[] content = os.toByteArray ( );
            InputStream is = new ByteArrayInputStream (content);
            // 设置response参数，可以打开下载页面
            response.reset ( );
            response.setContentType ("application/vnd.ms-excel;charset=utf-8");
            response.setHeader ("Content-Disposition", "attachment;filename=" + new String ((fileName + ".xlsx").getBytes ( ), "iso-8859-1"));
            ServletOutputStream sout = response.getOutputStream ( );
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;

            try {
                bis = new BufferedInputStream (is);
                bos = new BufferedOutputStream (sout);
                byte[] buff = new byte[2048];
                int bytesRead;
                // Simple read/write loop.
                while (-1 != (bytesRead = bis.read (buff, 0, buff.length))) {
                    bos.write (buff, 0, bytesRead);
                }
            } catch (Exception e) {
                logger.error ("导出excel出现异常:", e);
            } finally {
                if (bis != null)
                    bis.close ( );
                if (bos != null)
                    bos.close ( );
            }

        } catch (Exception e) {
            logger.error ("导出excel出现异常:", e);
        }


    }
}
