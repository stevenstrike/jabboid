/**
 * Attempt to create a share context by the whole application (failed)
 * 
 * @author		Steven Jalabert <steven.jalabert@gmail.com>
 * @version		rev1
 * @since		2014-06-18
 */
package fr.jbteam.jabboid.asmack;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import android.annotation.SuppressLint;
import android.os.Build;

public class ContextService 
{
	 
    @SuppressLint("TrulyRandom") 
    public static SSLContext createContext() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException 
    {
        KeyStore trustStore;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) 
        {
            trustStore = KeyStore.getInstance("AndroidCAStore");
        } 
        else 
        {
            trustStore = KeyStore.getInstance("BKS");
        }
 
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
        return sslContext;
    }
}