package com.techtest.jaxmppdemo;

import java.util.List;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.factory.UniversalFactory;
import tigase.jaxmpp.core.client.factory.UniversalFactory.FactorySpi;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem;
import tigase.jaxmpp.j2se.Jaxmpp;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector.DnsResolver;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;

public class GtalkService extends Service {

    private final String LOG_TAG = getClass().getSimpleName();

    private static final String GTALK_DOMAIN = "talk.google.com";
    private int mSessionId = 0;

    public static interface OnLoginComplete {
        public void onLoginComplete(int sessionId);
        public void onLoginFailure();
    }

    public static interface OnMessageSent {
        public void onMessageSent();
        public void onMessageFailed();
    }

    public static class LoginDetails {
        public String username;
        public String password;
        public OnLoginComplete callback;

        public LoginDetails(String username, String password, OnLoginComplete callback) {
            this.username = username;
            this.password = password;
            this.callback = callback;
        }
    }

    public interface GtalkBinder {

        /**
         * Returns a session ID which is the currency for all future calls
         * @param loginDetails
         * @return
         */
        public int login(LoginDetails loginDetails);
        public void cancelLogin(int sessionId);
        public List<RosterItem> getContacts(int sessionId);
        public void sendMessage(String message, RosterItem contact, int sessionId, OnMessageSent callback);
    }


    private final SparseArray<Jaxmpp> mSessions = new SparseArray<Jaxmpp>();
    private final SparseArray<LoginDetails> mLogins = new SparseArray<LoginDetails>();

    private class GtalkBinderImpl extends Binder implements GtalkBinder {

        @Override
        public int login(LoginDetails loginDetails) {
            return GtalkService.this.login(loginDetails);
        }

        @Override
        public void cancelLogin(int sessionId) {
            GtalkService.this.cancelLogin(sessionId);
        }

        @Override
        public List<RosterItem> getContacts(int sessionId) {
            return GtalkService.this.getContacts(sessionId);
        }

        @Override
        public void sendMessage(String message, RosterItem contact, int sessionId, OnMessageSent callback){
            GtalkService.this.sendMessage(message, contact, sessionId, callback);
        }
    }

    private final GtalkBinderImpl mBinder = new GtalkBinderImpl();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void sendMessage(final String message, final RosterItem contact,
            int sessionId, final OnMessageSent callback) {

        final Jaxmpp session = mSessions.get(sessionId);
        if(session == null) {
            throw new IllegalStateException("Trying to get contacts and not logged in");
        }

        new SendMessageAsyncTask(message, contact, sessionId, callback).execute();
    }

    private class SendMessageAsyncTask extends AsyncTask<Void, Void, Boolean> {

        private final RosterItem mContact;
        private final String mMessage;
        private final int mSessionId;
        private final OnMessageSent mCallback;  //TODO: Should be cancellable

        public SendMessageAsyncTask(final String message, final RosterItem contact,
                int sessionId, final OnMessageSent callback) {
            mSessionId = sessionId;
            mContact = contact;
            mMessage = message;
            mCallback = callback;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            final Jaxmpp session = mSessions.get(mSessionId);
            if(session == null) {
                return false;
            }

            try {
                session.sendMessage(JID.jidInstance(mContact.getJid()), "test", mMessage);
            } catch (XMLException e) {

                e.printStackTrace();
                return false;

            } catch (JaxmppException e) {

                e.printStackTrace();
                return false;

            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if(result) {
                mCallback.onMessageSent();
            }
            else {
                mCallback.onMessageFailed();
            }
        }
    }

    public List<RosterItem> getContacts(int sessionId) {

        Jaxmpp session = mSessions.get(sessionId);
        if(session == null) {
            throw new IllegalStateException("Trying to get contacts and not logged in");
        }

        return session.getRoster().getAll();
    }

    public void cancelLogin(int sessionId) {
        mLogins.remove(sessionId);
    }

    public int login(LoginDetails loginDetails) {

        int newSessionId = mSessionId++;
        mLogins.put(newSessionId, loginDetails);

        new LoginAsyncTask(newSessionId).execute();
        return newSessionId;
    }

    private class LoginAsyncTask extends AsyncTask<Void, Void, Boolean> {

        private final int mId;

        public LoginAsyncTask(int sessionId) {
            mId = sessionId;
        }

        @SuppressWarnings("rawtypes")
        @Override
        protected Boolean doInBackground(Void... params) {

            UniversalFactory.setSpi(DnsResolver.class.getName(), new FactorySpi() {
                @Override
                public AndroidDNSResolver create() {
                    return new AndroidDNSResolver();
                }
            });

            try {
                final Jaxmpp contact = new Jaxmpp();

                LoginDetails details = mLogins.get(mId);

                contact.getProperties().setUserProperty(SessionObject.DOMAIN_NAME, GTALK_DOMAIN);
                contact.getProperties().setUserProperty(SessionObject.USER_BARE_JID,
                        BareJID.bareJIDInstance(details.username) );
                contact.getProperties().setUserProperty(SessionObject.PASSWORD, details.password );

                Log.v(LOG_TAG, "Logging in as " + details.username + " and pw " + details.password);
                contact.login();

                Log.v(LOG_TAG, "Login ok");

                mSessions.put(mId, contact);
            }
            catch(JaxmppException ex) {
                Log.e(LOG_TAG, ex.getMessage());
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            //Always read from list in case it has been cancelled
            LoginDetails details = mLogins.get(mId);
            if(details == null) {
                return;
            }

            if(result) {
                details.callback.onLoginComplete(mId);
            }
            else {
                details.callback.onLoginFailure();
                mLogins.remove(mId);
            }
        }
    }
}

