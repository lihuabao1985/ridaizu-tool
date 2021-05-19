package tool.hikinoukennsyou;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Test {

    public static void main(String[] args) throws ParseException {
        // TODO 自動生成されたメソッド・スタブ
        //设置Date格式为“年-月-日 小时:分钟:秒.毫秒”
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        //设置时间，String转为Date
        String strStart = "2021/02/15 01:05:57.369";
        String strEnd = "2021/02/15 01:06:27.817";
        Date dateStart = sdf.parse(strStart);
        Date dateEnd = sdf.parse(strEnd);

        //直接获取Date的long值相减
        long duration = dateEnd.getTime() - dateStart.getTime();
        System.out.println("duration:" + duration);
    }

}
