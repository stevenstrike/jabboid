/**
 * Dialog type class permitting to setup the connection data/settings
 * 
 * @author		Steven Jalabert <steven.jalabert@gmail.com>
 * @version		rev12
 * @since		2014-06-18
 */
package fr.jbteam.jabboid.asmack;

import fr.jbteam.jabboid.R;
import fr.jbteam.jabboid.ui.ChatClient;
import android.app.Dialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
//settings get input and then connection is established

public class XmppSettings extends Dialog implements android.view.View.OnClickListener  
{
    private ChatClient chatClient;
   
    public XmppSettings(ChatClient chatClient)
    {
        super(chatClient);
        this.chatClient = chatClient;
    }
   
    protected void onStart() 
    {
        super.onStart();
        setContentView(R.layout.settings);
        setTitle(R.string.connectionSettings);
        if(chatClient.getKeepUserid() != null || chatClient.getKeepPassword() != null)
        {
        	this.setText(R.id.userid, chatClient.getKeepUserid());
        	this.setText(R.id.password, chatClient.getKeepPassword());
        }
        Button ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(this);
    }
   
    public void onClick(View v) 
    {
    	new XmppConnection(this.chatClient, this).execute();
    	chatClient.setKeepUserid(this.getText(R.id.userid));
    	chatClient.setKeepPassword(this.getText(R.id.password));
        dismiss();
    }
   
    public String getText(int id) 
    {
        EditText widget = (EditText) this.findViewById(id);
        return widget.getText().toString();
    }
    
    private void setText(int id, String text) 
    {
        EditText widget = (EditText) this.findViewById(id);
        widget.setText(text);
    }
}
