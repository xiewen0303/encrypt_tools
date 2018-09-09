package code.gen.tools;

import code.gen.tools.dbgen.service.GenDBService;
import code.gen.tools.xlsgen.service.GenConfigService;

import java.util.Arrays;
import java.util.List;

/**
 * 读取游戏数值配置
 */
public class StartMain {

    public static void main(String[] args) {
        List<String> datas = Arrays.asList("excel","db");
        String flag = "excel";
        if(args.length>=1){
            String flagStr = args[0].trim().replace(" ","");
            if(!datas.contains(flagStr)){
                System.out.println("生成类型不正确,请输入：excel 或 db \t"+flagStr);
            }
            flag = flagStr;
        }

        if(flag.equals("excel")){
            System.out.println("生成excel类开始：");
            new GenConfigService();
        }else if(flag.equals("db")){
            System.out.println("生成db类开始：");
            GenDBService.start();
        }
    }
}
