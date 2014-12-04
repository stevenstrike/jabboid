
/** 
 * Basic no frills app which integrates the ZBar barcode scanner with
 * the camera.
 * 
 * @author		Steven Jalabert <steven.jalabert@gmail.com>
 * @version		rev11
 * @since		2014-07-14
*/
package fr.jbteam.jabboid.ui;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONObject;

import fr.jbteam.jabboid.R;
import net.sourceforge.zbar.android.camerascan.CameraPreview;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.OperationApplicationException;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;
import android.widget.TextView;
import android.support.v7.app.ActionBarActivity;
/* Import ZBar Class files */
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import net.sourceforge.zbar.Config;

public class CameraActivity extends ActionBarActivity
{
    private static final int PROTOCOL_JABBER = 7;
	private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;

    TextView scanText;
    ImageButton scanButton;
    ImageButton addContactButton;

    ImageScanner scanner;

    private boolean barcodeScanned = false;
    private boolean previewing = true;
    private boolean isJSONJabboid = false;
    private boolean hasAddedContact = false;
    
    private String qrData = "";
    private String LastN = "";
    private String firstN = "";
    private String jabID ="";
    private String email = "";
    private String phoneNB = "";
    
    static {
        System.loadLibrary("iconv");
    } 

    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.qr_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        autoFocusHandler = new Handler();
        mCamera = getCameraInstance();

        /* Instance barcode scanner */
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);

        mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
        FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
        preview.addView(mPreview);

        scanText = (TextView)findViewById(R.id.scanText);

        scanButton = (ImageButton)findViewById(R.id.ScanButton);

        scanButton.setOnClickListener(new OnClickListener() 
        {
                public void onClick(View v) {
                    if (barcodeScanned) {
                        barcodeScanned = false;
                        scanText.setText(R.string.scanText);
                        mCamera.setPreviewCallback(previewCb);
                        mCamera.startPreview();
                        previewing = true;
                        mCamera.autoFocus(autoFocusCB);
                    }
                }
            });
        
        addContactButton = (ImageButton)findViewById(R.id.addContactButton);

        addContactButton.setOnClickListener(new OnClickListener() 
        {
                public void onClick(View v) 
                {
                	if(barcodeScanned && qrData != null)
                	{
                		try
                		{
                			JSONObject jObject = new JSONObject(qrData);
                			isJSONJabboid =	jObject.getBoolean("isJabboid");
                			if(isJSONJabboid)
                			{
                				LastN = jObject.getString("LastN");
                				firstN = jObject.getString("firstN");
                				jabID = jObject.getString("jabID");
                				email = jObject.getString("email");
                				phoneNB = jObject.getString("phoneNB");
                				hasAddedContact = doAddContact(LastN, firstN, jabID, email, phoneNB);
                				if(hasAddedContact)
                				{
                					//display toast in short period of time
                					Toast.makeText(getApplicationContext(), R.string.contactAdded, Toast.LENGTH_SHORT).show();
                				}
                				else
                				{
                					//display toast in short period of time
                					Toast.makeText(getApplicationContext(), R.string.contactNotAdded, Toast.LENGTH_SHORT).show();
                				}
                			}
                			else
                				Toast.makeText(getApplicationContext(), R.string.isNotJabboidJSON, Toast.LENGTH_SHORT).show();
                		}
                		catch (Exception e)
                		{
                			Toast.makeText(getApplicationContext(), R.string.isNotJSON, Toast.LENGTH_SHORT).show();
                		}
                	}
                }
        });
    }
    /* Triggered when pushing home button or simply leaving the activity without pushing back button */
    public void onPause() 
    {
        super.onPause();
        releaseCamera();
    }
    
    /* Triggered when going back to the application on this activity */
    public void onResume(){
        super.onResume();

        try {
            if(mCamera==null){

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            autoFocusHandler = new Handler();
            mCamera = getCameraInstance();
            this.getWindowManager().getDefaultDisplay().getRotation();

            scanner = new ImageScanner();
            scanner.setConfig(0, Config.X_DENSITY, 3);
            scanner.setConfig(0, Config.Y_DENSITY, 3);

            mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
            FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
            preview.addView(mPreview);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
        	e.printStackTrace();
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance()
    {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e){
        	e.printStackTrace();
        }
        return c;
    }

    private void releaseCamera() 
    {
        if (mCamera != null) {
            previewing = false;
            mCamera.setPreviewCallback(null);
            mPreview.getHolder().removeCallback(mPreview);
            mCamera.release();
            mCamera = null;
        }
    }

    private Runnable doAutoFocus = new Runnable() 
    {
            public void run() 
            {
                if (previewing)
                    mCamera.autoFocus(autoFocusCB);
            }
    };

    PreviewCallback previewCb = new PreviewCallback() 
    {
            public void onPreviewFrame(byte[] data, Camera camera) 
            {
                Camera.Parameters parameters = camera.getParameters();
                Size size = parameters.getPreviewSize();

                Image barcode = new Image(size.width, size.height, "Y800");
                barcode.setData(data);

                int result = scanner.scanImage(barcode);
                
                if (result != 0) 
                {
                    previewing = false;
                    mCamera.setPreviewCallback(null);
                    mCamera.stopPreview();
                    
                    SymbolSet syms = scanner.getResults();
                    for (Symbol sym : syms) 
                    {
                        scanText.setText(R.string.qr_code_found);
                        qrData = sym.getData();
                        barcodeScanned = true;
                    }
                }
            }
        };

    // Mimic continuous auto-focusing
    AutoFocusCallback autoFocusCB = new AutoFocusCallback() 
    {
            public void onAutoFocus(boolean success, Camera camera) 
            {
                autoFocusHandler.postDelayed(doAutoFocus, 3000);
            }
    };
    /* Add a contact on the contact list with the data parsed by the qr-code and the json */
    private boolean doAddContact(String LastN, String firstN, String jabID, String email, String phoneNB)
    {
    	ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        int rawContactInsertIndex = ops.size();
        
        ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_TYPE, null)
                .withValue(RawContacts.ACCOUNT_NAME, null).build());
        
        ops.add(ContentProviderOperation
                .newInsert(Data.CONTENT_URI)
                .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(StructuredName.DISPLAY_NAME, LastN + " " + firstN) // Name of the person
                .build());
        
        ops.add(ContentProviderOperation
                .newInsert(Data.CONTENT_URI)
                .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                .withValue(Phone.NUMBER, phoneNB) // Number of the person
                .withValue(Phone.TYPE, Phone.TYPE_MOBILE).build()); // Type of mobile number
        
        ops.add(ContentProviderOperation
        		.newInsert(Data.CONTENT_URI)
        		.withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
        		.withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
        		.withValue(Email.ADDRESS, email)
        		.withValue(Email.TYPE, Email.TYPE_HOME).build());
        
        ops.add(ContentProviderOperation
        		.newInsert(Data.CONTENT_URI)
        		.withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
        		.withValue(Data.MIMETYPE, Im.CONTENT_ITEM_TYPE)
        		.withValue(Im.PROTOCOL, PROTOCOL_JABBER)
        		.withValue(Im.DATA, jabID)
        		.withValue(Im.TYPE, Im.TYPE_HOME).build());
        
        try
        {
            ContentProviderResult[] res = getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            return true;
        }
        catch (RemoteException e)
        { 
            // error
        	e.printStackTrace();
        	return false;
        }
        catch (OperationApplicationException e) 
        {
            // error
        	e.printStackTrace();
        	return false;
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        	return false;
        }
    }
    
}
