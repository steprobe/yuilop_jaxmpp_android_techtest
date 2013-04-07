package com.techtest.jaxmppdemo;

import java.util.List;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.factory.UniversalFactory;
import tigase.jaxmpp.core.client.factory.UniversalFactory.FactorySpi;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule.PresenceEvent;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterStore;
import tigase.jaxmpp.j2se.Jaxmpp;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector.DnsResolver;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

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

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_list);

        if (findViewById(R.id.contacts_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((ContactsListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.contacts_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
        new TestAsync().execute();
    }

    private class TestAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            UniversalFactory.setSpi(DnsResolver.class.getName(), new FactorySpi() {

                @Override
                public AndroidDNSResolver create() {
                    return new AndroidDNSResolver();
                }
            });

            try {

               // InetAddress bob = new Inet
                //Socket sock = new Socket(dstAddress, 5222);

                final Jaxmpp contact = new Jaxmpp();

                contact.getModulesManager().getModule( PresenceModule.class ).
                    addListener( PresenceModule.ContactChangedPresence,
                            new Listener<PresenceModule.PresenceEvent>() {

                    @Override
                    public void handleEvent( PresenceEvent be ) throws JaxmppException {
                        System.out.println( String.format( "Presence received:\t %1$s is now %2$s (%3$s)", be.getJid(), be.getShow(), be.getStatus() != null ? be.getStatus() : "none" ) );
                    }
                } );

                contact.getProperties().setUserProperty(
                        SessionObject.DOMAIN_NAME, "talk.google.com");
                contact.getProperties().setUserProperty(
                        SessionObject.USER_BARE_JID, BareJID.bareJIDInstance("brian.mcdermott13@gmail.com" ) );
                contact.getProperties().setUserProperty(
                        SessionObject.PASSWORD, "drogheda82" );

                System.out.println( "Loging in..." );

                contact.login();
                RosterStore store = contact.getRoster();
                List<RosterItem> rosters = store.getAll();
                for(RosterItem item : rosters) {
                    System.out.println("Thing is : " + item.getName());
                }

                System.out.println( "Waiting for the presence for 10 minutes" );

                Thread.sleep( 10 * 60 * 1000 );

                //contact.sendMessage(JID.jidInstance("user@test.domain"), "Test", "This is a test");

                Thread.sleep( 2 * 60 * 1000 );

                contact.disconnect();
            }
            catch(JaxmppException ex) {
                Log.e("Steo", "Bob" + ex.getMessage());
            }
            catch(InterruptedException ex) {
                Log.e("Steo", "Bob" + ex.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
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
