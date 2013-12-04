import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class SQLManager {
	static Connection conn;
	static Statement st;

	public ArrayList<ArrayList<String>> execSQL(String sql) {
		ArrayList<ArrayList<String>> resultList = new ArrayList<ArrayList<String>>();
		if (sql.startsWith("SELECT") || sql.startsWith("select")) {
			resultList = execQuery(sql);
		} else if (sql.startsWith("INSERT") || sql.startsWith("insert")
				|| sql.startsWith("UPDATE") || sql.startsWith("update")
				|| sql.startsWith("DELETE") || sql.startsWith("delete")) {
			int count = execUpdate(sql);
			ArrayList<String> countList = new ArrayList<String>();
			countList.add(String.valueOf(count));
			resultList.add(countList);
		} else {
			System.out.println("SQL sentence error!");
		}
		return resultList;
	}

	public ArrayList<ArrayList<String>> execQuery(String sql) {
		ArrayList<ArrayList<String>> resultList = new ArrayList<ArrayList<String>>();
		conn = getConnection();
		try {
			st = (Statement) conn.createStatement();
			ResultSet rs = st.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();

			try {
				System.out.println("Query Result:");
				while (rs.next()) {
					ArrayList<String> tuple = new ArrayList<String>();
					String attribute = null;
					for (int i = 1; i <= columnsNumber; i++) {
						attribute = rs.getString(i);
						tuple.add(attribute);
					}
					resultList.add(tuple);
					for (int i = 0; i < tuple.size(); i++) {
						if (i != tuple.size() - 1) {
							System.out.print(tuple.get(i) + " | ");
						} else {
							System.out.println(tuple.get(i));
						}
					}
				}
				System.out.println();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			conn.close();
		} catch (SQLException e) {
			System.out.println("Fail to query data.");
		}
		return resultList;
	}

	public int execUpdate(String sql) {
		conn = getConnection();
		int count = 0;
		try {
			st = (Statement) conn.createStatement();
			count = st.executeUpdate(sql);
			System.out.println("Success Update!");
			conn.close();
		} catch (SQLException e) {
			System.out.println("Fail to query data.");
		}
		return count;
	}

	public static Connection getConnection() {
		Connection con = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");

			con = DriverManager.getConnection(
					"jdbc:mysql://127.0.0.1:3307/FinanceSharer", "root",
					"database");

		} catch (Exception e) {
			System.out.println("Fail to connect to the server "
					+ e.getMessage());
		}
		return con;
	}
}
