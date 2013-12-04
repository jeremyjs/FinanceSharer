public class FSServer extends Server {

	public FSServer() throws ChannelException {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void messageReceived(String message, int clientID) {
		System.out.println(message);
		// add back \n\n which is lost in transmission
		message = message + "\n\n";
		try {
			RequestProcessor rp = new RequestProcessor();
			String message_out = rp.processRequest(message);
			System.out.println(message_out);
			channel.sendMessage(message_out, clientID);
		} catch (ChannelException e) {
			e.printStackTrace();
		}
	}
	
	public void unitTest() throws ChannelException {
		// create the objects.
		Server server = new FSServer();
		Client client1 = new Client();
		Client client2 = new Client();
		// TODO Auto-generated method stub
		// send messages from clients.
		client1.sendMessage("2 2 ;;b;b\n\n");
		client1.sendMessage("2 1 g1;gp1;u1;up1\n\n");
		client1.sendMessage("2 1 g1;gp1;u2;up2\n\n");
		client1.sendMessage("2 1 g1;gp1;u3;up3\n\n");
		client1.sendMessage("0 0 ;g1;u3;30;2013-11-30;abcd;u1,u2,u3\n\n");
		client1.sendMessage("0 1 5;g1;u1;60;2013-11-30;abcdef;u1,u2,u3\n\n");
		client1.sendMessage("0 2 5;;;;;;\n\n");
		// wait for the reply.
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// close the client and server channels.
		client1.channel.close();
		client2.channel.close();
		server.channel.close();
	}

	public static void main(String[] args) throws ChannelException {
		// create the server object
		Server server = new FSServer();
	}
}