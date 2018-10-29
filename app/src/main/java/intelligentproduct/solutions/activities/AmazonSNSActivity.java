package intelligentproduct.solutions.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.amazonaws.services.sns.model.DeleteEndpointRequest;

import java.text.SimpleDateFormat;
import java.util.Date;

import intelligentproduct.solutions.amazonsns.AmazonSNSHelper;
import intelligentproduct.solutions.amazonsns.Constants;
import intelligentproduct.solutions.amazonsns.R;
import intelligentproduct.solutions.amazonsns.SNSAsyncTaskResponse;


/**
 * Created by mariettam on 9/15/17.
 */

public class AmazonSNSActivity  extends AppCompatActivity {
    String TAG = "Amazon SNS Test";
    Context context = this;

    final AmazonSNSHelper snsHelper = new AmazonSNSHelper(Constants.sampleAccessKey, Constants.sampleSecretKey, Constants.samplePlatformApplicationArn);

    String sharedPrefsAppName = "AmazonSNSTest";
    String sharedPrefsSubscriptionARN = "subscriptionARN";
    String sharedPrefsNewTopic = "newTopic";
    String sharedPrefsPlatEndpointARN = "platformEndpoint";
    SharedPreferences prefs ;
    String emailAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.amazon_sns_activity);

        prefs = context.getSharedPreferences(sharedPrefsAppName, Context.MODE_PRIVATE);

        /*********
         * Sets up the button listeners which start the necessary async tasks that will handle the
         * function since Amazon SNS requires its functions to not be run on the main thread
         *********/
        Button createSNSTopic = (Button) findViewById(R.id.create_sns_topic);
        createSNSTopic.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    String newTopicARN = prefs.getString(sharedPrefsNewTopic, null);
                    if(newTopicARN == null) {
                        new CreateTopic().execute();
                    }
                    else
                    {
                        showAlertDialog("Topic already created", "A new topic has already been created with this device. Please delete it and before creating a new one.");
                    }
                } catch (Exception e) {

                }
            }
        });

        Button subscribeDevice = (Button) findViewById(R.id.subscribe_sns_device);
        subscribeDevice.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    new DeviceSubscribe().execute();
                } catch (Exception e) {

                }
            }
        });

        Button subscribeEmail = (Button) findViewById(R.id.subscribe_sns_email);
        subscribeEmail.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {

                    EditText emailAddressET = (EditText) findViewById(R.id.sns_email_address);
                    emailAddress = emailAddressET.getText().toString();
                    new EmailSubscribe().execute();
                } catch (Exception e) {

                }
            }
        });

        Button sendMessage = (Button) findViewById(R.id.send_sns_message);
        sendMessage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    new SendMessage().execute();
                } catch (Exception e) {

                }
            }
        });

        Button unsubscribeDevice = (Button) findViewById(R.id.unsubscribe_sns_device);
        unsubscribeDevice.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    new DeviceUnsubscribe().execute();
                } catch (Exception e) {

                }
            }
        });

        Button unsubscribeEmail = (Button) findViewById(R.id.unsubscribe_sns_email);
        unsubscribeEmail.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    EditText emailAddressET = (EditText) findViewById(R.id.unsub_email_address);
                    emailAddress = emailAddressET.getText().toString();
                    new EmailUnsubscribe().execute();

                } catch (Exception e) {

                }
            }
        });

        Button deleteSNSTopic = (Button) findViewById(R.id.delete_sns_topic);
        deleteSNSTopic.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    String newTopicARN = prefs.getString(sharedPrefsNewTopic, null);
                    if(newTopicARN != null) {
                        new DeleteTopic().execute();
                    }
                    else
                    {
                        showAlertDialog("Topic not found", "Could not find a topic to delete. One may not have been made with this device. Please create one and try again.");
                    }
                } catch (Exception e) {

                }
            }
        });

        Button deleteEndpoint = (Button) findViewById(R.id.delete_endpoint);
        deleteEndpoint.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    new DeletePlatformEndpoint().execute();

                } catch (Exception e) {

                }
            }
        });
    }

    public void showAlertDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle(title)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /******************
     * Async Tasks
     * Amazon SNS functions cannot be done in the main thread
     ******************/

    /*
        Subscribe the device to the topic to start receiving messages from Amazon SNS
     */
    public class DeviceSubscribe extends AsyncTask<String, Void, SNSAsyncTaskResponse> {
        @Override
        protected SNSAsyncTaskResponse doInBackground(String... s) {

            // Creates a new PlatformEndpoint and gets its ARN
            String platformEndpointARN = snsHelper.createPlatformEndpoint();
            prefs.edit().putString(sharedPrefsPlatEndpointARN, platformEndpointARN).apply();

            String newTopicARN = prefs.getString(sharedPrefsNewTopic, null);
            if(newTopicARN != null) {
                String subscriptionARN = snsHelper.pushNotificationSubscribe(newTopicARN, platformEndpointARN);

                // Saves the subscription ARN to shared preferences to remember the ARN if the app is closed
                prefs.edit().putString(sharedPrefsSubscriptionARN, subscriptionARN).apply();
                return new SNSAsyncTaskResponse("Success", null);
            }
            else
            {
                Exception e = new Exception("Could not get the topic ARN");
                return new SNSAsyncTaskResponse(null, e);
            }
        }

        @Override
        protected void onPostExecute(SNSAsyncTaskResponse result) {
            super.onPostExecute(result);

            if (result.error == null) {
                showAlertDialog("Push Notification Subscription Successful", "You will now receive push notifications.");
            } else {
                Throwable error = result.error;
                showAlertDialog("Alert", "Unable to subscribe to notifications");
                if (error.getMessage() != null) {
                    Log.e(TAG, "Error: " + error.getMessage());
                }

            }
        }
    }

    /*
        Subscribe the email address to the topic to start receiving messages from Amazon SNS
     */
    public class EmailSubscribe extends AsyncTask<String, Void, SNSAsyncTaskResponse> {
        @Override
        protected SNSAsyncTaskResponse doInBackground(String... s) {

            try {
                // Gets the topic ARN that was saved to shared preferences
                String newTopicARN = prefs.getString(sharedPrefsNewTopic, null);
                if(newTopicARN != null) {
                    // Subscribes the email address to the topic
                    snsHelper.emailSubscribe(newTopicARN, emailAddress);
                    return new SNSAsyncTaskResponse("Success", null);
                }
                else
                {
                    Exception e = new Exception("Could not get the topic ARN");
                    return new SNSAsyncTaskResponse(null, e);
                }
            } catch (Exception e) {
                String ex = e.toString();
                return new SNSAsyncTaskResponse(null, e);
            }
        }

        @Override
        protected void onPostExecute(SNSAsyncTaskResponse result) {
            super.onPostExecute(result);

            if (result.error == null) {
                showAlertDialog("Email Notification Subscription Successful",
                        "You will now receive a subscription confirmation email. After you confirm the subscription, you will be able to receive notification emails.");
            } else {
                Throwable error = result.error;
                showAlertDialog("Alert", "Unable to subscribe to notifications");
                if (error.getMessage() != null) {
                    Log.e(TAG, "Error: " + error.getMessage());
                }
            }
        }
    }

    /*
        Unsubscribe the device from the topic to stop receiving messages from Amazon SNS
     */
    private class DeviceUnsubscribe extends AsyncTask<String, Void, SNSAsyncTaskResponse> {
        @Override
        protected SNSAsyncTaskResponse doInBackground(String... s) {

            try {
                // Gets the subscription ARN that was saved to shared preferences
                String subscriptionARN = prefs.getString(sharedPrefsSubscriptionARN, null);
                if(subscriptionARN != null)
                {
                    // Uses the subscription ARN to unsubscribe the device from the topic
                    snsHelper.pushNotificationUnsubscribe(subscriptionARN);
                    return new SNSAsyncTaskResponse("Success", null);
                }
                else
                {
                    Exception error = new Exception("Could not get the subscription ARN for this device. It may not be subscribed.");
                    return new SNSAsyncTaskResponse(null, error);
                }
            } catch (Exception e) {
                String ex = e.toString();
                return new SNSAsyncTaskResponse(null, e);
            }
        }

        @Override
        protected void onPostExecute(SNSAsyncTaskResponse result) {
            super.onPostExecute(result);

            if (result.error == null) {
                showAlertDialog("Push Notification Unsubscription Successful", "You will no longer receive push notifications.");
            } else {
                Throwable error = result.error;
                showAlertDialog("Alert", "Unable to unsubscribe from notifications");
                if (error.getMessage() != null) {
                    Log.e(TAG, "Error: " + error.getMessage());
                }
            }
        }
    }

    /*
        Unsubscribe the email address from the topic to stop receiving messages from Amazon SNS
     */
    private class EmailUnsubscribe extends AsyncTask<String, Void, SNSAsyncTaskResponse> {
        @Override
        protected SNSAsyncTaskResponse doInBackground(String... s) {
            // Gets the topic ARN that was saved in shared preferences
            String newTopicARN = prefs.getString(sharedPrefsNewTopic, null);
            if(newTopicARN != null) {
                // Unsubscribes the email address from the topic
                snsHelper.emailUnsubscribe(newTopicARN, emailAddress);
                return new SNSAsyncTaskResponse("Success", null);
            }
            else
            {
                Exception e = new Exception("Could not get the topic ARN");
                return new SNSAsyncTaskResponse(null, e);
            }
        }

        @Override
        protected void onPostExecute(SNSAsyncTaskResponse result) {
            super.onPostExecute(result);

            if (result.error == null) {
                showAlertDialog("Email Notification Unsubscription Successful", "You will no longer receive email notifications.");
            } else {
                Throwable error = result.error;
                showAlertDialog("Alert", "Unable to unsubscribe from email notifications");
                if (error.getMessage() != null) {
                    Log.e(TAG, "Error: " + error.getMessage());
                }
            }
        }
    }

    /*
        Sends an Amazon SNS message to any subscriptions on the topic
     */
    private class SendMessage extends AsyncTask<String, Void, SNSAsyncTaskResponse> {
        @Override
        protected SNSAsyncTaskResponse doInBackground(String... s) {
            // Gets the topic ARN that was saved in shared preferences
            String newTopicARN = prefs.getString(sharedPrefsNewTopic, null);
            if(newTopicARN != null) {
                // Publishes a test message to the topic which will be sent to anything that is subscribed to it
                snsHelper.publishMessage(newTopicARN, "Test message", "Amazon SNS Test");
                return new SNSAsyncTaskResponse("Success", null);
            }
            else
            {
                Exception e = new Exception("Could not get the topic ARN");
                return new SNSAsyncTaskResponse(null, e);
            }
        }

        @Override
        protected void onPostExecute(SNSAsyncTaskResponse result) {
            super.onPostExecute(result);

            if (result.error == null) {
                showAlertDialog("Message Sent", "Message sent successfully");
            } else {
                Throwable error = result.error;
                showAlertDialog("Alert", "Error when sending the message");
                if (error.getMessage() != null) {
                    Log.e(TAG, "Error: " + error.getMessage());
                }
            }
        }
    }

    /**
     * Creates a new topic
     */
    private class CreateTopic extends AsyncTask<String, Void, SNSAsyncTaskResponse> {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String currentDateandTime = sdf.format(new Date());
        String topicName = "AmazonSNSTest" + currentDateandTime.toString();
        @Override
        protected SNSAsyncTaskResponse doInBackground(String... s) {

            try {
                // Creates a new topic
                String newTopicARN = snsHelper.createTopic(topicName);

                // Saves the topic ARN to shared preferences
                prefs.edit().putString(sharedPrefsNewTopic, newTopicARN).apply();
                return new SNSAsyncTaskResponse("Success", null);
            } catch (Exception e) {
                String ex = e.toString();
                return new SNSAsyncTaskResponse(null, e);
            }
        }

        @Override
        protected void onPostExecute(SNSAsyncTaskResponse result) {
            super.onPostExecute(result);

            if (result.error == null) {
                showAlertDialog("Topic created", "The following topic was created successfully: " + topicName);
            } else {
                Throwable error = result.error;
                showAlertDialog("Alert", "Error when creating the topic");
                if (error.getMessage() != null) {
                    Log.e(TAG, "Error: " + error.getMessage());
                }
            }
        }
    }

    /**
     * Deletes the topic that was created
     */
    private class DeleteTopic extends AsyncTask<String, Void, SNSAsyncTaskResponse> {
        @Override
        protected SNSAsyncTaskResponse doInBackground(String... s) {

            try {
                // Gets the topic ARN that was saved in shared preferences
                String newTopicARN = prefs.getString(sharedPrefsNewTopic, null);
                if(newTopicARN != null) {
                    // Deletes the topic
                    snsHelper.deleteTopic(newTopicARN);

                    // Deletes the topic ARN from shared preferences
                    prefs.edit().putString(sharedPrefsNewTopic, null).apply();
                    return new SNSAsyncTaskResponse("Success", null);
                }
                else
                {
                    Exception e = new Exception("Could not find topic to delete");
                    return new SNSAsyncTaskResponse(null, e);
                }
            } catch (Exception e) {
                String ex = e.toString();
                return new SNSAsyncTaskResponse(null, e);
            }
        }

        @Override
        protected void onPostExecute(SNSAsyncTaskResponse result) {
            super.onPostExecute(result);

            if (result.error == null) {
                showAlertDialog("Topic deleted", "Topic deleted successfully");
            } else {
                Throwable error = result.error;
                showAlertDialog("Alert", "Error when deleting the topic");
                if (error.getMessage() != null) {
                    Log.e(TAG, "Error: " + error.getMessage());
                }
            }
        }
    }

    /*
    Delete's the endpoint for the application on this device that was created when subscribing this device for notifications
     */
    private class DeletePlatformEndpoint extends AsyncTask<String, Void, SNSAsyncTaskResponse> {
        @Override
        protected SNSAsyncTaskResponse doInBackground(String... s) {

            try {
                // Gets the platform endpoint ARN from shared preferences
                String endpointARN = prefs.getString(sharedPrefsPlatEndpointARN, null);
                if(endpointARN != null) {
                    // Creates a DeleteEndpointRequest object containing the platform endpoint ARN
                    DeleteEndpointRequest request = new DeleteEndpointRequest();
                    request.setEndpointArn(endpointARN);
                    snsHelper.deleteEndpoint(request);

                    // Deletes the platform endpoint ARN from shared preferences
                    prefs.edit().putString(sharedPrefsPlatEndpointARN, null).apply();
                    return new SNSAsyncTaskResponse("Success", null);
                }
                else
                {
                    Exception e = new Exception("Could not find the application's endpoint to delete");
                    return new SNSAsyncTaskResponse(null, e);
                }
            } catch (Exception e) {
                String ex = e.toString();
                return new SNSAsyncTaskResponse(null, e);
            }
        }

        @Override
        protected void onPostExecute(SNSAsyncTaskResponse result) {
            super.onPostExecute(result);

            if (result.error == null) {
                showAlertDialog("Endpoint deleted", "Endpoint deleted successfully");

            } else {
                Throwable error = result.error;
                showAlertDialog("Alert", "Error when deleting the endpoint");
                if (error.getMessage() != null) {
                    Log.e(TAG, "Error: " + error.getMessage());
                }
            }
        }
    }
}