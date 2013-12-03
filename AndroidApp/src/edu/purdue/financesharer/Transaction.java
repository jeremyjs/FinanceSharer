package edu.purdue.financesharer;


public class Transaction {
	String tid;
	String group;
	String buyer;
	String date;
	String amount;
	String forWho;
	String description;
	
	Transaction(String t, String g, String b, String a, String d, String desc, String fw){
			this.tid = t;
			this.group = g;
			this.buyer = b;
			this.amount = a;
			this.date = d;
			this.forWho = fw;
			this.description = desc;
	}
	
	String getShort(){
		return this.buyer + " $" + this.amount + " "+ this.date;
	}
}

