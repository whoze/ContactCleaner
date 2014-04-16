package de.martinboeschen.contactcleaner;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class ContactsActivity extends Activity {
	
	boolean DeveloperVersion = false;
	
	private ArrayList<Item> phoneNumbers;
	ArrayAdapter<Item> arrayAdapter;
	ListView lv;

	/*
	 * Returns the formatted number, if possible, the empty string otherwise
	 */
	private String formatNumber(String phonenumber) {
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		String lang = Locale.getDefault().getCountry(); 
		System.out.println(lang);
		try {
			PhoneNumber numberProto = phoneUtil.parse(phonenumber, lang);
			return phoneUtil.format(numberProto,
					PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
		} catch (NumberParseException e) {
			return "";
		}

	}
	
	/**
	 * Loads the contacts into the phoneNumbers List. 
	 */
	private void loadContacts() {
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		String[] projection = new String[] {
				ContactsContract.CommonDataKinds.Phone._ID,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.NUMBER };

		Cursor people = getContentResolver().query(uri, projection, null, null,
				null);

		int indexName = people
				.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
		int indexNumber = people
				.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
		int indexID = people
				.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID);

		while (people.moveToNext()){
			Item it = new Item();
			it.name = people.getString(indexName);
			it.number = people.getString(indexNumber);
			it.corrected_number = formatNumber(it.number);
			it.id = people.getLong(indexID);
			Log.v("",it.name);
			// only place item where we can help
			if (!it.number.equals("") && !it.corrected_number.equals("")
					&& !it.number.equals(it.corrected_number))
				phoneNumbers.add(it);
		}
		people.close();

	}

	// set (or unset) all the checkboxes
	private void setCheckboxes(boolean activated) {
		System.out.println(lv.getCount());
		for (Item i : phoneNumbers) {
			i.checked = activated;
		}
		arrayAdapter.notifyDataSetChanged();

	}

	@Override
	protected void onResume() {
		super.onResume();
		makeEverythingNew();

	}
	
	private void makeEverythingNew(){
		setContentView(R.layout.contacts_activity);
		lv = (ListView) findViewById(R.id.listView1);
		phoneNumbers = new ArrayList<Item>();

		loadContacts();
		LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		TextView footer = (TextView) vi.inflate(R.layout.footer, null);

		if (phoneNumbers.isEmpty())
			lv.addFooterView(footer);

		arrayAdapter = new MyCustomAdapter(this, R.layout.my_list_item,
				R.id.textView_name, phoneNumbers);

		lv.setAdapter(arrayAdapter);

		Button b_sel = (Button) findViewById(R.id.button_select);
		Button b_desel = (Button) findViewById(R.id.button_deselect);
		Button b_fix = (Button) findViewById(R.id.button_fix);
		b_sel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setCheckboxes(true);
				System.out.println("Check");

			}
		});

		b_desel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setCheckboxes(false);
				System.out.println("Uncheck");

			}
		});

		b_fix.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int zaehler = 0;
				for (Item item : phoneNumbers) {
					if (item.checked) {
						Log.v("", item.name + " will be updated");
						ContentValues cv = new ContentValues();
						cv.put(ContactsContract.CommonDataKinds.Phone.NUMBER,
								item.corrected_number);
						int x = getContentResolver()
								.update(ContactsContract.Data.CONTENT_URI,
										cv,
										ContactsContract.CommonDataKinds.Phone._ID
												+ " = ?",
										new String[] { item.id + "" });

						Log.v("", "Update was successful: " + x);
						zaehler += x;
					}
				}

				if (zaehler > 0) {
					Toast.makeText(getApplicationContext(),
							zaehler + " contacts sucessfully updated",
							Toast.LENGTH_SHORT).show();
					makeEverythingNew();
				}

			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(DeveloperVersion){
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
		}
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.test1:
	            testcase1();
				makeEverythingNew();
	            return true;
	        case R.id.test2:
	        	testcase2();
				makeEverythingNew();
	            return true;
	        case R.id.test3:
	        	testcase3();
				makeEverythingNew();
				return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	private void testcase1() {
		deleteAllContacts();
		Log.v("","Prepared Testcase 1: All contacts deleted");
		
	}
	
	private void testcase2() {
		deleteAllContacts();
		createContact("Martin", "44112345");
		createContact("Martin", "44199887");
		Log.v("","Prepared Testcase 2: Two broken contacts");
		
	}
	
	private void testcase3() {
		System.out.println("test");
		deleteAllContacts();
		createContact("Martin", "+49 441 12345");
		createContact("Martin", "44112345");
		Log.v("","Prepared Testcase 3: One broken contact, one correct contact");
		
	}
	
	private void deleteAllContacts(){
		ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
            System.out.println(contentResolver.delete(uri, null, null));
        }
	}
	
	private void createContact(String displayName, String phonenumber){
		 String name = displayName + Math.random();

		 ArrayList < ContentProviderOperation > ops = new ArrayList < ContentProviderOperation > ();

		 ops.add(ContentProviderOperation.newInsert(
		 ContactsContract.RawContacts.CONTENT_URI)
		     .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
		     .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
		     .build());

		 //------------------------------------------------------ Names
		 if (name != null) {
		     ops.add(ContentProviderOperation.newInsert(
		     ContactsContract.Data.CONTENT_URI)
		         .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
		         .withValue(ContactsContract.Data.MIMETYPE,
		     ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
		         .withValue(
		     ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
		     name).build());
		 }


		 //------------------------------------------------------ Home Numbers
		 if (phonenumber != null) {
		     ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
		         .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
		         .withValue(ContactsContract.Data.MIMETYPE,
		     ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
		         .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phonenumber)
		         .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
		     ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
		         .build());
		 }

	

		 // Asking the Contact provider to create a new contact                 
		 try {
		     getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
		 } catch (Exception e) {
		     e.printStackTrace();
		     Toast.makeText(getApplicationContext(), "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
		 } 
		
	}

	public class Item {
		String name;
		boolean checked;
		String number;
		String corrected_number;
		long id;
	}

	private class MyCustomAdapter extends ArrayAdapter<Item> {

		public MyCustomAdapter(Context context, int resource,
				int textViewResourceId, ArrayList<Item> countryList) {
			super(context, resource, textViewResourceId, countryList);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final Item x = getItem(position);
			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.my_list_item, parent, false);
			CheckBox cb = (CheckBox) convertView.findViewById(R.id.checkBox1);
			cb.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					x.checked = ((CheckBox) v).isChecked();
				}
			});
			cb.setChecked(x.checked);
			TextView tv = (TextView) convertView
					.findViewById(R.id.textView_name);
			tv.setText(x.name);

			TextView tv2 = (TextView) convertView
					.findViewById(R.id.textView_number);
			tv2.setText(x.number);

			TextView tv3 = (TextView) convertView
					.findViewById(R.id.textView_correctednumber);
			tv3.setText(x.corrected_number);

			return convertView;

		}

	}

}
