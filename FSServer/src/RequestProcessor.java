import java.util.ArrayList;

public class RequestProcessor {
	boolean validation_flag;

	public String processRequest(String message_in) {
		validation_flag = true;
		// message_in = "0 0 ;g1;u1;30;2013-11-30;abcdef;u1,u2,u3\n\n";
		// message_in = "0 1 34;g1;u1;60;2013-11-30;abcdef;u1,u2,u3\n\n";
		// message_in = "0 2 34;;;;;;\n\n";
		// message_in = "1 0 g1;;;;;;\n\n";
		// message_in = "1 1 g1;;;;;;\n\n";

		// message_in = "2 0 g2;gp2;;\n\n";
		// message_in = "2 1 g1;gp1;u1;up1\n\n";
		//message_in = "2 2 ;;b;b\n\n";

		// System.out.println(getAttr(message_in, 0));
		// System.out.println(getAttr(message_in, 1));
		// System.out.println(getAttr(message_in, 2));
		// System.out.println(getAttr(message_in, 3));

		ArrayList<ArrayList<String>> results = parse(message_in);

		// System.out.println("results-----");
		// for(int i=0; i < results.size(); i++){
		// for(int j=0; j < results.get(i).size(); j++){
		// System.out.println(Integer.toString(i) + ", " + Integer.toString(j) +
		// ": " + results.get(i).get(j));
		// }
		// }

		String message_out = translate(message_in, results, validation_flag);

		// System.out.println("message_out-----");
		//System.out.println(message_out);

		/*
		 * !!!!!! Jeremy: Please add your tranlate(message_in, results) function
		 * here. And use the member variable validation_flag if necessary. If
		 * there's something wrong with the generated SQL, parse() will set
		 * validation_flag=false. Except User login: in this part the parse only
		 * return the real upw, you need to check if it matches or not. !!!!!!
		 */

		return message_out;

	}

	public ArrayList<ArrayList<String>> parse(String message_in) {
		SQLManager sql_manager = new SQLManager();
		ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();

		String sql = null;
		if (message_in.charAt(0) == '0') {
			// Transaction Submission
			sql = parseSubmission(message_in);
		} else if (message_in.charAt(0) == '1') {
			// Report Request
			sql = parseReport(message_in);
		} else if (message_in.charAt(0) == '2') {
			// Account Creation/Login
			sql = parseAccount(message_in);
		} else {
			System.out.println("data_type is an invalid value!");
		}

		System.out.println("SQL sentence: \n" + sql);
		if (sql != null) {
			results = sql_manager.execSQL(sql);
		} else {
			validation_flag = false;
			System.out.println("SQL is null, request failed.");
		}

		return results;
	}

	public String parseSubmission(String message_in) {
		String sql = null;

		String tmpStr = message_in.substring(4);
		String tid = getAttr(tmpStr, 0);
		String gname = "'" + getAttr(tmpStr, 1) + "'";
		String uname = "'" + getAttr(tmpStr, 2) + "'";
		String amount = getAttr(tmpStr, 3);
		String time = "'" + getAttr(tmpStr, 4) + "'";
		String description = "'" + getAttr(tmpStr, 5) + "'";
		String for_whom = "'" + getAttr(tmpStr, 6) + "'";

		if (message_in.charAt(2) == '0') {
			// Add transaction
			SQLManager sql_manager = new SQLManager();
			ArrayList<String> max_tid = sql_manager.execQuery(
					"SELECT MAX(tid) FROM Transaction").get(0);
			max_tid.set(0, (max_tid.get(0) != null ? max_tid.get(0) : "0"));
			tid = String.valueOf(1 + Integer.parseInt(max_tid.get(0)));
			sql = "INSERT INTO Transaction VALUES(" + tid + "," + gname + ","
					+ uname + "," + amount + "," + time + "," + description
					+ "," + for_whom + ")";
			// Balance Maintenance
			addBalanceMt(uname, for_whom, amount);
		} else if (message_in.charAt(2) == '1') {
			// Edit transaction
			if (tid.length() != 0) {
				sql = "UPDATE Transaction SET ";
				if (!gname.equalsIgnoreCase("''")) {
					sql = sql + "gname=" + gname + ",";
				}
				if (!uname.equalsIgnoreCase("''")) {
					sql = sql + "uname=" + uname + ",";
				}
				if (!amount.equalsIgnoreCase("")) {
					sql = sql + "amount=" + amount + ",";
				}
				if (!time.equalsIgnoreCase("''")) {
					sql = sql + "time=" + time + ",";
				}
				if (!description.equalsIgnoreCase("''")) {
					sql = sql + "description=" + description + ",";
				}
				if (!for_whom.equalsIgnoreCase("''")) {
					sql = sql + "for_whom=" + for_whom + ",";
				}
				sql = sql.substring(0, sql.length() - 1);
				sql = sql + " WHERE tid=" + tid;

				editBalanceMt(tid, uname, for_whom, amount);
			} else {
				System.out.println("Update without tid!");
			}
		} else if (message_in.charAt(2) == '2') {
			// Delete transaction
			if (tid.length() != 0) {
				sql = "DELETE FROM Transaction WHERE tid=" + tid;
				deleteBalanceMt(tid);
			} else {
				System.out.println("Delete without tid!");
			}
		} else {
			System.out
					.println("In submission: action_type is an invalid value!");
		}
		return sql;
	}

	public String parseReport(String message_in) {
		String sql = null;

		String tmpStr = message_in.substring(4);
		String gname = "'" + getAttr(tmpStr, 0) + "'";
		String uname = "'" + getAttr(tmpStr, 1) + "'";
		String minAm = getAttr(tmpStr, 2);
		String maxAm = getAttr(tmpStr, 3);
		String beginTime = "'" + getAttr(tmpStr, 4) + "'";
		String endTime = "'" + getAttr(tmpStr, 5) + "'";
		String maxTNum = getAttr(tmpStr, 6);

		if (message_in.charAt(2) == '0') {
			// Transaction History
			if (!gname.equalsIgnoreCase("''")) {
				sql = "SELECT * FROM Transaction WHERE ";
				sql = sql + "gname=" + gname;
				if (!uname.equalsIgnoreCase("''")) {
					sql = sql + " AND " + "uname=" + uname;
				}
				if (!minAm.equalsIgnoreCase("")) {
					sql = sql + " AND " + "amount>=" + minAm;
				}
				if (!maxAm.equalsIgnoreCase("")) {
					sql = sql + " AND " + "amount<=" + maxAm;
				}
				if (!beginTime.equalsIgnoreCase("''")) {
					sql = sql + " AND " + "time>=" + beginTime;
				}
				if (!endTime.equalsIgnoreCase("''")) {
					sql = sql + " AND " + "time<=" + endTime;
				}
				// Order
				sql = sql + " ORDER BY time DESC";
				// Limit
				if (!maxTNum.equalsIgnoreCase("")) {
					sql = sql + " LIMIT " + maxTNum;
				}
			} else {
				System.out.println("In report for Transactions: Lack gname");
			}
		} else if (message_in.charAt(2) == '1') {
			// Balance
			sql = "SELECT uname,balance FROM User WHERE gname=" + gname;
		} else {
			System.out.println("In report: action_type is an invalid value!");
		}
		return sql;
	}

	public String parseAccount(String message_in) {
		String sql = null;

		String tmpStr = message_in.substring(4);
		String gname = "'" + getAttr(tmpStr, 0) + "'";
		String gpw = "'" + getAttr(tmpStr, 1) + "'";
		String uname = "'" + getAttr(tmpStr, 2) + "'";
		String upw = "'" + getAttr(tmpStr, 3) + "'";

		if (message_in.charAt(2) == '0') {
			// Create group account
			SQLManager sql_manager = new SQLManager();
			ArrayList<ArrayList<String>> findGroup = sql_manager
					.execQuery("SELECT gname FROM UserGroup WHERE gname="
							+ gname);
			if (findGroup.size() == 0) {
				sql = "INSERT INTO UserGroup VALUES(" + gname + "," + gpw + ")";
			} else {
				System.out
						.println("Create Group Account: Group name already exists!");
			}
		} else if (message_in.charAt(2) == '1') {
			// Create user account
			SQLManager sql_manager = new SQLManager();
			ArrayList<ArrayList<String>> findGroup = sql_manager
					.execQuery("SELECT * FROM UserGroup WHERE gname=" + gname);
			if (findGroup.size() != 0) {
				if (findGroup.get(0).get(1)
						.equals(gpw.substring(1, gpw.length() - 1))) {
					ArrayList<ArrayList<String>> findUser = sql_manager
							.execQuery("SELECT uname FROM User WHERE uname="
									+ uname);
					if (findUser.size() == 0) {
						sql = "INSERT INTO User VALUES(" + gname + "," + uname
								+ "," + upw + "," + "0)";
					} else {
						System.out
								.println("Create User Account: User name already exists!");
					}
				} else {
					System.out
							.println("Create User Account: Group password is incorrect!");
				}
			} else {
				System.out
						.println("Create User Account: Group name does not exist!");
			}
		} else if (message_in.charAt(2) == '2') {
			// User login
			// sql = "INSERT INTO User VALUES(" + gname + "," + uname + "," +
			// upw + "," + "0)";
			sql = "SELECT gname,upw FROM User WHERE uname=" + uname;
		}
		return sql;
	}

	/*
	 * Translates results into a string response for the client data_type <sp>
	 * action_type <sp> data\n\n
	 * 
	 * 1. Transaction submission: data_type=0; 0 0 validation_flag\n\n
	 * 
	 * 2. Report request response: data_type=1; 1) Transaction history:
	 * action_type=0; 1 0 tid;gname;uname;amount;time;desc;u1,u2\n ...\n \n
	 * 
	 * 2) Current balance: action_type=1; Send the balance of every user in the
	 * current user's group 1 1 u1:balance,u2:balance...\n\n
	 * 
	 * 3. Account creation/login response: data_type=2; 1) Creation:
	 * action_type=0; 2 0 validation_flag;gname\n\n
	 * 
	 * 2) Login: action_type=1; 2 1 validation_flag\n\n
	 */
	public String translate(String message_in,
			ArrayList<ArrayList<String>> results, boolean validation_flag) {
		int data_type = Character.digit(message_in.charAt(0), 10);
		int action_type = Character.digit(message_in.charAt(2), 10);

		// System.out.println("message in: " + message_in);
		// System.out.println("data type: " + Integer.toString(data_type) +
		// ", action type: " + Integer.toString(data_type));
		// System.out.println("validation flag: " +
		// String.valueOf(validation_flag));
		// System.out.println("results: " + String.valueOf(validation_flag));
		// System.out.println("validation flag: " +
		// String.valueOf(validation_flag));

		String message_out = "";
		if (data_type == 0 && action_type == 0) {
			// Transaction create
			message_out = validation_flag ? "0 0 1\n\n" : "0 0 0\n\n";
		} else if (data_type == 0 && action_type == 1) {
			// Transaction edit
			message_out = validation_flag ? "0 1 1\n\n" : "0 1 0\n\n";
		} else if (data_type == 0 && action_type == 2) {
			// Transaction delete
			message_out = validation_flag ? "0 2 1\n\n" : "0 2 0\n\n";
		} else if (data_type == 1 && action_type == 0) {
			// Transaction history
			message_out += "1 0 ";
			for (int i = 0; i < results.size(); i++) {
				for (int j = 0; j < results.get(i).size(); j++) {
					message_out += results.get(i).get(j);
					if (j != results.get(i).size() - 1) {
						message_out += ";";
					}
				}
				message_out += "\n";
			}
			message_out += "\n";
		} else if (data_type == 1 && action_type == 1) {
			// Current balance
			message_out += "1 1 ";
			for (int i = 0; i < results.size(); i++) {
				for (int j = 0; j < results.get(i).size(); j++) {
					message_out += results.get(i).get(j);
					if (j != results.get(i).size() - 1) {
						message_out += ":";
					}
				}
				if (i != results.size() - 1) {
					message_out += ",";
				}
			}
			message_out += "\n\n";
		} else if (data_type == 2 && action_type == 0) {
			// Group creation
			message_out = validation_flag ? "2 0 1" : "2 0 0";
			String group = getAttr(message_in, 0);
			group = group.substring(4, group.length());
			message_out += ";" + group + ";" + "\n\n";
		} else if (data_type == 2 && action_type == 1) {
			// Account creation
			message_out = validation_flag ? "2 1 1" : "2 1 0";
			String group = getAttr(message_in, 0);
			group = group.substring(4, group.length());
			String user = getAttr(message_in, 2);
			message_out += ";" + group + ";" + user + "\n\n";
		} else if (data_type == 2 && action_type == 2) {
			// Account login
			message_out = (validation_flag && results.get(0).get(1)
					.equals(getAttr(message_in, 3))) ? "2 2 1" : "2 2 0";
			String group = results.get(0).get(0);
			String user = getAttr(message_in, 2);
			message_out += ";" + group + ";" + user + "\n\n";
		} else {
			System.out
					.println("Translate error: Data or action type not recognized.");
		}
		return message_out;
	}

	public String getAttr(String src, int index) {
		String tmpStr = src;
		String result = null;
		int scIndex;
		boolean finish_flag = false;
		boolean find_flag = true;
		for (int i = 0; i <= index && find_flag; i++) {
			if (!finish_flag) {
				scIndex = tmpStr.indexOf(';');
				if (scIndex != -1) {
					result = tmpStr.substring(0, scIndex);
					tmpStr = tmpStr.substring(scIndex + 1);
				} else if (scIndex == -1) {
					scIndex = tmpStr.indexOf('\n');
					result = tmpStr.substring(0, scIndex);
					finish_flag = true;
				}
			} else {
				find_flag = false;
			}
		}
		if (find_flag) {
			return result;
		} else {
			return null;
		}
	}
	
	public void addBalanceMt(String uname, String for_whom, String amount) {
		SQLManager sql_manager = new SQLManager();
		String sql = null;
		int forWhomNum = numForWhom(for_whom);
		double balance;
		for ( int i=0;i<forWhomNum;i++ ) {
			String currentUser = getForWhom(for_whom, i);
			sql = "SELECT balance FROM User WHERE uname='" + currentUser + "'";
			balance = Double.parseDouble(sql_manager.execSQL(sql).get(0).get(0));
			balance = balance - Double.parseDouble(amount)/forWhomNum;
			sql = "UPDATE User SET balance=" + balance + " WHERE uname='" + currentUser + "'";
			sql_manager.execSQL(sql);
		}
		// For user who purchase the thing;
		sql = "SELECT balance FROM User WHERE uname=" + uname;
		balance = Double.parseDouble(sql_manager.execSQL(sql).get(0).get(0)) + Double.parseDouble(amount);
		sql = "UPDATE User SET balance=" + balance + " WHERE uname=" + uname;
		sql_manager.execSQL(sql);
	}
	
	public void deleteBalanceMt(String tid) {
		SQLManager sql_manager = new SQLManager();
		String sql = "SELECT uname,amount,for_whom FROM Transaction WHERE tid=" + tid;
		ArrayList<String> trans = sql_manager.execSQL(sql).get(0);
		String uname = trans.get(0);
		double amount = Double.parseDouble(trans.get(1));
		String for_whom = "'" + trans.get(2) + "'";
		
		int forWhomNum = numForWhom(for_whom);
		double balance;
		
		for ( int i=0;i<forWhomNum;i++ ) {
			String currentUser = getForWhom(for_whom, i);
			sql = "SELECT balance FROM User WHERE uname='" + currentUser + "'";
			balance = Double.parseDouble(sql_manager.execSQL(sql).get(0).get(0));
			balance = balance + amount/forWhomNum;		
			sql = "UPDATE User SET balance=" + balance + " WHERE uname='" + currentUser + "'";
			sql_manager.execSQL(sql);
		}
		// For user who purchase the thing;
		sql = "SELECT balance FROM User WHERE uname='" + uname + "'";
		balance = Double.parseDouble(sql_manager.execSQL(sql).get(0).get(0)) - amount;
		sql = "UPDATE User SET balance=" + balance + " WHERE uname='" + uname + "'";
		sql_manager.execSQL(sql);
	}
	
	public void editBalanceMt(String tid, String uname, String for_whom,
			String amount) {
		SQLManager sql_manager = new SQLManager();
		String sql = "SELECT uname,amount,for_whom FROM Transaction WHERE tid="
				+ tid;
		ArrayList<String> trans = sql_manager.execSQL(sql).get(0);
		String old_uname = trans.get(0);
		String old_amount = trans.get(1);
		String old_for_whom = "'" + trans.get(2) + "'";

		deleteBalanceMt(tid);
		if (uname.equalsIgnoreCase("''")) {
			uname = old_uname;
		}
		if (amount.equalsIgnoreCase("")) {
			amount = old_amount;
		}
		if (for_whom.equalsIgnoreCase("''")) {
			for_whom = old_for_whom;
		}

		addBalanceMt(uname, for_whom, amount);
	}

	public int numForWhom(String src) {
		String tmpStr = src.substring(1, src.length() - 1);
		int cmIndex;
		int num = 0;
		if (!tmpStr.equalsIgnoreCase("")) {
			while ((cmIndex = tmpStr.indexOf(',')) != -1) {
				tmpStr = tmpStr.substring(cmIndex + 1);
				num++;
			}
			num++;
		}
		return num;
	}

	public String getForWhom(String src, int index) {
		String tmpStr = src.substring(1, src.length() - 1);
		String user = null;
		int cmIndex;
		for (int i = 0; i <= index; i++) {
			cmIndex = tmpStr.indexOf(',');
			if (cmIndex != -1) {
				user = tmpStr.substring(0, cmIndex);
				tmpStr = tmpStr.substring(cmIndex + 1);
			} else {
				user = tmpStr;
			}
		}
		return user;
	}
}
