package code.gen.tools.xlsgen.service;

import code.gen.tools.xlsgen.entity.ClassContext;
import code.gen.tools.xlsgen.entity.FieldType;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.*;
import java.util.*;

public class GenConfigService {

    static{
        Properties p = new Properties();
        p.setProperty(Velocity.ENCODING_DEFAULT, "UTF-8");
        p.setProperty("file.resource.loader.class","org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(p);
    }

    private static Template beanTemplate = Velocity.getTemplate("/config/template/ExcelParseBean.vm","utf-8");
    private static Template beanBaseTemplate = Velocity.getTemplate("/config/template/ExcelParseBeanBase.vm","utf-8");
    private static Template configServiceTemplate = Velocity.getTemplate("/config/template/ExcelParseConfigService.vm","utf-8");
    private static Template excelParseExtraConfigService = Velocity.getTemplate("/config/template/ExcelParseExtraConfigService.vm","utf-8");


    private String excelPath ="";
    private String fileOutputPath = "";



    public GenConfigService() {
        try {
            init();
            load();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            System.out.println("文件生成完成.");
        }
    }

    /**
     * 关联实体类的名称与excel表格
     */
    public void init()  {
        Properties prop = new Properties();
        InputStream in = null;
        try {
            in = GenConfigService.class.getClassLoader().getResourceAsStream("config/xlsTozlib.properties");
            prop.load(new InputStreamReader(in, "utf-8"));

            excelPath = prop.getProperty("EXCEL_PATH");
            System.out.println("excel文件目录：\t"+excelPath);

            fileOutputPath = prop.getProperty("FILE_OUTPUT_PATH");
            System.out.println("输出文件目录：\t"+fileOutputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @return
     */
    public ClassContext getSheetValue(Sheet sheet, String fileName) {

        ClassContext classContext = new ClassContext();
        List<FieldType> fieldTypes = new ArrayList<>();
        classContext.setFieldTypes(fieldTypes);

        classContext.setClassName(fileName+"Config");
        classContext.setPackageName(fileName.toLowerCase());
        classContext.setFileName(fileName);

        Row needAnalysisRows = sheet.getRow(4);
        Row defaultValRows = sheet.getRow(5);
        Row fieldTypeRows = sheet.getRow(6);
        Row fieldDescRows = sheet.getRow(7);
        Row fieldNameRows = sheet.getRow(8);

        int needAnalysisLen = needAnalysisRows.getLastCellNum();

        for (int i = 1; i < fieldNameRows.getLastCellNum(); i++) {
            FieldType fieldTypeBean = new FieldType();
            if(needAnalysisLen < i){
                break;
            }

            Cell fieldNameCell = fieldNameRows.getCell(i);
            if(fieldNameCell == null){
                continue;
            }

            fieldNameCell.setCellType(Cell.CELL_TYPE_STRING);
            String fieldName = fieldNameCell.getStringCellValue().trim();
            if("".equals(fieldName)){
                continue;
            }

            Cell needAnalysisCell = needAnalysisRows.getCell(i);
            if(needAnalysisCell == null){
                continue;
            }
            needAnalysisCell.setCellType(Cell.CELL_TYPE_STRING);
            if(!"1".equals(needAnalysisCell.getStringCellValue())){
                continue;
            }

            String fieldTypeStr = "String";
            Cell fieldTypeCell = fieldTypeRows.getCell(i);
            if(fieldTypeCell != null) {
                fieldTypeCell.setCellType(Cell.CELL_TYPE_STRING);
                if(fieldTypeCell.getStringCellValue()!=null && !"".equals(fieldTypeCell.getStringCellValue().trim())){
                    fieldTypeStr = fieldTypeCell.getStringCellValue().trim();
                }
            }

            fieldTypeStr = getTypeString(fieldTypeStr);

            String fieldDescStr = "";
            Cell fieldDescCell = fieldDescRows.getCell(i);
            if(fieldDescCell != null) {
                fieldDescCell.setCellType(Cell.CELL_TYPE_STRING);
                if(fieldDescCell.getStringCellValue() != null){
                    fieldDescStr = fieldDescCell.getStringCellValue().trim();
                }
            }

            String fieldDefaultVal = null;
            Cell defaultValCell = defaultValRows.getCell(i);
            if(defaultValCell != null) {
                defaultValCell.setCellType(Cell.CELL_TYPE_STRING);
                if(defaultValCell.getStringCellValue()!=null && !"".equals(defaultValCell.getStringCellValue().trim())){
                    fieldDefaultVal = defaultValCell.getStringCellValue().trim();

                    if("boolean".equals(fieldTypeStr)){
                        fieldDefaultVal = "1".equals(fieldDefaultVal) ? "true" : "false";
                    }
                }
            }

            fieldTypeBean.setDataType(fieldTypeStr);
            fieldTypeBean.setFieldDesc(fieldDescStr);
            fieldTypeBean.setFieldName(fieldName);
            char[] name = fieldName.toCharArray();
            name[0]-=32;
            fieldTypeBean.setFieldMethodName(String.valueOf(name));
            fieldTypeBean.setDefaultValue(fieldDefaultVal);
            fieldTypes.add(fieldTypeBean);
        }

        return classContext;
    }

    private String getTypeString(String fieldTypeStr) {
        switch (fieldTypeStr) {
            case "string":
            case ":":
            case "yyyy-MM-dd":
            case "yyyy-MM-dd HH:mm":
            case "HH:mm":
                fieldTypeStr = "String";
                break;

            case "number":
                fieldTypeStr = "long";
                break;
            case "|":
                fieldTypeStr = "List<String>";
                break;
            case "|:":
                fieldTypeStr = "Map<String,String>";
                break;
            default:
                break;
        }
        return fieldTypeStr;
    }

    @SuppressWarnings("resource")
    public void load() throws Exception {
        //获取当前运行路径
        String path = excelPath;

        //获取当前路径对应的文件夹
        File folder = new File(path);

        //对该文件夹内的所有文件进行遍历
        if (folder.listFiles() == null) {
            System.err.println("发生严重错误，未能读取EXCEL文件路径,path="+path);
            return;
        }

        HashMap<String, File> files = new HashMap<String, File>();

        for (File file : folder.listFiles()) {
            //获取文件夹内的文件名称和文件对象
            files.put(file.getName(), file);
        }


        for (File file : files.values()) {
            String fileName = file.getName();

            Workbook workbook = null;

            if (fileName.endsWith(".xls")) {
                workbook = new HSSFWorkbook(new FileInputStream(file));
            } else if (fileName.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(new FileInputStream(file));
            } else {
                continue;
            }

            //对所有标签进行遍历
            Iterator<Sheet> iterator = workbook.sheetIterator();
            while (iterator.hasNext()) {
                Sheet sheet = iterator.next();
                if("导出".equals(sheet.getSheetName().trim())){
                    ClassContext classContext = getSheetValue(sheet,fileName.substring(0, fileName.indexOf(".")));
                    String targetPackageName = fileOutputPath + "/com/junyou/gameconfig/model/"+classContext.getPackageName();
                    File dirPath = new File(targetPackageName+"/bean/"+classContext.getClassName()).getParentFile();
                    if(!dirPath.exists()){
                        dirPath.mkdirs();
                    }
                    genFile(beanBaseTemplate, classContext,targetPackageName+"/bean/"+classContext.getClassName()+"Base.java",true);

                    genFile(beanTemplate, classContext,targetPackageName+"/bean/"+classContext.getClassName()+".java",false);
                    genFile(configServiceTemplate, classContext,targetPackageName+"/"+classContext.getClassName()+"Service.java",false);
                }else if("附加数据".equals(sheet.getSheetName().trim())){

                    ClassContext classContext = getExtensionSheetValue(sheet,fileName.substring(0, fileName.indexOf("."))+"Extra",fileName.substring(0, fileName.indexOf(".")));

                    String targetPackageName = fileOutputPath + "/com/junyou/gameconfig/model/"+fileName.substring(0, fileName.indexOf("."));
                    File dirPath = new File(targetPackageName+"/bean/"+classContext.getClassName()).getParentFile();
                    if(!dirPath.exists()){
                        dirPath.mkdirs();
                    }
                    genFile(beanBaseTemplate, classContext,targetPackageName+"/bean/"+classContext.getClassName()+"Base.java",true);

                    genFile(beanTemplate, classContext,targetPackageName+"/bean/"+classContext.getClassName()+".java",false);
                    genFile(excelParseExtraConfigService, classContext,targetPackageName+"/"+classContext.getClassName()+"Service.java",false);
                }else{
                    System.out.println("sheet 名字【"+sheet.getSheetName()+"】错误，只允许是：\"导出\"或\"附加数据\"，参考其他文件。");
                }
            }
        }
    }


    private ClassContext getExtensionSheetValue(Sheet sheet, String fileName,String packageName) {
        ClassContext classContext = new ClassContext();
        List<FieldType> fieldTypes = new ArrayList<>();
        classContext.setFieldTypes(fieldTypes);

        classContext.setClassName(fileName+"Config");
        classContext.setPackageName(packageName.toLowerCase());
        classContext.setFileName(fileName);
        int lenRowNum = sheet.getLastRowNum();

        for (int i = 1; i < lenRowNum; i++) {
            Row row = sheet.getRow(i);
            FieldType fieldTypeBean = new FieldType();


            Cell fieldNameCell = row.getCell(0);
            if(fieldNameCell == null){
                continue;
            }

            fieldNameCell.setCellType(Cell.CELL_TYPE_STRING);
            String fieldName = fieldNameCell.getStringCellValue().trim();
            if("".equals(fieldName)){
                continue;
            }

            Cell needAnalysisCell = row.getCell(5);
            if(needAnalysisCell == null){
                continue;
            }
            needAnalysisCell.setCellType(Cell.CELL_TYPE_STRING);
            if(!"1".equals(needAnalysisCell.getStringCellValue())){
                continue;
            }

            String fieldTypeStr = "String";
            Cell fieldTypeCell = row.getCell(2);
            if(fieldTypeCell != null) {
                fieldTypeCell.setCellType(Cell.CELL_TYPE_STRING);
                if(fieldTypeCell.getStringCellValue()!=null && !"".equals(fieldTypeCell.getStringCellValue().trim())){
                    fieldTypeStr = fieldTypeCell.getStringCellValue().trim();
                }
            }
            fieldTypeStr = getTypeString(fieldTypeStr);

            String fieldDescStr = "";
            Cell fieldDescCell = row.getCell(3);
            if(fieldDescCell != null) {
                fieldDescCell.setCellType(Cell.CELL_TYPE_STRING);
                if(fieldDescCell.getStringCellValue() != null){
                    fieldDescStr = fieldDescCell.getStringCellValue().trim();
                }
            }


            fieldTypeBean.setDataType(fieldTypeStr);
            fieldTypeBean.setFieldDesc(fieldDescStr);
            fieldTypeBean.setFieldName(fieldName);
            char[] name = fieldName.toCharArray();
            name[0]-=32;
            fieldTypeBean.setFieldMethodName(String.valueOf(name));
            fieldTypes.add(fieldTypeBean);
        }
        return classContext;
    }

    public void genFile(Template template,ClassContext classContext,String fileName,boolean iscover){
        FileOutputStream fos = null;
        BufferedWriter writer = null;
        try {
            VelocityContext context = new VelocityContext();
            context.put("ClassContext", classContext);

            File targetFileName = new File(fileName);
            if(targetFileName.exists() && !iscover){
                return;
            }

            fos = new FileOutputStream(targetFileName);
            writer = new BufferedWriter(new OutputStreamWriter(fos,"utf-8"));
            if (template != null){
                template.merge(context, writer);
            }

            writer.flush();
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                if(writer !=null && fos!=null){
                    writer.close();
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
