package com.techtest.jaxmppdemo;

import java.util.List;

import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.techtest.jaxmppdemo.GtalkService.OnMessageSent;

public class ContactsListActivity extends ServiceActivity {

    private static final String TEST_MESSAGE = "Greetings from the Jaxmpp Demo-Bot";

    private final String LOG_TAG = getClass().getSimpleName();

    public static final String BUNDLE_SESSION_ID = "sessionid";

    private int mSessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_list);

        mSessionId = getIntent().getExtras().getInt(BUNDLE_SESSION_ID);
    }

    @Override
    protected void onServiceConnected() {

        ListView lv = (ListView) findViewById(R.id.contacts_list);

        final List<RosterItem> contacts = mBinder.getContacts(mSessionId);
        lv.setAdapter(new ContactsAdapter(this, android.R.layout.simple_list_item_1, contacts));

        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position,
                    long id) {

                RosterItem contact = contacts.get(position);

                Log.i(LOG_TAG, "Sending message to " + contact.getJid().getLocalpart());
                mBinder.sendMessage(TEST_MESSAGE, contact, mSessionId, new OnMessageSent() {

                    @Override
                    public void onMessageSent() {
                        String hdr = getResources().getString(R.string.message_sent);
                        Toast.makeText(ContactsListActivity.this, hdr, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onMessageFailed() {
                        String hdr = getResources().getString(R.string.message_error);
                        Toast.makeText(ContactsListActivity.this, hdr, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onServiceDisaconnected() {
        // TODO Auto-generated method stub

    }

    private class ContactsAdapter extends ArrayAdapter<RosterItem> {

        private final int mTextViewRes;
        private List<RosterItem> mContacts;

        public ContactsAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            mTextViewRes = textViewResourceId;
        }

        public ContactsAdapter(Context context, int textViewResourceId, List<RosterItem> contacts) {
            super(context, textViewResourceId, contacts);
            mContacts = contacts;
            mTextViewRes = textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;
            if (view == null) {

                LayoutInflater vi = LayoutInflater.from(getContext());
                view = vi.inflate(mTextViewRes, null);

            }

            final RosterItem contact = mContacts.get(position);

            TextView tv = (TextView)view;
            tv.setText(contact.getJid().getLocalpart());

            return view;

        }
    }
}
