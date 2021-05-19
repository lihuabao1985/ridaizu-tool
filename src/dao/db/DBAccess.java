package dao.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DBAccess {
	private DBAccess(){

	}

	/**
	 * Select文を発行する
	 * @param sql 実行するSQL
	 * @return 結果のセットを返す。内部エラーが発生した場合はnull。
	 * @author jin@isr.co.jp
	 * @data   2012/01/24
	 */
	public static ResultSet query(String sql){
		try{
			return executeQuery(sql);
		}catch(SQLException e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * Select文を発行する
	 * @param sql 実行するSQL
	 * @param objects パラメータ
	 * @return 結果のセットを返す。内部エラーが発生した場合はnull。
	 * @author jin@isr.co.jp
	 * @data   2012/01/24
	 */
	public static ResultSet query(String sql,Object...objects){
		try{
			return executeQuery(sql,objects);
		}catch(SQLException e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * Insert Delete Update 文を発行する。
	 * @param sql 実行するSQL
	 * @return 成功した場合は対象レコードの件数を返す。エラーの場合は-1
	 * @author jin@isr.co.jp
	 * @data   2012/01/24
	 */
	public static int update(String sql){
		try {
			return executeUpdate(getPreparedStatement(sql));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}
	/**
	 * Delete Update 文を発行する。
	 * @param sql 実行するSQL
	 * @param objects パラメータ
	 * @return 成功した場合は対象レコードの件数を返す。エラーの場合は-1
	 * @author jin@isr.co.jp
	 * @data   2012/01/24
	 */
	public static int update(String sql,Object...objects){
		try {
			return executeUpdate(getPreparedStatement(sql,objects));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}
	/**
	 * Delete Update 文を発行する。
	 * @param sql 実行するSQL
	 * @param objects パラメータ
	 * @return 成功した場合は対象レコードの件数を返す。エラーの場合は-1
	 * @author jin@isr.co.jp
	 * @data   2012/01/24
	 */
	public static int updateReturnId(String sql,Object...objects){
		try {
			return executeUpdateReturnId(getPreparedStatementReturnId(sql,objects));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}
	/**
	 * Select文を発行する
	 * @param sql 実行するSQL
	 * @return 結果の第１行の第１列の値を返す。内部エラーが発生した場合はnull。
	 * @author jin@isr.co.jp
	 * @data   2012/01/24
	 */
	public static Object single(String sql){
		try{
			ResultSet rs = executeQuery(sql);
			if(rs.next()) return rs.getObject(1);
		}catch(SQLException e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * Select文を発行する
	 * @param sql 実行するSQL
	 * @param objects パラメータ
	 * @return 結果の第１行の第１列の値を返す。内部エラーが発生した場合はnull。
	 * @author jin@isr.co.jp
	 * @data   2012/01/24
	 */
	public static Object single(String sql,Object...objects){
		try{
			ResultSet rs = executeQuery(sql,objects);
			if(rs.next()) return rs.getObject(1);
		}catch(SQLException e){
			e.printStackTrace();
		}
		return null;
	}
	private static int executeUpdate(PreparedStatement pstmt) throws SQLException{
		return pstmt.executeUpdate();
	}
	private static int executeUpdateReturnId(PreparedStatement pstmt) throws SQLException{
		pstmt.executeUpdate();
		ResultSet rs = pstmt.getGeneratedKeys();
		int id = -1;
		if (rs.next()) {
			id = rs.getInt(1);
		}
		return id;
	}
	private static ResultSet executeQuery(String sql,Object...objects) throws SQLException{
		return executeQuery(getPreparedStatement(sql,objects));
	}
	private static ResultSet executeQuery(PreparedStatement pstmt) throws SQLException{
		return pstmt.executeQuery();
	}
	private static PreparedStatement getPreparedStatement(String sql,Object...objects) throws SQLException{
		PreparedStatement pstmt = getPreparedStatement(sql);
		setParameters(pstmt,objects);
		return pstmt;
	}
	private static PreparedStatement getPreparedStatement(String sql) throws SQLException{
		return DBManager.getConnection().prepareStatement(sql);
	}
	private static PreparedStatement getPreparedStatementReturnId(String sql,Object...objects) throws SQLException{
		PreparedStatement pstmt = getPreparedStatementReturnId(sql);
		setParameters(pstmt,objects);
		return pstmt;
	}
	private static PreparedStatement getPreparedStatementReturnId(String sql) throws SQLException{
		return DBManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	}
	private static void setParameters(PreparedStatement pstmt,Object...objects) throws SQLException{
		for (int i = 0; i < objects.length; i++) {
			pstmt.setObject(i + 1, objects[i]);
		}
	}
}
