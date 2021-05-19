package test;

import jp.co.fit.vfreport.Vrw32;

public class SqlTimeTest {
    public static void main(String[] args) {

//        final String URL
//        = "jdbc:sqlite:spt_reports.db";
////        final String USER = "";
////        final String PASS = "";
//        final String SQL = "select * from TM_REPORTDEFINITION";
//
//        try(Connection conn =
//                DriverManager.getConnection(URL);
//            PreparedStatement ps = conn.prepareStatement(SQL)){
//
//            try(ResultSet rs = ps.executeQuery()){
//                while (rs.next()) {
//                    System.out.println(
//                        rs.getString("SYSTEM_ID") + " " +
//                        rs.getString("REPORT_ID"));
//                }
//            };
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            System.out.println("処理が完了しました");
//        }


        Vrw32 svf = new Vrw32();

        int ret = svf.VrSetLocale("ja");
        if (ret < 0) {
            throw new IllegalArgumentException();
        }
        System.out.println(ret);

//        ret = svf.VrInit("MS932");
//        if (ret < 0) {
//            throw new IllegalArgumentException();
//        }
//        System.out.println(ret);

        // 2020.07.05 デフォルトはPDFを設定

        ret = svf.VrSetPrinter("", "PDF");

        System.out.println(ret);
    }
}