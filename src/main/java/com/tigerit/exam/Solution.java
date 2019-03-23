package com.tigerit.exam;


import static com.tigerit.exam.IO.*;
import java.util.*;

/**
 * All of your application logic should be placed inside this class.
 * Remember we will load your application from our custom container.
 * You may add private method inside this class but, make sure your
 * application's execution points start from inside run method.
 */
public class Solution implements Runnable {
	// for holding database table information
	public class Table {
		ArrayList <String> columnNames;
		HashMap <String, Integer> columnMap;
		ArrayList< ArrayList <Integer> > entryList;

		public Table() {
			columnNames = new ArrayList <String>();
			entryList = new ArrayList< ArrayList <Integer> >();
			columnMap = new HashMap <String, Integer>();
		}

		public String toString() {
			return "columnNames = " + columnNames + "\n"
					+ "columnMap = " + columnMap + "\n"
					+ "entryList = " + entryList + "\n";
		}
	}

	// for handling queries
	public class Query {
		String table1Name, table2Name, table1JoinColumn, table2JoinColumn;
		ArrayList < ArrayList <String> > selectList; // stores select entries as { table, column }

		public Query() {
			selectList = new ArrayList < ArrayList <String> >();
		}

		public String toString() {
			return "table1Name = " + table1Name + "\n"
					+ "table2Name = " + table2Name + "\n"
					+ "table1JoinColumn = " + table1JoinColumn + "\n"
					+ "table2JoinColumn = " + table2JoinColumn + "\n"
					+ "selectList = " + selectList + "\n";
		}

		// parses queries from input
		public void parseQuery() {
			String tempStr = readLine();
			ArrayList <String> selectRow = parseString(tempStr, " ");
			tempStr = readLine();
			ArrayList <String> fromRow = parseString(tempStr, " ");
			tempStr = readLine();
			ArrayList <String> joinRow = parseString(tempStr, " ");
			tempStr = readLine();
			ArrayList <String> onRow = parseString(tempStr, " ");
			tempStr = readLine(); // reading empty line
			
			// parsing name and data of tables involved in join operation
			table1Name = fromRow.get(1);
			table2Name = joinRow.get(1);
			Table table1 = tableList.get(table1Name);
			Table table2 = tableList.get(table2Name);

			// parsing tables' short names
			HashMap <String, String> tableShortNameMap = new HashMap <String, String>();
			if(fromRow.size() == 2) { // case -> no short name
				tableShortNameMap.put(fromRow.get(1), fromRow.get(1));
				tableShortNameMap.put(joinRow.get(1), joinRow.get(1));
			}
			else {
				tableShortNameMap.put(fromRow.get(2), fromRow.get(1));
				tableShortNameMap.put(joinRow.get(2), joinRow.get(1));	
			}

			// parsing select information
			if(selectRow.size() == 2) { // select * case
				for(String column : table1.columnNames) {
					ArrayList <String> p = new ArrayList <String>();
					p.add(table1Name);
					p.add(column);
					selectList.add(p);
				}
				for(String column : table2.columnNames) {
					ArrayList <String> p = new ArrayList <String>();
					p.add(table2Name);
					p.add(column);
					selectList.add(p);
				}
			}
			else {
				for(String x : selectRow) {
					if(!x.contains(".")) continue;
					String str = x.replace(',', ' ').trim();
					ArrayList <String> tempList = parseString(str, ".");
					ArrayList <String> p = new ArrayList <String>();
					p.add(tableShortNameMap.get(tempList.get(0)));
					p.add(tempList.get(1));
					selectList.add(p);
				}
			}

			// parsing exact columns on which join should be applied
			for(String x : onRow) {
				if(!x.contains(".")) continue;
				ArrayList <String> tempList = parseString(x, ".");
				if(table1Name.equals(tableShortNameMap.get(tempList.get(0)))) table1JoinColumn = tempList.get(1);
				if(table2Name.equals(tableShortNameMap.get(tempList.get(0)))) table2JoinColumn = tempList.get(1);
			}
		}

		// resolves queries
		public void resolveQuery() {
			answer.clear();
			Table table1 = tableList.get(table1Name);
			Table table2 = tableList.get(table2Name);
			int table1JoinColumnIndex = table1.columnMap.get(table1JoinColumn);
			int table2JoinColumnIndex = table2.columnMap.get(table2JoinColumn);
			for(ArrayList <Integer> table1Entry : table1.entryList) {
				for(ArrayList <Integer> table2Entry : table2.entryList) {
					if(Integer.compare(table1Entry.get(table1JoinColumnIndex), table2Entry.get(table2JoinColumnIndex)) == 0) {
						ArrayList <Integer> answerRow = new ArrayList <Integer>();
						for(ArrayList <String> selectEntry : selectList) {
							if(table1Name.equals(selectEntry.get(0))) {
								int columnIndex = table1.columnMap.get(selectEntry.get(1));
								answerRow.add(table1Entry.get(columnIndex));
							}
							else {
								int columnIndex = table2.columnMap.get(selectEntry.get(1));
								answerRow.add(table2Entry.get(columnIndex));	
							}
						}
						answer.add(answerRow);
					}
				}
			}
		}

		// sorts rows lexicographically, and prints query answer
		public void printAnswer() {
			Collections.sort(answer, new AnswerComparator());
			boolean isPrinted = false;
			for(ArrayList <String> selectEntry : selectList) {
				if(isPrinted) System.out.print(" ");
				else isPrinted = true;
				System.out.print(selectEntry.get(1));
			}
			System.out.println();
			for(ArrayList <Integer> answerRow : answer) {
				isPrinted = false;
				for(Integer x : answerRow) {
					if(isPrinted) System.out.print(" ");
					else isPrinted = true;
					System.out.print(x);		
				}
				System.out.println();
			}
			System.out.println();
		}
	}

	// holds compare function for sorting answer rows
	public class AnswerComparator implements Comparator< ArrayList <Integer> > {
		public int compare(ArrayList <Integer> a, ArrayList <Integer> b) {
			for(int i=0; i<a.size(); i++) {
				if(Integer.compare(a.get(i), b.get(i)) == 0) continue;
				return Integer.compare(a.get(i), b.get(i));
			}
			return 0;
		}
	}

	public static HashMap <String, Table> tableList; // stores table data against table name
	public static ArrayList < ArrayList <Integer> > answer; // stores answer

	// tokenizes and returns an ArrayList of String wrt delim
	public ArrayList <String> parseString(String str, String delim) {
		StringTokenizer stringTokenizer = new StringTokenizer(str, delim);
        ArrayList<String> ret = new ArrayList<String>();
        while(stringTokenizer.hasMoreTokens()) {
            ret.add(stringTokenizer.nextToken());
        }
        return ret;
	}

	// reads new table from input
	public void readNewTable() {
		String tableName = readLine();
		Table table = new Table();
		String tempStr = readLine();
		ArrayList <String> tempList = parseString(tempStr, " ");
		int columnCount = Integer.parseInt(tempList.get(0));
		int entryCount  = Integer.parseInt(tempList.get(1));
		tempStr = readLine();
		table.columnNames = parseString(tempStr, " ");
		for(int i=0; i<columnCount; i++) {
			table.columnMap.put(table.columnNames.get(i), i);
		}
		for(int i=0; i<entryCount; i++) {
			tempStr = readLine();
			tempList = parseString(tempStr, " ");
			ArrayList <Integer> entry = new ArrayList <Integer>();
			for(String x : tempList) entry.add(new Integer(x));
			table.entryList.add(entry);
		}
		// System.out.println(table);
		tableList.put(tableName, table);
	}

	@Override
    public void run() {
        // your application entry point
    	int test = readLineAsInteger(), kase=1;
    	tableList = new HashMap <String, Table>();
    	answer = new ArrayList < ArrayList <Integer> >();
    	while(test-- > 0) {
    		System.out.println("Test: " + kase++);
    		// reading and storing input tables
    		tableList.clear();
    		int tableCount = readLineAsInteger();
    		while(tableCount-- > 0) readNewTable();
    		// handling queries
    		int q = readLineAsInteger();
    		while(q-- > 0) {
    			Query query = new Query();
    			query.parseQuery();
    			// System.out.println(query);
    			query.resolveQuery();
    			query.printAnswer();
    		}
    	}
    }
}
