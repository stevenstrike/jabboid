/**
 * Class permitting the ChatClient Activity to connect to XMPP
 * 
 * @author		Steven Jalabert <steven.jalabert@gmail.com>
 * @version		rev10
 * @since		2014-06-18
 */
package fr.jbteam.jabboid.asmack;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.apache.harmony.javax.security.sasl.SaslException;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import fr.jbteam.jabboid.R;
import fr.jbteam.jabboid.ui.ChatClient;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class XmppConnection extends AsyncTask<String, Void, String>
{
	private XMPPConnection connection;
	private ChatClient chatClient;
	private XmppSettings xmppSettings;

	
	public XmppConnection(ChatClient chatClient, XmppSettings xmppSettings)
	{
        this.chatClient = chatClient;
        this.xmppSettings = xmppSettings;
    }

	
	@Override
	protected String doInBackground(String... arg0) 
	{
		SmackAndroid.init(chatClient);

		String host = xmppSettings.getText(R.id.host);
        String port = xmppSettings.getText(R.id.port);
        String service = xmppSettings.getText(R.id.service);
        String username = xmppSettings.getText(R.id.userid);
        String password = xmppSettings.getText(R.id.password);
		
		ConnectionConfiguration connConfig =
                new ConnectionConfiguration(host, Integer.parseInt(port), service);
		connConfig.setCompressionEnabled(true);
		connConfig.setSecurityMode(SecurityMode.disabled);
		try 
		{
			connConfig.setCustomSSLContext(ContextService.createContext());
		}
		catch (KeyManagementException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (KeyStoreException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (NoSuchAlgorithmException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
        this.connection = new XMPPTCPConnection(connConfig);     
        
        try 
        {
            try 
            {
				this.connection.connect();
			}
            catch (SmackException e) 
            {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            catch (IOException e) 
            {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            Log.i("XMPPClient", "[SettingsDialog] Connected to " + this.connection.getHost());
        }
        catch (XMPPException ex) 
        {
            Log.e("XMPPClient", "[SettingsDialog] Failed to connect to " + this.connection.getHost());
            chatClient.setConnection(null);
        }
        
        try
        {
            this.connection.login(username, password);
            Log.i("XMPPClient", "Logged in as " + this.connection.getUser());

            // Set the status to available
            Presence presence = new Presence(Presence.Type.available);
            this.connection.sendPacket(presence);
            chatClient.setConnection(this.connection);
        }
        catch (XMPPException ex) 
        {
            Log.e("XMPPClient", "[SettingsDialog] Failed to log in as " + username);
            chatClient.setConnection(null);
        }
        catch (SaslException e) 
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
			chatClient.setConnection(null);
		}
        catch (SmackException e) 
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
			chatClient.setConnection(null);
		}
        catch (IOException e) 
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
			chatClient.setConnection(null);
		}
		return null;
	}
}
