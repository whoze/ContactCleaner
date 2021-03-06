package de.martinboeschen.phoneFormatter;

/* taken form: http://www.techrepublic.com/blog/software-engineer/a-reusable-about-dialog-for-your-android-apps/#. */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

<<<<<<< HEAD:phoneFormatter/src/de/martinboeschen/phoneFormatter/AboutActivity.java
=======
import de.martinboeschen.contactcleaner.R;
>>>>>>> 3f271e988c39a939502dc783d3bf3130139125b7:phoneFormatter/src/de/martinboeschen/phoneFormatter/AboutActivity.java
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.widget.TextView;

public class AboutActivity extends Dialog {
	private static Context mContext = null;

	public AboutActivity(Context context) {
		super(context);

		mContext = context;
	}

	/**
	 * 
	 * 
	 * Standard Android on create method that gets called when the activity
	 * initialized.
	 */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		setContentView(R.layout.activity_about);
		TextView tv = (TextView) findViewById(R.id.info_text);

		tv.setText(Html.fromHtml(readRawTextFile(R.raw.info)));

		Linkify.addLinks(tv, Linkify.ALL);

	}

	public static String readRawTextFile(int id) {

		InputStream inputStream = mContext.getResources().openRawResource(id);

		InputStreamReader in = new InputStreamReader(inputStream);
		BufferedReader buf = new BufferedReader(in);

		String line;

		StringBuilder text = new StringBuilder();
		try {

			while ((line = buf.readLine()) != null)
				text.append(line);
		} catch (IOException e) {
			return null;

		}

		return text.toString();

	}

}