package edu.purdue.financesharer;

import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;




public class MainActivity extends Activity {


	//Transaction[] myTrans;
	//UserBalance[] myUserBalances;
	
	ClientComm myClient;
	//String thisUser;
	//String thisGroup;
	
	String searchBuyer = "";
	String searchLowDate = "";
	String searchHighDate = "";
	String searchLowAmount = "";
	String searchHighAmount = "";
	
	
	void gotoEnterPort(){
		setContentView(R.layout.enter_port);
		
        final Button button1 = (Button) findViewById(R.id.enter);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText et1=(EditText)findViewById(R.id.portNumberBox);   
                int portNumber = Integer.parseInt(et1.getText().toString());   
                
                if (portNumber>1024 && portNumber<65536){
                	myClient.initializePort(portNumber);
                    gotoSignin("Welcome!");
                }

                
            }
        });
        
	}
	void gotoSignin(String error){
		setContentView(R.layout.signin);

        final TextView errorMsg = (TextView) findViewById(R.id.errorText);
        errorMsg.setText(error);
        
        final Button button1 = (Button) findViewById(R.id.signIn);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText et1=(EditText)findViewById(R.id.usernameBox);  
                EditText et2=(EditText)findViewById(R.id.passwordBox);  
                String username = et1.getText().toString();  
                String password = et2.getText().toString();  
                
                searchBuyer="";
                searchLowDate="1990-01-01";
                searchHighDate="2030-01-01";
                searchLowAmount="0.00";
                searchHighAmount="1000000.00";
                
                //myClient.user = username;
                myClient.sendMessage("2 2 ;;"+username+";"+password+"\n\n");

            }
        });
        
        final Button button2 = (Button) findViewById(R.id.createUser);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	gotoCreateUser();
            }
        });
        
        final Button button3 = (Button) findViewById(R.id.createGroup);
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	gotoCreateGroup();
            }
        });
        
	}

	void gotoViewTran(final int tranNum){
		setContentView(R.layout.view_tran);	
		
        TextView mytextview = (TextView) findViewById(R.id.buyerView);		
		mytextview.setText("Buyer: "+myClient.myTrans[tranNum].buyer);
        mytextview = (TextView) findViewById(R.id.dateView);		
		mytextview.setText("Date: "+myClient.myTrans[tranNum].date);
        mytextview = (TextView) findViewById(R.id.amountView);		
		mytextview.setText("Amount: $"+myClient.myTrans[tranNum].amount);
        mytextview = (TextView) findViewById(R.id.forWhoView);		
		mytextview.setText("For: "+myClient.myTrans[tranNum].forWho);
        mytextview = (TextView) findViewById(R.id.descriptionView);		
		mytextview.setText("Memo: "+myClient.myTrans[tranNum].description);
		
        mytextview = (TextView) findViewById(R.id.userText);		
		mytextview.setText(myClient.user);		
        mytextview = (TextView) findViewById(R.id.groupText);		
		mytextview.setText(myClient.group);	
		final Button button1 = (Button) findViewById(R.id.signOut);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                gotoSignin("");
            }
        });
        
        final Button button2 = (Button) findViewById(R.id.gotoTrans);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {   
            	requestViewTrans();
            }
        });
        
        final Button button3 = (Button) findViewById(R.id.gotoBalances);
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {  
            	myClient.balanceUsedFor="view";
            	myClient.sendMessage("1 1 "+myClient.group+";;;;;\n\n"); 
            }
        });
        
        final Button button4 = (Button) findViewById(R.id.changeTran);
        button4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { 
            	myClient.balanceUsedFor="add";
            	myClient.balanceTranNum = tranNum;
            	myClient.sendMessage("1 1 "+myClient.group+";;;;;\n\n");    
            }
        });
        
        final Button button5 = (Button) findViewById(R.id.deleteTran);
        button5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            	myClient.sendMessage("0 2 "+myClient.myTrans[tranNum].tid+";;;;;;\n\n");

            }
        });
	}

	
	void gotoAddTran(final int tranNum){
		setContentView(R.layout.create_tran);		
		
		final RadioButton[] theseRadioButtons = new RadioButton[myClient.numUB];
		final CheckBox[] theseCheckBoxes = new CheckBox[myClient.numUB];
		
    	for(int i=0; i<myClient.numUB; i++){
    		RadioGroup rg = (RadioGroup) findViewById(R.id.buyerRadioGroup);
    		LinearLayout ll = (LinearLayout)findViewById(R.id.forWhoCheckboxGroup);
    		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    		
    		theseRadioButtons[i] = new RadioButton(this);
    		theseCheckBoxes[i] = new CheckBox(this);
    		theseRadioButtons[i].setText(myClient.userBalances[i].user);
    		theseCheckBoxes[i].setText("");
    		
    		rg.addView(theseRadioButtons[i], lp);
    		ll.addView(theseCheckBoxes[i], lp);

    	}
    	
    	if (tranNum!=-1){
	        TextView mytextview = (TextView) findViewById(R.id.dateBox);		
			mytextview.setText(myClient.myTrans[tranNum].date);
	        mytextview = (TextView) findViewById(R.id.amountBox);		
			mytextview.setText(myClient.myTrans[tranNum].amount);
	        mytextview = (TextView) findViewById(R.id.descriptionBox);		
			mytextview.setText(myClient.myTrans[tranNum].description);
			
			for(int i=0; i<myClient.numUB; i++){
				if (myClient.myTrans[tranNum].buyer.equals(myClient.userBalances[i].user)){
					theseRadioButtons[i].setChecked(true);
				}
				if (myClient.myTrans[tranNum].forWho.contains(myClient.userBalances[i].user)){
					theseCheckBoxes[i].setChecked(true);
				}
			}
    	}
		
        final Button button1 = (Button) findViewById(R.id.createTran);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	String buyer="";
            	String forWho="";
            	for(int i=0; i<myClient.numUB; i++){
            		if (theseRadioButtons[i].isChecked()){
            			buyer = myClient.userBalances[i].user;
            		}
            		if (theseCheckBoxes[i].isChecked()){
            			if (!forWho.equals("")){
            				forWho += ",";
            			}
            			forWho += myClient.userBalances[i].user;
            		}	
            	}
                EditText et1=(EditText)findViewById(R.id.dateBox); 
                EditText et2=(EditText)findViewById(R.id.amountBox);    
                EditText et3=(EditText)findViewById(R.id.descriptionBox);  
                String date = et1.getText().toString();
                String amount = et2.getText().toString();  
                String description = et3.getText().toString();
                
                if (tranNum==-1){
                	myClient.sendMessage("0 0 ;"+myClient.group+";"+buyer+";"+amount+";"+date+";"+description+";"+forWho+"\n\n");
                }
                else{
                	myClient.sendMessage("0 1 "+myClient.myTrans[tranNum].tid+";"+myClient.group+";"+buyer+";"+amount+";"+date+";"+description+";"+forWho+"\n\n");
                }
                //if (myClient.addTran("","",buyer, date, amount, forWho, description)){
                //	myClient.deleteTran(tranNum);
                //	requestViewTrans();
            	//}
            }
        });
        
        final Button button2 = (Button) findViewById(R.id.cancel);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { 
            	if (tranNum==-1){
            		requestViewTrans();
            	}
            	else{
            		gotoViewTran(tranNum);    
            	}
            }
        });
	}
	
	void gotoSearchTrans(){
		setContentView(R.layout.search_trans);		

		final CheckBox[] theseCheckBoxes = new CheckBox[myClient.numUB];
		
    	for(int i=0; i<myClient.numUB; i++){
    		LinearLayout ll = (LinearLayout)findViewById(R.id.buyerCheckboxes);
    		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    		
    		theseCheckBoxes[i] = new CheckBox(this);
    		theseCheckBoxes[i].setText(myClient.userBalances[i].user);
    		
    		ll.addView(theseCheckBoxes[i], lp);
    	}
    	
        final Button button1 = (Button) findViewById(R.id.searchTrans);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	searchBuyer="";
            	for(int i=0; i<myClient.numUB; i++){
            		if (theseCheckBoxes[i].isChecked()){
            			if (!searchBuyer.equals("")){
            				searchBuyer += ",";
            			}
            			searchBuyer += myClient.userBalances[i].user;
            		}	
            	} 
                EditText et2=(EditText)findViewById(R.id.lowDateBox); 
                EditText et3=(EditText)findViewById(R.id.highDateBox);  
                EditText et4=(EditText)findViewById(R.id.lowAmountBox);  
                EditText et5=(EditText)findViewById(R.id.highAmountBox);  
                searchLowDate = et2.getText().toString();
                searchHighDate = et3.getText().toString();  
                searchLowAmount = et4.getText().toString();
                searchHighAmount = et5.getText().toString();
                requestViewTrans();
            }
        });
        
        final Button button2 = (Button) findViewById(R.id.cancel);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {   
            	requestViewTrans();
            }
        });
	}
	
	void requestViewTrans(){
		myClient.sendMessage("1 0 "+myClient.group+";"+searchBuyer+";"+searchLowAmount+";"+searchHighAmount+";"+searchLowDate+";"+searchHighDate+";30\n\n");
	}
	void gotoViewTrans(){
    	//myTrans = myClient.getSearchTrans(searchBuyer, searchLowDate, searchHighDate, searchLowAmount, searchHighAmount);
    	setContentView(R.layout.view_trans);		


    	for(int i=0; i<myClient.numTrans; i++){
    		LinearLayout ll = (LinearLayout)findViewById(R.id.transactionLayout);
    		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    		
    		TextView myButton = new TextView(this);
    		myButton.setText(myClient.myTrans[i].getShort());
    		final int temp = i;
    		myButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    gotoViewTran(temp);
                }
            });
            
            
    		ll.addView(myButton, lp);
    	}

		TextView mytextview = (TextView) findViewById(R.id.userText);		
		mytextview.setText(myClient.user);		
        mytextview = (TextView) findViewById(R.id.groupText);		
		mytextview.setText(myClient.group);
        final Button button1 = (Button) findViewById(R.id.signOut);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                gotoSignin("");
            }
        });
        
        final Button button2 = (Button) findViewById(R.id.gotoBalances);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {   
            	myClient.balanceUsedFor="view";
            	myClient.sendMessage("1 1 "+myClient.group+";;;;;\n\n"); 
            }
        });
        
        final Button button3 = (Button) findViewById(R.id.searchTrans);
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {  
            	myClient.balanceUsedFor="search";
            	myClient.sendMessage("1 1 "+myClient.group+";;;;;\n\n");    
            }
        });
        
        final Button button4 = (Button) findViewById(R.id.mostRecent);
        button4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {   
            	searchBuyer="";
            	searchLowDate="1990-01-01";
            	searchHighDate="2030-01-01";
            	searchLowAmount="0.00";
            	searchHighAmount="1000000.00";
                
            	requestViewTrans();
            }
        });
        
        final Button button5 = (Button) findViewById(R.id.addTran);
        button5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {   
            	myClient.balanceUsedFor="add";
            	myClient.balanceTranNum = -1;
            	myClient.sendMessage("1 1 "+myClient.group+";;;;;\n\n");     
            }
        });
        
	}
	
	void gotoViewBalances(){
		setContentView(R.layout.view_balances);		
		
		//myUserBalances = myClient.getUserBalances(myClient.group);
		
    	for (int i=0; i<myClient.numUB;i++){
    		LinearLayout ll = (LinearLayout)findViewById(R.id.balancesLayout);
    		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    		
    		TextView myTextView = new TextView(this);
    		myTextView.setText(myClient.userBalances[i].user + ": $" + myClient.userBalances[i].balance);

    		ll.addView(myTextView, lp);
    	}

		TextView mytextview = (TextView) findViewById(R.id.userText);		
		mytextview.setText(myClient.user);		
        mytextview = (TextView) findViewById(R.id.groupText);		
		mytextview.setText(myClient.group);
        final Button button1 = (Button) findViewById(R.id.signOut);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                gotoSignin("");
            }
        });
        
        final Button button2 = (Button) findViewById(R.id.gotoTrans);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {   
            	requestViewTrans();
            }
        });
	}
	void gotoCreateGroup(){
		setContentView(R.layout.create_group);
		
        final Button button1 = (Button) findViewById(R.id.createGroup);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText et1=(EditText)findViewById(R.id.groupnameBox);  
                EditText et2=(EditText)findViewById(R.id.groupPasswordBox);  
                String groupname = et1.getText().toString();  
                String grouppassword = et2.getText().toString();

                myClient.sendMessage("2 0 "+groupname+";"+grouppassword+";;\n\n");
                //if (myClient.createGroup(groupname,grouppassword)){
                //	gotoSignin("Group Created: "+groupname);
                //}
                
            }
        });
        
        final Button button2 = (Button) findViewById(R.id.cancel);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                
                gotoSignin("");
                
            }
        });
        
        
	}
	
	void gotoCreateUser(){
		setContentView(R.layout.create_user);
		
        final Button button1 = (Button) findViewById(R.id.createUser);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText et1=(EditText)findViewById(R.id.usernameBox);  
                EditText et2=(EditText)findViewById(R.id.userPasswordBox); 
                EditText et3=(EditText)findViewById(R.id.groupnameBox);  
                EditText et4=(EditText)findViewById(R.id.groupPasswordBox);  
                String username = et1.getText().toString();  
                String userpassword = et2.getText().toString();
                String groupname = et3.getText().toString();  
                String grouppassword = et4.getText().toString();
                
                myClient.sendMessage("2 1 "+groupname+";"+grouppassword+";"+username+";"+userpassword+"\n\n");
                //if (myClient.createUser(username, userpassword, groupname, grouppassword)){
                //    gotoSignin("User Created: "+username);
                //}
                
            }
        });
        
        final Button button2 = (Button) findViewById(R.id.cancel);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                
                gotoSignin("");
                
            }
        });
        
        
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	    StrictMode.setThreadPolicy(policy);
	      
		myClient = new ClientComm(this);

        gotoEnterPort();
        

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


}
