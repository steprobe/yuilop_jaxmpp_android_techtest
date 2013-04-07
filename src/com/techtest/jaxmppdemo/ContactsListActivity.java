package com.techtest.jaxmppdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * An activity representing a list of Contact. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link ContactsDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ContactsListFragment} and the item details (if present) is a
 * {@link ContactsDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link ContactsListFragment.Callbacks} interface to listen for item
 * selections.
 */
public class ContactsListActivity extends FragmentActivity implements
        ContactsListFragment.Callbacks {

    public static final String BUNDLE_SESSION_ID = "sessionid";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private int mSessionId;
    private ContactsListFragment mContactsListFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_list);

        mSessionId = getIntent().getExtras().getInt(BUNDLE_SESSION_ID);

        if (findViewById(R.id.contacts_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            mContactsListFrag = (ContactsListFragment) getSupportFragmentManager().
                    findFragmentById(R.id.contacts_list));

            mContactsListFrag.setActivateOnItemClick(true);
            mContactsListFrag.setSessionId(mSessionId);
        }
    }

    /**
     * Callback method from {@link ContactsListFragment.Callbacks} indicating
     * that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ContactsDetailFragment.ARG_ITEM_ID, id);
            ContactsDetailFragment fragment = new ContactsDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contacts_detail_container, fragment).commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, ContactsDetailActivity.class);
            detailIntent.putExtra(ContactsDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }
}
