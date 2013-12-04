package edu.purdue.financesharer;

import android.os.AsyncTask;
import android.os.Handler;
import edu.purdue.cs.cs252.channel.ChannelException;
import edu.purdue.cs.cs252.channel.MessageListener;
import edu.purdue.cs.cs252.channel.TCPChannel;

/**
 * AndroidChannel is a layer on top of the Channel/TCPChannel classes for managing connections to the Server, supporting
 * the background network processing required by the Android OS.
 * 
 * @author jtk
 * 
 */
public class AndroidChannel implements MessageListener {
    private Model model;
    private TCPChannel channel;
    private AndroidHandler androidHandler;

    /**
     * Class constructor specifying the model, host name, and port number of the server. Opens the TCPChannel in
     * background.
     * 
     * @param mainActivity
     * @param model
     * @param host
     * @param port
     */
    public AndroidChannel(MainActivity mainActivity, Model model, String host, int port) {
        this.model = model;
        new ChannelOpener(this, model, host, port).execute();
        androidHandler = new AndroidHandler(mainActivity);
    }

    /**
     * Sends a message to the server. Handles the message processing in the background so it can be called from the UI
     * thread.
     * 
     * @param message
     */
    public void sendMessage(String message) {
        new MessageSender(model, channel, message).execute();
    }

    /**
     * Sets the TCPChannel once it has been allocated. Used only internally.
     * 
     * @param channel
     */
    public void setTCPChannel(TCPChannel channel) {
        this.channel = channel;
    }

    /**
     * Handles messages received from the Server, passing them to the UI thread via a handler to the MainActivity.
     */
    @Override
    public void messageReceived(String message, int clientID) {
        android.os.Message msg = new android.os.Message();
        msg.obj = message;
        androidHandler.sendMessage(msg);
    }
}

/**
 * ChannelOpener opens a channel to the Server, but doing so using an ASyncTask worker thread so that it can be called
 * from the UI thread.
 * 
 * @author jtk
 */
class ChannelOpener extends AsyncTask<Void, Void, String> {
    private AndroidChannel androidChannel;
    private Model model;
    private String hostName;
    private int portNumber;

    ChannelOpener(AndroidChannel androidChannel, Model model, String hostName, int portNumber) {
        this.androidChannel = androidChannel;
        this.model = model;
        this.hostName = hostName;
        this.portNumber = portNumber;
    }

    @Override
    protected void onPreExecute() {
        model.setStatus("Opening channel to server");
        model.setUIEnabled(false); // disable Submit button until server connection made
        model.notifyObservers();
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            System.out.printf("OPENING channel\n");
            TCPChannel channel = new TCPChannel(hostName, portNumber);
            androidChannel.setTCPChannel(channel);
            channel.setMessageListener(androidChannel);
            System.out.printf("CHANNEL opened\n");
            return "Channel opened to server";
        } catch (ChannelException e) {
            System.err.printf("ERROR: Connection to server at %s:%d failed to open\n", hostName, portNumber);
            return "Could not open channel to server";
        }
    }

    @Override
    protected void onPostExecute(String status) {
        model.setUIEnabled(true);
        model.setStatus(status);
        model.notifyObservers();
    }
}

/**
 * MessageSender sends a message to the Server, but doing so using an ASyncTask worker thread so that it can be called from
 * the UI thread.
 * 
 * @author jtk
 */
class MessageSender extends AsyncTask<Void, Void, String> {
    Model model;
    TCPChannel channel;
    String message;

    MessageSender(Model model, TCPChannel channel, String message) {
        this.model = model;
        this.channel = channel;
        this.message = message;
    }

    protected String doInBackground(Void... params) {
        if (channel == null)
            return "Channel to server is not available; try again later...";
        else
            try {
                channel.sendMessage(message);
                return "Message sent; waiting...";
            } catch (ChannelException e) {
                System.err.printf("ERROR: sendMessage to server failed\n");
                return "Could not send message to server; try again later...";
            }
    }

    protected void onPostExecute(String status) {
        model.setUIEnabled(true);
        model.setStatus(status);
        model.notifyObservers();
    }
}

/**
 * Handles messages received by the Channel thread that need to be acted on by the UI thread.
 */
class AndroidHandler extends Handler {
    MainActivity mainActivity;

    AndroidHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    /**
     * Note: This method is run on the UI thread, so can update the GUI.
     */
    @Override
    public void handleMessage(android.os.Message msg) {
        String message = (String) msg.obj;
        mainActivity.myClient.messageReceived(message);
    }
}
