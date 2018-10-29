package intelligentproduct.solutions.amazonsns;

import android.support.test.runner.AndroidJUnit4;

import com.amazonaws.services.sns.model.DeleteEndpointRequest;
import com.amazonaws.services.sns.model.Endpoint;
import com.amazonaws.services.sns.model.ListEndpointsByPlatformApplicationRequest;
import com.amazonaws.services.sns.model.ListEndpointsByPlatformApplicationResult;
import com.amazonaws.services.sns.model.ListSubscriptionsByTopicResult;
import com.amazonaws.services.sns.model.ListTopicsResult;
import com.amazonaws.services.sns.model.Subscription;
import com.amazonaws.services.sns.model.Topic;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static intelligentproduct.solutions.amazonsns.Constants.emailAddressToTest;
import static intelligentproduct.solutions.amazonsns.Constants.samplePlatformApplicationArn;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import static junit.framework.Assert.assertNotNull;
import static org.awaitility.Awaitility.*;


/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AmazonSNSHelperTest {
    static String topicARN = null;
    static String platformARN = null;
    static String subscriptionArn = null;

    /**
     * Tests that the platform endpoint can be created
     */
    @Test
    public void test1_createPlatformEndpointTest() {

        try {
            final AmazonSNSHelper snsHelper = new AmazonSNSHelper(Constants.sampleAccessKey, Constants.sampleSecretKey, samplePlatformApplicationArn);
            platformARN = snsHelper.createPlatformEndpoint();
            assertNotNull("Platform ARN is null", platformARN);

            String endpointARNStart = samplePlatformApplicationArn.replace("app", "endpoint");
            assertThat(platformARN, startsWith(endpointARNStart));

            //Gets the list of current endpoints from AWS and checks that the new endpoint is in this list
            await().until(checkAWSEnpointList(snsHelper, endpointARNStart), equalTo(true));
        } catch (Exception e) {
            assertTrue("Exception caught: " + e.getMessage(), false);
        }
    }

    /**
     * Tests that a topic can be created in the platform endpoint that was previously created
     */
    @Test
    public void test2_createTopicTest() {

        try {
            final AmazonSNSHelper snsHelper = new AmazonSNSHelper(Constants.sampleAccessKey, Constants.sampleSecretKey, samplePlatformApplicationArn);
            topicARN = snsHelper.createTopic(Constants.testTopicName);
            assertNotNull("Topic ARN is null", topicARN);
            assertEquals("Topic ARN does not match the expected value", topicARN, Constants.testTopicArn);

            //Gets the list of current topics from AWS and checks that the new topic is in this list
            await().until(checkAWSTopicList(snsHelper), equalTo(true));

        } catch (Exception e) {
            assertTrue("Exception caught: " + e.getMessage(), false);
        }
    }

    /**
     * Tests that an email address can be subscribed to receive notifications from the topic
     * NOTE: the email address in the Constants class must be updated before running this test
     */
    @Test
    public void test3_emailSubscribeTest() {

        try {
            assertNotNull("Topic ARN is null", topicARN);
            final AmazonSNSHelper snsHelper = new AmazonSNSHelper(Constants.sampleAccessKey, Constants.sampleSecretKey, Constants.samplePlatformApplicationArn);
            snsHelper.emailSubscribe(topicARN, emailAddressToTest);

            //Gets the list of current subscriptions from AWS and checks that the new subscription is in this list
            await().until(checkAWSEmailSubscriptionList(snsHelper), equalTo(true));
        } catch (Exception e) {
            assertTrue("Exception caught: " + e.getMessage(), false);
        }
    }

    /**
     * Tests that the device can be subscribed to receive notifications from the topic
     */
    @Test
    public void test4_pushNotificationSubscribeTest() {

        try {
            assertNotNull("Topic ARN is null", topicARN);
            final AmazonSNSHelper snsHelper = new AmazonSNSHelper(Constants.sampleAccessKey, Constants.sampleSecretKey, Constants.samplePlatformApplicationArn);
            subscriptionArn = snsHelper.pushNotificationSubscribe(topicARN, platformARN);
            assertNotNull("Subscription ARN is null", subscriptionArn);

            //Gets the list of current subscriptions from AWS and checks that the new subscription is in this list
            await().until(checkAWSPushSubscriptionList(snsHelper), equalTo(true));

        } catch (Exception e) {
            assertTrue("Exception caught: " + e.getMessage(), false);
        }
    }

    /**
     * Tests that a message can be published to the topic and checks that the device, which is
     * subscribed to the topic, received the message
     */
    @Test
    public void test5_publishMessageTest() {

        try {
            //clear log
            Process clearProcess = new ProcessBuilder()
                    .command("logcat", "-c")
                    .redirectErrorStream(true)
                    .start();

            assertNotNull("Topic ARN is null", topicARN);
            final AmazonSNSHelper snsHelper = new AmazonSNSHelper(Constants.sampleAccessKey, Constants.sampleSecretKey, Constants.samplePlatformApplicationArn);
            snsHelper.publishMessage(topicARN, "message", "subject");

            //Get log
            Process getLogProcess = Runtime.getRuntime().exec("logcat");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(getLogProcess.getInputStream()));

            StringBuilder log = new StringBuilder();
            String line = "";
            boolean notificationMessageFound = false;

            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
                if (line.contains("Amazon SNS message received - Message data payload:")) {
                    notificationMessageFound = true;
                    break;
                }
            }

            assertTrue("Notification was not sent", notificationMessageFound);

        } catch (Exception e) {
            assertTrue("Exception caught: " + e.getMessage(), false);
        }
    }

    /**
     * Tests that an email address that is subscribed to the topic can be unsubscribed
     * NOTE: The email address that is used in this test must have been subscribed to the topic
     * and must have clicked the "Confirm Subscription" link in the email it received. This
     * test will fail if the subscription was not confirmed prior to running this test
     */
    @Test
    public void test6_emailUnsubscribeTest() {

        try {
            assertNotNull("Topic ARN is null", topicARN);
            final AmazonSNSHelper snsHelper = new AmazonSNSHelper(Constants.sampleAccessKey, Constants.sampleSecretKey, Constants.samplePlatformApplicationArn);
            snsHelper.emailUnsubscribe(topicARN, emailAddressToTest);

            //Gets the list of current subscriptions from AWS and checks that the deleted subscription is not in this list
            await().until(checkAWSEmailSubscriptionList(snsHelper), equalTo(false));
        } catch (Exception e) {
            assertTrue("Exception caught: " + e.getMessage(), false);
        }
    }

    /**
     * Tests that the device, which was subscribed to the topic, can be unsubscribed
     */
    @Test
    public void test7_pushNotificationUnsubscribeTest() {

        try {
            assertNotNull("Subscription ARN is null", subscriptionArn);
            final AmazonSNSHelper snsHelper = new AmazonSNSHelper(Constants.sampleAccessKey, Constants.sampleSecretKey, Constants.samplePlatformApplicationArn);
            snsHelper.pushNotificationUnsubscribe(subscriptionArn);

            //Gets the list of current subscriptions from AWS and checks that the deleted subscription is not in this list
            await().until(checkAWSPushSubscriptionList(snsHelper), equalTo(false));
        } catch (Exception e) {
            assertTrue("Exception caught: " + e.getMessage(), false);
        }
    }

    /**
     * Tests that the topic can be deleted
     */
    @Test
    public void test8_deleteTopicTest() {

        try {
            assertNotNull("Topic ARN is null", topicARN);
            final AmazonSNSHelper snsHelper = new AmazonSNSHelper(Constants.sampleAccessKey, Constants.sampleSecretKey, samplePlatformApplicationArn);
            snsHelper.deleteTopic(topicARN);
            topicARN = null;

            //Gets the list of current topics from AWS and checks that the deleted topic is not in this list
            await().until(checkAWSTopicList(snsHelper), equalTo(false));
        } catch (Exception e) {
            assertTrue("Exception caught: " + e.getMessage(), false);
        }
    }

    /**
     * Tests that the endpoint can be deleted
     */
    @Test
    public void test9_deleteEndpointTest() {

        try {
            final AmazonSNSHelper snsHelper = new AmazonSNSHelper(Constants.sampleAccessKey, Constants.sampleSecretKey, samplePlatformApplicationArn);

            assertNotNull("Platform ARN is null", platformARN);
            DeleteEndpointRequest request = new DeleteEndpointRequest();
            request.setEndpointArn(platformARN);
            String endpointArn = request.getEndpointArn();
            assertNotNull("DeleteEndpointRequest is null", request);

            snsHelper.deleteEndpoint(request);
            platformARN = null;

            //Gets the list of current endpoints from AWS and checks that the deleted endpoint is not in this list
            await().until(checkAWSEnpointList(snsHelper, endpointArn), equalTo(false));

        } catch (Exception e) {
            assertTrue("Exception caught: " + e.getMessage(), false);
        }
    }

    /**
     * Receives the list of current endpoints from AWS and checks if a certain endpoint exists in it
     * @param snsHelper AmazonSNSHelper object
     * @param endpointARNStart The endpoint that is being searched for
     * @return true if it was found and false if it was not
     */
    private Callable<Boolean> checkAWSEnpointList(AmazonSNSHelper snsHelper, String endpointARNStart) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                //Gets the list of current endpoints from AWS
                ListEndpointsByPlatformApplicationRequest endpointListRequest = new ListEndpointsByPlatformApplicationRequest();
                endpointListRequest.setPlatformApplicationArn(samplePlatformApplicationArn);
                ListEndpointsByPlatformApplicationResult currentEndpoints = snsHelper.pushClient.listEndpointsByPlatformApplication(endpointListRequest);
                List<Endpoint> endpointObjList = currentEndpoints.getEndpoints();

                // Checks if the endpoint was found in the list that was returned
                boolean endpointFound = false;
                for (Endpoint endpoint : endpointObjList) {
                    if (endpoint.getEndpointArn().startsWith(endpointARNStart))
                        endpointFound = true;
                }
                return endpointFound;
            }
        };
    }

    /**
     * Receives the list of current topics from AWS and checks if the current topic exists in it
     * @param snsHelper AmazonSNSHelper object
     * @return true if it was found and false if it was not
     */
    private Callable<Boolean> checkAWSTopicList(AmazonSNSHelper snsHelper) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                ListTopicsResult currentTopics = snsHelper.pushClient.listTopics();
                List<Topic> topicList = currentTopics.getTopics();
                List<String> topicARNs = new ArrayList<>();
                for (Topic topic : topicList) {
                    topicARNs.add(topic.getTopicArn());
                }
                return topicARNs.contains(topicARN);
            }
        };
    }

    /**
     * Receives the list of current subscriptions from AWS and checks if the android device's
     * subscription information is returned in it
     * @param snsHelper AmazonSNSHelper object
     * @return true if it was found and false if it was not
     */
    private Callable<Boolean> checkAWSPushSubscriptionList(AmazonSNSHelper snsHelper) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                ListSubscriptionsByTopicResult currentSubscriptions = snsHelper.pushClient.listSubscriptionsByTopic(topicARN);
                List<Subscription> subscriptionList = currentSubscriptions.getSubscriptions();
                String endpointARNStart = samplePlatformApplicationArn.replace("app", "endpoint");
                boolean subscriptionFound = false;
                for (Subscription subs : subscriptionList) {

                    if (subs.getEndpoint().startsWith(endpointARNStart) && subs.getProtocol().equals("application"))
                        subscriptionFound = true;
                }

                return subscriptionFound;
            }
        };
    }

    /**
     * Receives the list of current subscriptions from AWS and checks if a certain email address
     * subscription is returned in it
     * @param snsHelper AmazonSNSHelper object
     * @return true if it was found and false if it was not
     */
    private Callable<Boolean> checkAWSEmailSubscriptionList(AmazonSNSHelper snsHelper) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                ListSubscriptionsByTopicResult currentSubscriptions = snsHelper.pushClient.listSubscriptionsByTopic(topicARN);
                List<Subscription> subscriptionList = currentSubscriptions.getSubscriptions();
                List<String> subscriptions = new ArrayList<>();
                for (Subscription subs : subscriptionList) {
                    subscriptions.add(subs.getEndpoint());
                }

                return subscriptions.contains(emailAddressToTest);
            }
        };
    }
}
