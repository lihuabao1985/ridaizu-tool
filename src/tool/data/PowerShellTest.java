package tool.data;

import java.io.File;
import java.io.IOException;

import common.Common;
import common.Def;
import config.Config;

public class PowerShellTest implements TableDataOpt {

    public void exec(String[] args) throws IOException {
        System.out.println("実行開始");
        String serverIp = Config.getString("POWER_SHELL_SERVER_IP");
        String username = Config.getString("POWER_SHELL_ACCESS_USERNAME");
        String password = Config.getString("POWER_SHELL_ACCESS_PASSWORD");
        String destBasePath = Config.getString("POWER_SHELL_DEST_BASE_PATH");

        String[] destBasePathArray = destBasePath.split(":");

        String destBasePath2 = String.format("%s$%s", destBasePathArray[0], destBasePathArray[1]);
        String functionId = Config.getString("POWER_SHELL_FUNCTION_ID");
        String logFilename = Config.getString("POWER_SHELL_LOG_FILENAME");
        String localFilePath = Def.SRC_POWER_SHELL_COPY_TO_LOCAL_FILEPATH;

        String powerShellScript = String.format(getPowerShellScript(),
                username, password, serverIp, destBasePath, functionId, functionId, serverIp, destBasePath2, functionId,
                logFilename, localFilePath, logFilename);

        System.out.println(String.format("接続サーバー：%s", serverIp));
        System.out.println(String.format("接続アカウント：%s", username));
        System.out.println(String.format("接続パスワード：%s", password));
        System.out.println(String.format("実行対象フォルダー：%s%s%s", destBasePath, File.separator, functionId));
        System.out.println(String.format("実行対象機能ID：%s", functionId));

        System.out.println(powerShellScript);
        Common.saveDataToFile("task.ps1", powerShellScript);

        String command = String.format("%s -File task.ps1", Config.getString("POWER_SHELL_EXE_PATH"));

        Runtime runtime = Runtime.getRuntime(); // ランタイムオブジェクトを取得する
        try {
            runtime.exec(command); // 指定したコマンドを実行する
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        new File("task.ps1").delete();

        File checkSaveLogFile = new File(localFilePath);
        if (!checkSaveLogFile.exists()) {
            throw new IOException(String.format("現行ログファイルの保存が失敗しました", localFilePath));
        } else {
            System.out.println(String.format("ファイルが「%s」に保存されました。", localFilePath));
        }


        System.out.println("実行終了");
    }

    private String getPowerShellScript() {
        StringBuffer sb = new StringBuffer();
        sb.append("$Username = '%s' \n");
        sb.append("$Password = '%s' \n");
        sb.append("$pass = ConvertTo-SecureString -AsPlainText $Password -Force \n");
        sb.append("$Cred = New-Object System.Management.Automation.PSCredential -ArgumentList $Username,$pass \n");
        sb.append("$s = New-PSSession -ComputerName \"%s\"  -credential $Cred \n");
        sb.append("Invoke-Command -Session $s -ScriptBlock { cd %s\\%s } \n");
        sb.append("Invoke-Command -Session $s -ScriptBlock { start %s.bat } \n");
        sb.append("Start-Sleep -s 5 \n");
        sb.append("$Source = \"\\\\%s\\%s\\%s\\%s\" \n");
        sb.append("$Dest   = \"%s\" \n");
        sb.append("$WebClient = New-Object System.Net.WebClient \n");
        sb.append("$WebClient.Credentials = New-Object System.Net.NetworkCredential($Username, $Password) \n");
        sb.append("$WebClient.DownloadFile($Source, $Dest) \n");
        sb.append("Invoke-Command -Session $s -ScriptBlock { Remove-Item %s } \n");

        return sb.toString();
    }

}
