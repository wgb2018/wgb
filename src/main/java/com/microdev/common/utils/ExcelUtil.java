package com.microdev.common.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.util.List;

/**
 * excel工具类
 */
public class ExcelUtil {

    public static <T> void download(HttpServletResponse response, List<T> list, String[] strArr, String title, String name) {
        if (StringUtils.isEmpty(name)) {
            name = String.valueOf(System.currentTimeMillis()).substring(4, 13);
        }
        OutputStream out = null;
        try {
            HSSFWorkbook workbook = new HSSFWorkbook();                 // 创建工作簿对象
            HSSFSheet sheet = workbook.createSheet();                  // 创建工作表
            workbook.setSheetName(0, title);

            //sheet样式定义【getColumnTopStyle()/getStyle()均为自定义方法 - 在下面  - 可扩展】
            HSSFCellStyle columnTopStyle = getColumnTopStyle(workbook);//获取列头样式对象
            HSSFCellStyle style = getStyle(workbook);                    //单元格样式对象

            // 定义所需列数
            int columnNum = strArr.length;
            HSSFRow rowRowName = sheet.createRow(0);                // 在索引0的位置创建行

            // 将列头设置到sheet的单元格中
            for (int n = 0; n < columnNum; n++) {
                HSSFCell cellRowName = rowRowName.createCell(n);         //创建列头对应个数的单元格
                cellRowName.setCellType(CellType.STRING);      //设置列头单元格的数据类型
                HSSFRichTextString text = new HSSFRichTextString(strArr[n]);
                cellRowName.setCellValue(text);                          //设置列头单元格的值
                cellRowName.setCellStyle(columnTopStyle);                //设置列头单元格样式
                sheet.setColumnWidth(n, strArr[n].getBytes().length * 2 * 256); //设置自动列宽
            }

            //将查询出的数据设置到sheet对应的单元格中
            T obj;
            for (int i = 0; i < list.size(); i++) {

                obj = list.get(i);//遍历每个对象
                HSSFRow row = sheet.createRow(i + 1);//创建所需的行数

                Class clazz = obj.getClass();
                Field[] fields = clazz.getDeclaredFields();
                int num = fields.length;
                HSSFCell cell = null;
                for (int f = 0; f < num; f++) {
                    cell = row.createCell(f, CellType.STRING);
                    Field field = fields[f];
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    if (value != null) {
                        cell.setCellValue(value.toString());
                    } else {
                        cell.setCellValue("");
                    }
                    cell.setCellStyle(style);
                }

            }
            //让列宽随着导出的列长自动适应
            for (int colNum = 0; colNum < columnNum; colNum++) {
                int columnWidth = sheet.getColumnWidth(colNum) / 256;
                for (int rowNum = 0; rowNum < sheet.getLastRowNum(); rowNum++) {
                    HSSFRow currentRow;
                    //当前行未被使用过
                    if (sheet.getRow(rowNum) == null) {
                        currentRow = sheet.createRow(rowNum);
                    } else {
                        currentRow = sheet.getRow(rowNum);
                    }
                    if (currentRow.getCell(colNum) != null) {
                        HSSFCell currentCell = currentRow.getCell(colNum);
                        if (currentCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                            int length = currentCell.getStringCellValue().getBytes().length;
                            if (columnWidth < length) {
                                columnWidth = length;
                            }
                        }
                    }
                }
                if (colNum == 0) {
                    sheet.setColumnWidth(colNum, (columnWidth - 2) * 256);
                } else {
                    sheet.setColumnWidth(colNum, (columnWidth + 4) * 256);
                }
            }

            if (workbook != null) {
                try {

                    String fileName = new String(name.getBytes("UTF-8"), "iso-8859-1") + ".xls";
                    response.setContentType("application/msexcel");              
                    response.setCharacterEncoding("UTF-8");
                    response.setHeader("Content-Disposition",  "attachment;filename=" + fileName);
                    out = response.getOutputStream();
                    workbook.write(out);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (out != null) {
                        out.close();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static HSSFCellStyle getColumnTopStyle(HSSFWorkbook workbook) {

        // 设置字体
        HSSFFont font = workbook.createFont();
        //设置字体大小
        font.setFontHeightInPoints((short) 11);
        //字体加粗
        font.setBold(true);
        //设置字体名字
        font.setFontName("Courier New");
        //设置样式;
        HSSFCellStyle style = workbook.createCellStyle();
        //设置底边框;
        style.setBorderBottom(BorderStyle.THIN);
        //设置底边框颜色;
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        //设置左边框;
        style.setBorderLeft(BorderStyle.THIN);
        //设置左边框颜色;
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        //设置右边框;
        style.setBorderRight(BorderStyle.THIN);
        //设置右边框颜色;
        style.setRightBorderColor(HSSFColor.BLACK.index);
        //设置顶边框;
        style.setBorderTop(BorderStyle.THIN);
        //设置顶边框颜色;
        style.setTopBorderColor(HSSFColor.BLACK.index);
        //在样式用应用设置的字体;
        style.setFont(font);
        //设置自动换行;
        style.setWrapText(false);
        //设置水平对齐的样式为居中对齐;
        style.setAlignment(HorizontalAlignment.CENTER);
        //设置垂直对齐的样式为居中对齐;
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;

    }

    /*
     * 列数据信息单元格样式
     */
    private static HSSFCellStyle getStyle(HSSFWorkbook workbook) {
        // 设置字体
        HSSFFont font = workbook.createFont();
        //设置字体大小
        font.setFontHeightInPoints((short)10);
        //字体加粗

        //font.setBold(true);
        //设置字体名字

        font.setFontName("仿宋_GB2312");
        //设置样式;
        HSSFCellStyle style = workbook.createCellStyle();
        //设置底边框;
        style.setBorderBottom(BorderStyle.THIN);
        //设置底边框颜色;
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        //设置左边框;
        style.setBorderLeft(BorderStyle.THIN);
        //设置左边框颜色;
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        //设置右边框;
        style.setBorderRight(BorderStyle.THIN);
        //设置右边框颜色;
        style.setRightBorderColor(HSSFColor.BLACK.index);
        //设置顶边框;
        style.setBorderTop(BorderStyle.THIN);
        //设置顶边框颜色;
        style.setTopBorderColor(HSSFColor.BLACK.index);
        //在样式用应用设置的字体;
        style.setFont(font);
        //设置自动换行;
        style.setWrapText(false);
        //设置水平对齐的样式为居中对齐;
        style.setAlignment(HorizontalAlignment.CENTER);
        //设置垂直对齐的样式为居中对齐;
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;

    }

    public static String[] hotelAccount = {"用人单位名称", "任务类型", "任务内容", "工作日期", "开始/结束",  "应付款(元)", "已付款(元)", "待确认款(元)", "未付款(元)"};
    public static String[] hrAccount = {"人力公司名称", "任务类型", "任务内容", "工作日期", "开始/结束",  "应付款(元)", "已付款(元)", "待确认款(元)", "未付款(元)"};
    public static String[] workerAccount = {"用人单位名称", "任务类型", "任务内容", "工作日期", "开始/结束",  "应付款(元)", "已付款(元)", "待确认款(元)", "未付款(元)"};
    public static String[] workerCooperate = {"昵称", "性别", "电话", "头像", "创建时间", "小时工状态"};
    public static String[] cooperate = {"公司名称", "公司logo", "营业执照", "劳务派遣证","负责人", "联系电话", "地址", "公司状态"};
    public static String[] employerTask = {"用人单位名称", "任务内容", "任务类型", "时薪(元)", "工作日期", "开始/结束", "已报名/总数", "用人单位结算", "任务状态"};
    public static String[] hrTask = {"用人单位名称", "任务内容", "任务类型", "用人单位时薪(元)", "时薪(元)", "工作日期", "开始/结束", "已报名/总数", "用人单位结算", "人力公司结算", "任务状态"};
    public static String[] workerTask = {"用人单位", "用人单位负责人", "用人单位电话", "人力公司名称", "任务内容", "任务类型", "时薪(元)", "工作日期", "开始/结束", "拒绝原因", "任务状态"};

    public static String[] payRecord = {"结款方", "收款方", "支付金额", "支付时间", "支付状态"};
    public static String[] hotelInfo = {"来源", "公司名称", "公司logo", "营业执照", "负责人", "联系电话", "地址", "公司状态"};
    public static String[] hrInfo = {"来源", "公司名称", "公司logo", "营业执照", "劳务派遣证","负责人", "联系电话", "地址", "公司状态"};
    public static String[] workerInfo = {"来源", "昵称", "性别", "电话", "头像", "创建时间", "小时工状态"};
}
