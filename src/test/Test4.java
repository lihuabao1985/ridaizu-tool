package test;

import java.util.ArrayList;
import java.util.List;

public class Test4 {

    public static void main(String[] args) throws Exception {
        // TODO 自動生成されたメソッド・スタブ
//        String cmdStr = "dir";
//        Runtime run = Runtime.getRuntime();
//        try {
//            Process process = run.exec(cmdStr);
//            InputStream in = process.getInputStream();
//            InputStreamReader reader = new InputStreamReader(in);
//            BufferedReader br = new BufferedReader(reader);
//            StringBuffer sb = new StringBuffer();
//            String message;
//            while((message = br.readLine()) != null) {
//                sb.append(message);
//            }
//            System.out.println(sb);
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

//        File file = new File("C:\\Users\\li.huabao\\Desktop\\証拠");
////		Desktop.getDesktop().open(file);
//
////		Runtime.getRuntime().exec("explorer.exe " + file.getPath());
//
////		String[] command = new String[4];
////		command[0] = "cmd.exe";
////		command[1] = "/C"; // 以下のコマンドを実行するオプション
////		command[2] = "start";
////		command[3] = file.getPath();
////		Runtime.getRuntime().exec(command);
//
//        String[] command = new String[5];
//        command[0] = "cmd.exe";
//        command[1] = "/C";
//        command[2] = "start";
//        command[3] = "\"dummy\"";
//        command[4] = file.getPath();
//        Runtime.getRuntime().exec(command);
//
////		Desktop.getDesktop().open(file.getCanonicalFile());


    }

    private static List<String> getColumnNameList(String str) {
        List<String> list = new ArrayList<String>();

        String splitChar = "\"";

        while(str.contains(splitChar)) {
            int startIndex = str.indexOf("\"") + 1;
            str = str.substring(startIndex);
            int endIndex = str.indexOf("\"");
            list.add(str.substring(0, endIndex));
            str = str.replaceFirst(splitChar, "");
        }

        return list;
    }

}
