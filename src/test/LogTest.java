package test;

public class LogTest {

    public static void main(String[] args) {

        String tmp = "C:\\次期国内スバル基幹\\sptpj\\SIC_IDE_PH15_C\\p21-app\\p21-lxpb\\p21-lxpb-business\\src\\main\\java\\spt\\lxp\\lxpb\\pbs03601\\PBS03601.java";
        System.out.println(tmp.substring(tmp.lastIndexOf("\\") + 1));

//        int startRowNo = 93;
//        String keyword = "CLOSE CURSOR";
//
//        List<String> allLines = Common.readAllLines("20201029115907537_PIS02101_I0000_06308.log");
//
//        int count = 0;
//
//        while(allLines.get(startRowNo + 19).contains(keyword)) {
//
//            int endRowNo = startRowNo + 19;
//
//            System.out.println("-------------------------------------------------------------------------------------");
//
//            for (int i = startRowNo; i <= endRowNo; i++) {
//                System.out.println(allLines.get(i));
//            }
//
//            startRowNo = endRowNo + 1;
//
//            System.out.println("-------------------------------------------------------------------------------------");
//
//            count++;
//        }
//
//        System.out.println(allLines.size());
//        System.out.println(count);
/*
 *
 * 2020/10/29 11:59:08.125
 * 2020/10/29 12:01:26.336
 *
 * 2:18
 *
 *
 *
 * 2020/10/29 11:59:10.016
 * 2020/10/29 12:01:25.373
 *
 * 2:15
 *
 */

    }

}
