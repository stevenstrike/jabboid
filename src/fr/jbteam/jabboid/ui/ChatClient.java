/**
 * Basic XMPP chat implementation using Asmack
 * 
 * @author		Steven Jalabert <steven.jalabert@gmail.com>
 * @version		rev16
 * @since		2014-06-18
 */
package fr.jbteam.jabboid.ui;

import java.util.ArrayList;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.filter.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.util.StringUtils;

import fr.jbteam.jabboid.*;
import fr.jbteam.jabboid.asmack.XmppSettings;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;


public class ChatClient extends ActionBarActivity 
{
	   
    private ArrayList<String> messages = new ArrayList<String>();
    private Handler handler = new Handler();
    private XmppSettings settings;
    private EditText recipient;
    private EditText text;
    private ListView list;
    private XMPPConnection connection;
    private String keepUserid;
    
    public void setKeepUserid(String keepUserid) {
		this.keepUserid = keepUserid;
	}

	public void setKeepPassword(String keepPassword) {
		this.keepPassword = keepPassword;
	}

	private String keepPassword;
   
    public String getKeepUserid() {
		return keepUserid;
	}

	public String getKeepPassword() {
		return keepPassword;
	}
	
	public static String filename = "JabboidSPfile";
    SharedPreferences SP;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        String contactID = "";
        Bundle extras = getIntent().getExtras();  
        if(extras != null)
        {
        	contactID = extras.getString("contactID");
        }
        if(contactID != null)
        {
        	recipient = (EditText) this.findViewById(R.id.recipient);
        	recipient.setText(contactID);
        }
        else
        {
        	recipient = (EditText) this.findViewById(R.id.recipient);
        }
        SP = getSharedPreferences(filename, 0);
        String getUserID = SP.getString("keyUserID", null);
        String getPassword = SP.getString("keyPassword", null);
        if(getUserID != null || getPassword != null)
        {
        	this.keepUserid = getUserID;
        	this.keepPassword = getPassword;
        }
        text = (EditText) this.findViewById(R.id.message);
        list = (ListView) this.findViewById(R.id.thread);
        setListAdapter();
       
        //Window for getting settings
        settings = new XmppSettings(this);
       
        //Listener for chat message
        ImageButton send = (ImageButton) this.findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() 
        {
            @Override
            public void onClick(View v) 
            {
            	if (connection!=null) 
            	{
	                String to = recipient.getText().toString();
	                String text1 = text.getText().toString();
	               
	                Message msg = new Message(to, Message.Type.chat);
	                msg.setBody(text1);
	                try 
	                {
						connection.sendPacket(msg);
						text.getText().clear();
					} 
	                catch (Exception e) 
	                {
						// TODO Auto-generated catch block
						e.printStackTrace();
						raiseToast(R.string.unestablishedConnection);
					}
	                messages.add(connection.getUser() + ":");
	                messages.add(text1);
	                setListAdapter();        
            	}
            	else
            		raiseToast(R.string.unestablishedConnection);
            }
        });
    }
   
    //Called by settings when connection is established
   
    public void setConnection (XMPPConnection connection) 
    {
        this.connection = connection;
        if (connection != null) 
        {
            //Packet listener to get messages sent to logged in user
            PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
            connection.addPacketListener(new PacketListener() 
            {
                public void processPacket(Packet packet) 
                {
                    Message message = (Message) packet;
                    if (message.getBody() != null) 
                    {
                        String fromName = StringUtils.parseBareAddress(message.getFrom());
                        messages.add(fromName + ":");
                        messages.add(message.getBody());
                        handler.post(new Runnable()
                        {
                            public void run() 
                            {
                                setListAdapter();
                            }
                        });
                    }
                }
            }, filter);
        }
    }
   
    private void setListAdapter() 
    {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
            (this, R.layout.multi_line_list_item, messages);
        list.setAdapter(adapter);
    }
   
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        return true;
    }
   
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        switch(item.getItemId()) 
        {
        case R.id.action_online:
            try 
            {
            	// Set the status to available
                Presence presence = new Presence(Presence.Type.available);
                presence.setMode(Mode.available);
				connection.sendPacket(presence);
				raiseToast(R.string.presence_online);
			} 
            catch (Exception e) 
            {
				// TODO Auto-generated catch block
				e.printStackTrace();
				raiseToast(R.string.errorSettingPresence);
			}
        	return true;
        case R.id.action_away:
            try 
            {
            	// Set the status to away
                Presence presence1 = new Presence(Presence.Type.available);
                presence1.setMode(Mode.away);
				connection.sendPacket(presence1);
				raiseToast(R.string.presence_away);
			} 
            catch (Exception e) 
            {
				// TODO Auto-generated catch block
				e.printStackTrace();
				raiseToast(R.string.errorSettingPresence);
			}
        	return true;
        case R.id.action_occupied:
            try 
            {
            	// Set the status to Occupied
                Presence presence2 = new Presence(Presence.Type.available);
                presence2.setMode(Mode.dnd);
				connection.sendPacket(presence2);
				raiseToast(R.string.presence_dnd);
			}
            catch (Exception e) 
            {
				// TODO Auto-generated catch block
				e.printStackTrace();
				raiseToast(R.string.errorSettingPresence);
			}
        	return true;
        case R.id.action_settings:
            settings.show();
            return true;
        case R.id.quit:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    public void raiseToast(int message)
    {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}
    
    public void onStop()
    {
    	super.onStop();
    	if(this.connection != null)
    	{
	    	try 
	    	{
				this.connection.disconnect();
			} 
	    	catch (NotConnectedException e) 
	    	{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	SharedPreferences.Editor editit = SP.edit();
        editit.putString("keyUserID", this.keepUserid);
        editit.putString("keyPassword", this.keepPassword);
        editit.commit();
    }
}