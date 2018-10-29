package intelligentproduct.solutions.amazonsns;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.DeleteEndpointRequest;
import com.amazonaws.services.sns.model.ListSubscriptionsByTopicResult;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sns.model.Subscription;
import com.amazonaws.services.sns.model.UnsubscribeRequest;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.List;

/**
 * Created by mariettam on 9/15/17.
 */

/**
 * This class implements AWS functions for setting up, subscribing to, and unsubscribing to Amazon SNS for sending and receiving messages.
 * The full AWS API can be found here:
 * http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/sns/AmazonSNSClient.html
 */
public class AmazonSNSHelper {
    public AWSCredentials awsCredentials;
    public AmazonSNSClient pushClient;
    public String AWSAccessKey, AWSSecretKey, platformApplicationArn;

    public AmazonSNSHelper(String accessKey, String secretKey, String platformAppArn){
        AWSAccessKey = accessKey;
        AWSSecretKey = secretKey;
        platformApplicationArn = platformAppArn;

        awsCredentials = new BasicAWSCredentials(AWSAccessKey, AWSSecretKey);
        pushClient = new AmazonSNSClient(awsCredentials);
    }

    /**
     * Creates the PlatformEndpoint for the app
     * @return the ARN of the PlatformEndpoint that was created
     */
    public String createPlatformEndpoint()
    {
        try
        {
            // Get Firebase token
            String token = FirebaseInstanceId.getInstance().getToken();

            String msg = "InstanceID Token: " + token;

            // Makes the platform application for the app
            CreatePlatformEndpointRequest platformEndpointRequest = new CreatePlatformEndpointRequest();
            platformEndpointRequest.setCustomUserData("Android app");
            platformEndpointRequest.setToken(token);
            platformEndpointRequest.setPlatformApplicationArn(platformApplicationArn);

            CreatePlatformEndpointResult result = pushClient.createPlatformEndpoint(platformEndpointRequest);
            return result.getEndpointArn();
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    /**
     * Creates a new topic
     * @param topicName The name of the topic to create
     * @return the topic ARN
     */
    public String createTopic(String topicName)
    {
        try
        {
            CreateTopicResult createTopicResult = pushClient.createTopic(topicName);
            return createTopicResult.getTopicArn();
        }
        catch (Exception e)
        {
            throw e;
        }

    }

    /**
     * Deletes a topic
     * @param topicARN The topic ARN of the topic to delete
     */
    public void deleteTopic(String topicARN)
    {
        try
        {
            pushClient.deleteTopic(topicARN);
        }
        catch (Exception e)
        {
            throw e;
        }
    }

    /**
     * Deletes the Amazon SNS endpoint for this application
     * @param deleteEndpointRequest the DeleteEndpointRequest containing the endpoint ARN for this application
     */
    public void deleteEndpoint(DeleteEndpointRequest deleteEndpointRequest)
    {
        try
        {
            pushClient.deleteEndpoint(deleteEndpointRequest);
        }
        catch (Exception e)
        {
            throw e;
        }
    }

    /**
     *
     * Subscribes an email address to receive notifications from Amazon SNS
     * @param topicARN the topic that was received from the bridge that the app will subscribe to
     * @param emailAddress the email address to subscribe to the notifications
     */
    public void emailSubscribe(String topicARN, String emailAddress)
    {
        try
        {
            // Creates the subscription request and subscribes the email address to the topic
            SubscribeRequest sr = new SubscribeRequest();
            sr.setTopicArn(topicARN);
            sr.setEndpoint(emailAddress);
            sr.setProtocol("email");
            SubscribeResult subResult = pushClient.subscribe(sr);
        }
        catch(Exception e)
        {
            throw e;
        }

    }

    /**
     * Unsubscribes the email address from receiving notifications from Amazon SNS
     * @param topicARN
     * @param emailAddress the email address to unsubscribe from notifications
     */
    public void emailUnsubscribe(String topicARN, String emailAddress)
    {
        try
        {
            // Gets a list of all of the subscriptions in the topic
            ListSubscriptionsByTopicResult listResult = pushClient.listSubscriptionsByTopic(topicARN);
            List<Subscription> subscriptions = listResult.getSubscriptions();
            String subscriptionARN="";

            // Searches for the subscription that contains the email address
            for (Subscription subs : subscriptions) {
                if(subs.getProtocol().equals("email") && subs.getEndpoint().equals(emailAddress)) {
                    // Gets this subscription's ARN
                    subscriptionARN = subs.getSubscriptionArn();
                    break;
                }
            }

            if (subscriptionARN != "")
            {
                // Creates the unsubscribe request and unsubscribes the email address from the topic
                UnsubscribeRequest ur = new UnsubscribeRequest();
                ur.setSubscriptionArn(subscriptionARN);
                pushClient.unsubscribe(ur);
            }

        }
        catch(Exception e)
        {
            throw e;
        }
    }


    /**
     * Publishes a message to a topic. Any device or address that is subcribed to this topic will receive the message
     * @param topicArn The topic ARN that the message will be published to
     * @param message The message that will be sent
     * @param subject The subject of the message that will be sent
     */
    public void publishMessage(String topicArn, String message, String subject)
    {
        try
        {
            PublishResult publishResult = pushClient.publish(topicArn, message, subject);
        }
        catch (Exception e)
        {
            throw e;
        }
    }

    /**
     * Subscribes the application to receive push notifications from Amazon SNS
     * @param topicARN the topic that was received from the bridge that the app will subscribe to
     */
    public String pushNotificationSubscribe(String topicARN, String platformEndpointARN)
    {
        try
        {
            // Creates the subscription request and subscribes to SNS
            SubscribeRequest sr = new SubscribeRequest();
            sr.setTopicArn(topicARN);
            sr.setEndpoint(platformEndpointARN);
            sr.setProtocol("application");
            SubscribeResult subResult = pushClient.subscribe(sr);
            return subResult.getSubscriptionArn();
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    /**
     * Unsubscribes the application from receiving push notifications from Amazon SNS
     * @param subscriptionARN the subscription ARN to unsubscribe with
     */
    public void pushNotificationUnsubscribe(String subscriptionARN)
    {
        try
        {
            // Creates the unsubscription request and unsubscribes
            UnsubscribeRequest ur = new UnsubscribeRequest();
            ur.setSubscriptionArn(subscriptionARN);
            pushClient.unsubscribe(ur);
        }
        catch(Exception e)
        {
            throw e;
        }
    }
}
