package edu.purdue.financesharer;
import edu.purdue.cs.cs252.channel.*;


public class ClientComm implements MessageListener{
	UserBalance[] userBalances = new UserBalance[30];
	Transaction[] myTrans = new Transaction[30];
	String user;
	String group;   
	String balanceUsedFor;
	int balanceTranNum;
	boolean useChannel=true;
	boolean selfRespond=false;
	
	
	int numTrans;
	int numUB;
	
	final Model model = new Model();
	AndroidChannel channel = null;
    //TCPChannel channel = null;
    MainActivity UI = null;

    
	ClientComm(MainActivity myUI){
		//this.UI = newUI;
		
		UI = myUI;
		
	}
	
	public void messageReceived(String message, int channelID){
			this.messageReceived(message);
	}

    public void messageReceived(String message){//, int channelID) {
    	message = message + "\n\n";                                 
        System.out.println("REC:"+message);
        
        if (message.charAt(0)=='0'){
        	if (message.charAt(4)=='1'){
            	UI.requestViewTrans();
        	}
    	}
    	if (message.charAt(0)=='1'){
    		if (message.charAt(2)=='0'){
    			int transIndex=4;
    			int endIndex;
    			this.numTrans=0;
    			String[] args;
    			while (message.charAt(transIndex)!='\n'){
    				endIndex = message.indexOf('$',transIndex);
    				args = message.substring(transIndex,endIndex).split(";",7);
    				
    				args[3] = String.format("%.2f", Float.parseFloat(args[3]));
    				this.myTrans[this.numTrans]=new Transaction(args[0],args[1],args[2],args[3],args[4],args[5],args[6]);
    				
    				this.numTrans++;
    				transIndex = message.indexOf('$',transIndex)+1;
    				
    				System.out.println(""+transIndex);
    			}
    			UI.gotoViewTrans();
    		}
    		if (message.charAt(2)=='1'){
        		int pairIndex=4;
        		int colonIndex;
        		int endIndex;
        		this.numUB=0;
        		while (pairIndex>0){
        			colonIndex=message.indexOf(':',pairIndex);
        			endIndex=message.indexOf(',',pairIndex);
        			if (endIndex==-1){
        				endIndex=message.indexOf('\n');
        			}
        			
        			String amount = message.substring(colonIndex+1,endIndex);
    				amount = String.format("%.2f", Float.parseFloat(amount));
    				
        			this.userBalances[this.numUB]= new UserBalance(message.substring(pairIndex,colonIndex),amount);
        			this.numUB++;
        			
        			pairIndex=message.indexOf(',',pairIndex)+1;
        		}
        		
        		if (this.balanceUsedFor.equals("view")){
        			UI.gotoViewBalances();
        		}
        		if (this.balanceUsedFor.equals("add")){
        			UI.gotoAddTran(this.balanceTranNum);
        		}
        		if (this.balanceUsedFor.equals("search")){
        			UI.gotoSearchTrans();
        		}
    		}
    	}
    	if (message.charAt(0)=='2'){
    		int firstColon = message.indexOf(';');
    		int secondColon = message.indexOf(';',firstColon+1);
    		String groupname = message.substring(firstColon+1,secondColon);
    		String username = message.substring(secondColon+1,message.indexOf('\n'));
    		if (message.charAt(2)=='0'){
    			if (message.charAt(4)=='0'){//creation bad
    				UI.gotoSignin("Group Creation Failed");
    			}
    			if (message.charAt(4)=='1'){//creation good
    				UI.gotoSignin("Group "+groupname+" Created Successfully");
    			}   
    		}
    		if (message.charAt(2)=='1'){
    			if (message.charAt(4)=='0'){//creation bad
    				UI.gotoSignin("User Creation Failed");
    			}   
    			if (message.charAt(4)=='1'){//creation good
    				UI.gotoSignin("User "+username+" Created Successfully In "+groupname);
        			
    			}   
    		}     		
    		if (message.charAt(2)=='2'){
    			if (message.charAt(4)=='0'){//login bad
    				UI.gotoSignin("Authentification Failed");
    			}
    			if (message.charAt(4)=='1'){//login good
    				this.group = groupname;
    				this.user = username;
    				this.balanceUsedFor="view";
    				this.sendMessage("1 1 "+this.group+";;;;;\n\n");
    			}        		
    		}   	  			
    	}
        // simply print the message.

    }

    public void sendMessage(String message) {
        // send a message, since we did not specify a client ID, then the
        // message will be sent to the server.
        System.out.println("SEND:"+message);

        if (selfRespond){
	        if (message.charAt(0)=='0' && message.charAt(2)=='0'){
	        	this.messageReceived("0 0 1\n\n", 0);
	        }
	        if (message.charAt(0)=='0' && message.charAt(2)=='1'){
	        	this.messageReceived("0 1 1\n\n", 0);
	        }
	        if (message.charAt(0)=='0' && message.charAt(2)=='2'){
	        	this.messageReceived("0 2 1\n\n", 0);
	        }
	        
	        if (message.charAt(0)=='2' && message.charAt(2)=='0'){
	        	if (message.charAt(4)=='a'){
	        		this.messageReceived("2 0 1;"+message.substring(4,message.indexOf(';'))+";Steve\n\n", 0);
	        	}
	        	else{
	        		this.messageReceived("2 0 0;;\n\n",0);
	        	}
	        }
	        if (message.charAt(0)=='2' && message.charAt(2)=='1'){
	        	if (message.charAt(4)=='a'){
	        		this.messageReceived("2 1 1;"+message.substring(4,message.indexOf(';'))+";Steve\n\n", 0);
	        	}
	        	else{
	        		this.messageReceived("2 1 0;;\n\n",0);
	        	}
	        }
	        if (message.charAt(0)=='2' && message.charAt(2)=='2'){
	        	if (message.charAt(6)=='S'){
	        		this.messageReceived("2 2 1;apt4;Steve\n\n", 0);
	        	}
	        	else{
	        		this.messageReceived("2 2 0;;\n\n",0);
	        	}
	        }
	        
	        if (message.charAt(0)=='1' && message.charAt(2)=='1'){
	        	this.messageReceived("1 1 Steve:1.0111,St:2.00,Niral:3.00\n\n",0);
	        }
	        
	        if (message.charAt(0)=='1' && message.charAt(2)=='0'){
	        	String temp = "1 0 ";
	        	for(int i=0; i<20; i++){
	        		temp = temp + i + ";apt4;Steve;1.0111;2013-11-11;Walmart;Steve$";
	        	}
	        	temp = temp + "\n\n";
	        	this.messageReceived(temp,0);
	        }
        }
        
        if (useChannel){
	        //try {
        	//System.out.println(message);
	        this.channel.sendMessage(message);
	        //} catch (ChannelException e) {
	        //    e.printStackTrace();
	        //}
        }
        
        
    }
    
    
	void initializePort(String address, int portNumber){
        
		System.out.println(""+portNumber);
		
		if (useChannel){
			//try {
				this.channel = new AndroidChannel(this.UI, this.model, address, portNumber);
            	//this.channel = new TCPChannel("moore01.cs.purdue.edu", portNumber);
        	//} catch (ChannelException e) {
            	//e.printStackTrace();
            	//System.err.println("Cannot Open Port");
        	//}
        	//System.err.println("Port Open Successful");
        	//this.channel.setMessageListener(this);
		}
        
	
	}
	
	
	
}


/*	
	boolean verify(String username, String password){
		//this.sendMessage("2 2 ;;"+username+";"+password+"\n\n");
		
		if (username.equals("Steve") && password.equals("password")){
			return true;
		}
		return false;
	}
	
	String getGroup(String username){
		return "apartment4";
	}
	

	UserBalance[] getUserBalances(String groupname){
		return userBalances;
	}

	
	boolean addTran(String t, String g, String b, String d, String a, String fw, String desc){
		myTrans[numTrans] = new Transaction(t,g,b,d,a,fw,desc);
		numTrans++;
		return true;
	}//NEEDS TO RETURN BOOLEAN THAT INDICATES IF ADDED SUCCESSFULLY
	
	void deleteTran(int index){
		myTrans[index] = new Transaction("del","del","del","del","del","del","del");
	}

	
	Transaction[] getSearchTrans(String user, String lowDate, String highDate, String lowAmount, String highAmount){
		if (user.equals("") && lowDate.equals("") && highDate.equals("") && lowAmount.equals("") && highAmount.equals("")){
			return myTrans;
		}
		
		Transaction[] tempTrans = new Transaction[2];
		tempTrans[0] = new Transaction("0","",user,lowDate,lowAmount,"abc","def");
		tempTrans[1] = new Transaction("1","",user,highDate,highAmount,"abc","def");
		return tempTrans;		
	}

	boolean createGroup(String groupname, String grouppassword){
		if (groupname.equals("")){
			return false;
		}
		return true;
	}

	boolean createUser(String username, String userpassword, String groupname, String grouppassword){
		if (username.equals("")){
			return false;
		}
		userBalances[numUB] = new UserBalance(username,"0.00");
		numUB++;
		return true;
	}
	*/

