package com.shishkin.steganographie.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;

import org.apache.commons.io.FileUtils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.shishkin.steganographie.IEncryptor;
import com.shishkin.steganographie.Parameters;
import com.shishkin.steganographie.R;
import com.shishkin.steganographie.UnableToDecodeException;
import com.shishkin.steganographie.UnableToEncodeException;
import com.shishkin.steganographie.crypto.AESCrypto;
import com.shishkin.steganographie.crypto.ICrypto;
import com.shishkin.steganographie.gif.EncryptingFileParameters;
import com.shishkin.steganographie.gif.GIFEncryptorByLSBMethod;
import com.shishkin.steganographie.gif.GIFEncryptorByPaletteExtensionMethod;
import com.shishkin.steganographie.zip.ICompressor;
import com.shishkin.steganographie.zip.StringCompressor;

/**
 * Starting activity of the application displays the basic data.
 * 
 * @author e.shishkin
 *
 */
public class MainActivity extends SherlockActivity 
		implements CreateNdefMessageCallback, OnNdefPushCompleteCallback {
	private static final String LOG_TAG = MainActivity.class.getSimpleName();
	
	private static final int SELECT_IMAGE = 0;
	private static final int MESSAGE_SENT = 1;
	
	private ImageView imageView;
	private TextView textView;
	
	private File image;
	
	private NfcAdapter mNfcAdapter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.v(LOG_TAG, "onCreate() called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        imageView = (ImageView) findViewById(android.R.id.icon);
        textView = (TextView) findViewById(android.R.id.text1);
        
		// Check for available NFC Adapter
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter == null) {
			Toast.makeText(this, "NFC is not available on this device", Toast.LENGTH_LONG).show();
		}
		// Register callback to set NDEF message
		mNfcAdapter.setNdefPushMessageCallback(this, this);
		// Register callback to listen for message-sent success
		mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
    }
    
    /**
     * Implementation for the CreateNdefMessageCallback interface
     */
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
    	Log.v(LOG_TAG, "createNdefMessage() called");
    	NdefMessage msg = null;
    	try {
	        msg = new NdefMessage(
	                new NdefRecord[] { createMimeRecord(
	                        "application/com.shishkin.steganographie", FileUtils.readFileToByteArray(image))
	         /**
	          * The Android Application Record (AAR) is commented out. When a device
	          * receives a push with an AAR in it, the application specified in the AAR
	          * is guaranteed to run. The AAR overrides the tag dispatch system.
	          * You can add it back in to guarantee that this
	          * activity starts when receiving a beamed message. For now, this code
	          * uses the tag dispatch system.
	          */
	          // ,NdefRecord.createApplicationRecord("com.shishkin.steganographie")
	        });
    	} catch (IOException e) {
			e.printStackTrace();
		}
        return msg;
    }

    /**
     * Implementation for the OnNdefPushCompleteCallback interface
     */
    @Override
    public void onNdefPushComplete(NfcEvent arg0) {
    	Log.v(LOG_TAG, "onNdefPushComplete() called");
        // A handler is needed to send messages to the activity when this
        // callback occurs, because it happens from a binder thread
        mHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
    }

    /** This handler receives a message from onNdefPushComplete */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_SENT:
                Toast.makeText(getApplicationContext(), "Message sent!", Toast.LENGTH_LONG).show();
                break;
            }
        }
    };
    
    @Override
    public void onResume() {
    	Log.v(LOG_TAG, "onResume() called");
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
    	Log.v(LOG_TAG, "onNewIntent() called");
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    void processIntent(Intent intent) {
    	Log.v(LOG_TAG, "processIntent() called");
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        byte[] bytes = msg.getRecords()[0].getPayload();
        //write the bytes in file
        try {
        	image = new File(Environment.getExternalStorageDirectory() + "/Steganography/" + "foto.gif");
			if (!image.getParentFile().exists()) {
				image.getParentFile().mkdirs();
			}
	        FileOutputStream fos = new FileOutputStream(image);
	        try {
	        	fos.write(bytes);
	        } finally {
	        	fos.close();
	        }
	        imageView.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
        } catch (IOException e) {
			e.printStackTrace();
		}
    }

    /**
     * Creates a custom MIME type encapsulated in an NDEF record
     *
     * @param mimeType
     */
    public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
    	Log.v(LOG_TAG, "createMimeRecord() called");
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
        NdefRecord mimeRecord = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
        return mimeRecord;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	Log.v(LOG_TAG, "onCreateOptionsMenu() called");
    	menu.add(Menu.NONE, R.id.menu_select_image, Menu.NONE, R.string.menu_select_image)
		    .setIcon(R.drawable.ic_action_gallery)
		    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(Menu.NONE, R.id.menu_encrypt, Menu.NONE, R.string.menu_encrypt)
		    .setIcon(R.drawable.ic_action_encrypt)
		    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	    menu.add(Menu.NONE, R.id.menu_decrypt, Menu.NONE, R.string.menu_decrypt)
		    .setIcon(R.drawable.ic_action_decrypt)
		    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	    menu.add(Menu.NONE, R.id.menu_preferences, Menu.NONE, R.string.menu_preferences)
		    .setIcon(R.drawable.ic_action_decrypt)
		    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
	    return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.v(LOG_TAG, "onOptionsItemSelected() called");
	    switch (item.getItemId()) {
	        case R.id.menu_select_image:
	        	selectImage();
	            break;
	        case R.id.menu_encrypt:
	        	encryptImage();
	        	break;
	        case R.id.menu_decrypt:
	        	decryptImage();
	        	break;
	        case R.id.menu_preferences:
	        	invokePreferencesActivity();
	        	break;
	            
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	    return true;
	}
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.v(LOG_TAG, "onActivityResult() called");
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK) {
			Uri selectedImageUri = data.getData();
			String selectedImagePath = null;
			try {
				//MEDIA GALLERY
				selectedImagePath = getPath(selectedImageUri);
			} catch (Exception e) {
				//OI FILE Manager
				selectedImagePath = selectedImageUri.getPath();
			}
			Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
			imageView.setImageBitmap(bitmap);
			image = new File(selectedImagePath);
			updatePossibleTextLength();
		}
	}
	
	/**
	 * Select Image from SD card
	 * (http://stackoverflow.com/questions/2507898/how-to-pick-a-image-from-gallery-sd-card-for-my-app-in-android)
	 */
	private void selectImage() {
		Log.v(LOG_TAG, "selectImage() called");
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, SELECT_IMAGE);
	}
	
	/**
	 * Convert URI to image path.
	 * 
	 * @param uri
	 * @return image path
	 */
	private String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
	/**
	 * Update Possible Text Length. Called when changing graphics container.
	 */
	@SuppressWarnings("static-access")
	private void updatePossibleTextLength() {
		Log.v(LOG_TAG, "updatePossibleTextLength() called");
		EncryptingFileParameters params = null;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		try {
			if (preferences.getString("encryptionMethodPref", "lsb").equals("lsb")) {
				params = new GIFEncryptorByLSBMethod().getEncryptingFileParameters(image);
			} else {
				params = new GIFEncryptorByPaletteExtensionMethod().getEncryptingFileParameters(image);
			}
			textView.setText(String.format("Possible message length: %d", params.getPossibleTextLength()));
			textView.setVisibility(View.VISIBLE);
		} catch (Exception e) {
			Log.w(LOG_TAG, e);
			textView.setVisibility(View.INVISIBLE);
		}
	}
	
	/**
	 * Start Preference activity to display the settings.
	 */
	private void invokePreferencesActivity() {
		Log.v(LOG_TAG, "invokePreferencesActivity() called");
		startActivity(new Intent(this, PreferencesActivity.class));
	}
	
	/**
	 * Formation of steganographic messages using selected encryption method.
	 */
	private void encryptImage() {
		Log.v(LOG_TAG, "encryptImage() called");
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		IEncryptor encryptor = preferences.getString("encryptionMethodPref", "lsb").equals("lsb") ? 
				new GIFEncryptorByLSBMethod() : new GIFEncryptorByPaletteExtensionMethod();
		ICompressor compressor = new StringCompressor();
		ICrypto crypto = new AESCrypto();
		// obtaining the original message
		String message = preferences.getString("messagePref", "");
		// obtaining the encryption key
		String key = preferences.getString("keyPref", "");
		try {
			// encrypt the original message
			byte[] encryptedData = crypto.encrypt(compressor.compress(message.getBytes()), key);
			File out = new File(Environment.getExternalStorageDirectory() + "/Steganography/" + image.getName());
			if (!out.getParentFile().exists()) {
				out.getParentFile().mkdirs();
			}
			// introduction of an encrypted message to the graphics container
			encryptor.encrypt(image, out, encryptedData);
			imageView.setImageBitmap(BitmapFactory.decodeFile(out.getPath()));
			image = out;
			Toast.makeText(this, Parameters.MESSAGE_ENCRYPTION_COMPLETED, Toast.LENGTH_LONG).show();
		} catch (UnableToEncodeException e) {
			Toast.makeText(this, Parameters.MESSAGE_ENCRYPTION_ERROR, Toast.LENGTH_LONG).show();
			Log.w(LOG_TAG, e);
		} catch (IOException e) {
			Toast.makeText(this, Parameters.MESSAGE_IO_ERROR, Toast.LENGTH_LONG).show();
			Log.w(LOG_TAG, e);
		} catch (NullPointerException e) {
			Toast.makeText(this, Parameters.MESSAGE_UNEXPECTED_ERROR, Toast.LENGTH_LONG).show();
			Log.w(LOG_TAG, e);
		} catch (GeneralSecurityException e) {
			Toast.makeText(this, Parameters.MESSAGE_ENCRYPTION_ERROR, Toast.LENGTH_LONG).show();
			Log.w(LOG_TAG, e);
		}
	}
	
	/**
	 * Decrypt steganographic messages using selected encryption 
	 * method and display the encrypted message.
	 */
	private void decryptImage() {
		Log.v(LOG_TAG, "decryptImage() called");
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		IEncryptor encryptor = preferences.getString("encryptionMethodPref", "lsb").equals("lsb") ? 
				new GIFEncryptorByLSBMethod() : new GIFEncryptorByPaletteExtensionMethod();
		ICompressor compressor = new StringCompressor();
		ICrypto crypto = new AESCrypto();
		// obtaining the encryption key
		String key = preferences.getString("keyPref", "");
		try {
			// extracting hidden data from the image
			byte[] data = encryptor.decrypt(image);
			// decoding of hidden data
			String decryptedMsg = new String(compressor.decompress(crypto.decrypt(data, key)));
			Toast.makeText(this, "decrypted message: " + decryptedMsg, Toast.LENGTH_LONG).show();
		} catch (UnableToDecodeException e) {
			Toast.makeText(this, Parameters.MESSAGE_DECRYPTION_ERROR, Toast.LENGTH_LONG).show();
			Log.w(LOG_TAG, e);
		} catch (IOException e) {
			Toast.makeText(this, Parameters.MESSAGE_IO_ERROR, Toast.LENGTH_LONG).show();
			Log.w(LOG_TAG, e);
		} catch (NullPointerException e) {
			Toast.makeText(this, Parameters.MESSAGE_UNEXPECTED_ERROR, Toast.LENGTH_LONG).show();
			Log.w(LOG_TAG, e);
		} catch (GeneralSecurityException e) {
			Toast.makeText(this, Parameters.MESSAGE_DECRYPTION_ERROR, Toast.LENGTH_LONG).show();
			Log.w(LOG_TAG, e);
		}
	}
	
}