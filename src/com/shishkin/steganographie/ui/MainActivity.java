package com.shishkin.steganographie.ui;

import java.io.File;
import java.io.IOException;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.shishkin.steganographie.Encryptor;
import com.shishkin.steganographie.Parameters;
import com.shishkin.steganographie.R;
import com.shishkin.steganographie.UnableToDecodeException;
import com.shishkin.steganographie.UnableToEncodeException;
import com.shishkin.steganographie.gif.GIFEncryptorByLSBMethod;
import com.shishkin.steganographie.gif.GIFEncryptorByPaletteExtensionMethod;

/**
 * 
 * @author e.shishkin
 *
 */
public class MainActivity extends SherlockActivity {
	
	private static final int SELECT_IMAGE = 1;
	
	private ImageView imageView;
	private File image;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        imageView = (ImageView) findViewById(android.R.id.icon);
        imageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				selectImage();
			}
		});
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
	    switch (item.getItemId()) {
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
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK) {
			Uri selectedImageUri = data.getData();
			String selectedImagePath = getPath(selectedImageUri);
			Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
			imageView.setImageBitmap(bitmap);
			image = new File(selectedImagePath);
		}
	}
	
	private void selectImage() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, SELECT_IMAGE);
	}
	
	private String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		@SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
	
	private void invokePreferencesActivity() {
		startActivity(new Intent(this, PreferencesActivity.class));
	}
	
	private void encryptImage() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Encryptor encryptor = preferences.getString("encryptionMethodPref", "lsb").equals("lsb") ? 
				new GIFEncryptorByLSBMethod() : new GIFEncryptorByPaletteExtensionMethod();
		String message = preferences.getString("messagePref", "");
		try {
			File out = new File(Environment.getExternalStorageDirectory() + "/Steganography/" + image.getName());
			if (!out.getParentFile().exists()) {
				out.getParentFile().mkdirs();
			}
			encryptor.encrypt(image, out, message);
			imageView.setImageBitmap(BitmapFactory.decodeFile(out.getPath()));
			image = out;
			Toast.makeText(this, Parameters.MESSAGE_ENCRYPTION_COMPLETED, Toast.LENGTH_LONG).show();
		} catch (UnableToEncodeException e) {
			Toast.makeText(this, Parameters.MESSAGE_ENCRYPTION_ERROR, Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			Toast.makeText(this, Parameters.MESSAGE_IO_ERROR, Toast.LENGTH_LONG).show();
		} catch (NullPointerException e) {
			Toast.makeText(this, Parameters.MESSAGE_UNEXPECTED_ERROR, Toast.LENGTH_LONG).show();
		}
	}
	
	private void decryptImage() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Encryptor encryptor = preferences.getString("encryptionMethodPref", "lsb").equals("lsb") ? 
				new GIFEncryptorByLSBMethod() : new GIFEncryptorByPaletteExtensionMethod();
		try {
			String message = encryptor.decrypt(image);
			Toast.makeText(this, "decrypted message: " + message, Toast.LENGTH_LONG).show();
		} catch (UnableToDecodeException e) {
			Toast.makeText(this, Parameters.MESSAGE_DECRYPTION_ERROR, Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			Toast.makeText(this, Parameters.MESSAGE_IO_ERROR, Toast.LENGTH_LONG).show();
		} catch (NullPointerException e) {
			Toast.makeText(this, Parameters.MESSAGE_UNEXPECTED_ERROR, Toast.LENGTH_LONG).show();
		}
	}
	
}