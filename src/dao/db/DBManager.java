package dao.db;

import java.sql.Connection;
//import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public final class DBManager {
	private DBManager(){

	}

	private static final ThreadLocal<Connection> CONNS = new ThreadLocal<Connection>();
	private static DataSource ds = null;

	static{initDao();}

	private static void initDao(){
		try {
			InitialContext ic = new InitialContext();
			ds = (DataSource)ic.lookup("java:comp/env/jdbc/mysql");
		} catch (NamingException e) {}
	}
	public static void setDataSource(DataSource dataSource){
		ds = dataSource;
	}
	/**
	 * 当ThreadのDao対象を取得する
	 * @return
	 * @ticket
	 * @author jin@isr.co.jp
	 * @data   2011/12/27
	 */
	public static Connection getConnection(){
		try {
			Connection conn = CONNS.get();
			if(conn ==null || conn.isClosed()){
				//unit test
//				Class.forName("com.mysql.jdbc.Driver");
//				conn = DriverManager.getConnection("jdbc:mysql://153.126.135.189:3306/ridaizu_taobao", "root", "ly22613757");
				//product
				conn = ds.getConnection();
				CONNS.set(conn);
			}
			return conn;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
//			catch (ClassNotFoundException e) {
//			e.printStackTrace();
//			return null;
//		}
	}
	/**
	 * 当ThreadのDao対象を解放する
	 *
	 * @ticket
	 * @author jin@isr.co.jp
	 * @data   2011/12/27
	 */
	public static void closeConnection(){
		Connection conn = CONNS.get();
		if(conn == null)return;
		try {
			if(!conn.isClosed()) conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		conn = null;
		CONNS.set(null);
		CONNS.remove();
	}


	/**
	 * トランザクション開始。（オートコミットを無効�?
	 *
	 * @return 成功した場合 true
	 */
	public static boolean beginTransaction(){
		// log.debug("beginTransaction Start.");
		Connection con = CONNS.get();
		if(con == null){
			// log.error("beginTransaction Connection is null.");
			return false;
		}

		try{
			con.setAutoCommit(false);
		}catch(Exception e){
			// log.error("beginTransaction Exception.",e);
		}
		// log.debug("beginTransaction End.");

		return true;
	}

	/**
	 * トランザクション終了。（コミット-オートコミットを有効�?
	 *
	 * @return 成功した場合 true
	 */
	public static boolean commitTransaction(){
		// log.debug("commitTransaction Start.");
		Connection con = CONNS.get();
		if(con == null){
			// log.error("commitTransaction Connection is null.");
			return false;
		}

		try{
//			releaseStatement();
			con.commit();
			con.setAutoCommit(true);
		}catch(Exception e){
			// log.error("commitTransaction Exception.",e);
		}
		// log.debug("commitTransaction End.");
		return true;
	}

	/**
	 * ロールバック
	 *
	 * @return 成功した場合 true
	 */
	public static boolean rollbackTransaction(){
		// log.debug("rollbackTransaction Start.");
		Connection con = CONNS.get();
		if(con == null){
			// log.error("rollbackTransaction Connection is null.");
			return false;
		}

		try{
			con.rollback();
			con.setAutoCommit(false);
		}catch(Exception e){
			// log.error("rollbackTransaction Exception.",e);
		}

		// log.debug("rollbackTransaction End.");
		return true;
	}


	/**
	 * 使用中のステートメント・リザルトセットをクリアする�?
	 *
	 * @return 成功した場合�?true
	 */
//	private boolean releaseStatement(){
//		// log.debug("releaseStatement Start.");
//		try{
//			if(rs != null){
//				rs.close();
//			}
//			if(stmt != null){
//				stmt.close();
//			}
//		}catch(Exception e){
//			// log.error("releaseStatement Exception.",e);
//			rs = null;
//			stmt = null;
//			return false;
//		}
//		// log.debug("releaseStatement End.");
//		return true;
//	}


}
