package cz.incad.utils.ssl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class LDAPSSLSocketFactory extends SSLSocketFactory {
	public static final String KEYSTORE_FILE = "com.amaio.ldap.keystore.file";
	public static final String KEYSTORE_PASS = "com.amaio.ldap.keystore.pass";
	
	private static final LDAPSSLSocketFactory INSTANCE = new LDAPSSLSocketFactory();
	static {
		installDefaultKeystore();
	}
	
	private final MyTrustManager manager;
	private final SSLSocketFactory factory;

	private LDAPSSLSocketFactory() {
		try {
			this.manager = new MyTrustManager();
			SSLContext sc = SSLContext.getInstance("SSL");
			//SSLContext sc = SSLContext.getInstance("TLS");
			//sc.init(null, new TrustManager[]{manager}, new java.security.SecureRandom());
			sc.init( null, new TrustManager[]{manager}, null );
			this.factory = sc.getSocketFactory();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (KeyManagementException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static LDAPSSLSocketFactory getDefault() {
		return INSTANCE;
	}
	
	public static void installKeyStore( KeyStore keyStore ) {
		INSTANCE.manager.keyStore = keyStore;
	}
	
	public static void installKeyStore( InputStream stream, char[] password ) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		System.err.println("Installing keystore");
		KeyStore keyStore = KeyStore.getInstance( KeyStore.getDefaultType() );
		keyStore.load( stream, password );
		installKeyStore( keyStore );
	}
	
	public static void installDefaultKeystore() {
		String keyStoreFile = System.getProperty( KEYSTORE_FILE );
		String keyStorePass = System.getProperty( KEYSTORE_PASS );
		if( keyStoreFile!=null ) {
			char[] password = keyStorePass==null ? null : keyStorePass.toCharArray();
			InputStream stream = null;
			try {
				stream = new FileInputStream( keyStoreFile );
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			try {
				installKeyStore( stream, password );
			} catch (KeyStoreException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (CertificateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println(LDAPSSLSocketFactory.class.getName()+": KeyStore not installed. SERVER NOT VERIFIED");
		}
	}

	@Override
	public String[] getDefaultCipherSuites() {
		//log.fine("getDefaultCipherSuites()");
		return factory.getDefaultCipherSuites();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		//log.fine("getSupportedCipherSuites()");
		return factory.getSupportedCipherSuites();
	}

	@Override
	public Socket createSocket(Socket socket, String host, int port, boolean autoclose) throws IOException {
		//log.fine("1> createSocket( socket="+socket+", host="+host+", port="+port+", autoclose="+autoclose+" )");
		return factory.createSocket( socket, host, port, autoclose );
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
		//log.fine("2> createSocket(host="+host+", port="+port+")");
		return factory.createSocket( host, port );
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
		//log.fine("3> createSocket(host="+host+", port="+port+", localHost="+localHost+", localPort="+localPort+")");
		return factory.createSocket( host, port, localHost, localPort );
	}

	@Override
	public Socket createSocket(InetAddress host, int port) throws IOException {
		//log.fine("4> createSocket(host="+host+", port="+port+")");
		return factory.createSocket(host, port);
	}

	@Override
	public Socket createSocket( InetAddress  host, int port, InetAddress localHost, int localPort ) throws IOException {
		//log.fine("5> createSocket(host="+host+", port="+port+", localHost="+localHost+", localPort="+localPort+")");
		return factory.createSocket( host, port, localHost, localPort);
	}
	
	
	public static void save( KeyStore keyStore, String filename, char[] password ) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		FileOutputStream os = new FileOutputStream( filename );
        keyStore.store( os, password);
        os.close();
	}
	
	public static KeyStore load( String filename, char[] password ) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		KeyStore keyStore = KeyStore.getInstance( KeyStore.getDefaultType() );
		FileInputStream is = null;
		try {
			is = new FileInputStream( filename );
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		keyStore.load( is, password );
		return keyStore;
	}	

	
	
	public static void insertCertificate( KeyStore keyStore, X509Certificate[] chain ) throws KeyStoreException {
        for (int i = 0; i < chain.length; i++)  {
            keyStore.setCertificateEntry(chain[i].getIssuerDN().toString(), chain[i]);
        }
	}
	
	
	private static class MyTrustManager implements X509TrustManager {
		
		private KeyStore keyStore = null;
		
		private KeyStore getKeyStore() {
			return keyStore;
		}
	
		
		private boolean isChainTrusted(X509Certificate[] chain) {
			KeyStore keyStore = getKeyStore();
			if( keyStore==null ) {
				return true;
			}
			
			boolean trusted = false;
			try {
				// Start with the root and see if it is in the Keystore.
				// The root is at the end of the chain.
				for (int i = chain.length - 1; i >= 0; i--) {
					if (keyStore.getCertificateAlias(chain[i]) != null) {
						trusted = true;
						break;
					}
				}
			} catch (Exception e) {
				System.out.println("isChainTrusted Exception: " + e.toString());
				trusted = false;
			}
			return trusted;
		}

		public X509Certificate[] getAcceptedIssuers() {
			KeyStore keyStore = getKeyStore();
			if( keyStore==null ) {
				return null;
			}
			
			X509Certificate[] X509Certs = null;
			try {
				int numberOfEntry = keyStore.size();
				if (numberOfEntry > 0) {
					X509Certs = new X509Certificate[numberOfEntry];
					Enumeration aliases = keyStore.aliases();
					int i = 0;
					while (aliases.hasMoreElements()) {
						X509Certs[i] = (X509Certificate) keyStore
								.getCertificate((String) aliases.nextElement());
						i++;
					}

				}
			} catch (Exception e) {
				System.out.println("getAcceptedIssuers Exception: " + e.toString());
				X509Certs = null;
			}
			return X509Certs;
		}

		public void checkClientTrusted( X509Certificate[] chain, String authType ) throws CertificateException {
			if ( !isChainTrusted(chain) ) {
				throw new CertificateException();
			}
		}

		public void checkServerTrusted( X509Certificate[] chain, String authType ) throws CertificateException {
			if ( !isChainTrusted(chain) ) {
				throw new CertificateException();
			}
		}
		
	}

}
