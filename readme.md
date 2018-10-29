# Android Amazon SNS 

## Overview
The AmazonSNS sample project provides examples on how to use the Amazon Simple Notification Service (SNS), which is a service which lets endpoints, such as devices or email addresses, receive messages from the cloud. To do this, some set up is required for creating an AWS account, AWS users, a platform application, etc. These steps are described in the following sections. Also, to use SNS in an Android application, the device must also be registered to a push notification service, which is Firebase Cloud Messaging (FCM). This requires a Google account and the steps for setting up FCM are also described in the following section.

**Note:** This application only works on Android devices that are running Android 4.0 or higher and have Google Play Services installed on it at version 15.0.0 or higher.

In SNS, topics must be created which are what the endpoint subscribes to. Messages are published to these topics and then any endpoint that is subscribed to it will receive the message. For devices, it will appear as a push notification and for email addresses, it will appear as a new email. When endpoints are unsubscribed from the topic, they will no longer receive these messages. Using SNS makes it easier to send out notices to a large number of users. SNS also allows you to set up other kinds of endpoints as well as Android device and email, such as iOS devices, which are not described here. 


## Amazon SNS
**Package name:** intelligentproduct.solutions.amazonsns

**Permissions needed:**  
`<uses-permission android:name="android.permission.INTERNET" />`  

**Gradle dependencies:**  
`implementation 'com.google.firebase:firebase-core:16.0.1'`  
`implementation 'com.google.firebase:firebase-messaging:17.1.0'`  
`implementation 'com.amazonaws:aws-android-sdk-sns:2.6.24'`  

The latest versions of these dependencies can be found here:  
https://firebase.google.com/support/release-notes/android

### Amazon SNS Notifications Setup
To use Amazon SNS notifications in your own application, several steps are needed to set up the project in Firebase and Amazon prior to implementing it. The following steps outline what is needed for this:
1.	**Create a user in AWS**
    * a. In AWS, go to the IAM service section
    * b. Go to the User's tab on the left side of the screen
    * c. Click "Add user"
    * d. Enter a username and check "Programmatic Access" for the Access Type. Then click "Next: Permissions"
    * e. Select "Attach existing policies directly" and click "Create policy". This should open in a new tab
    * f. Select "Create policy". 
    **Note:** This will open a new screen in a new tab.
    * g. Click the JSON tab and enter a Policy Name and Description and copy this code into the Policy Document:
    ```
    {
        "Version": "2012-10-17",
        "Statement": [
            {
            "Sid": "AccessToSNS",
            "Effect": "Allow",
            "Action": [
                "sns:*"
            ],
            "Resource": [
                "*"
            ]
            }
        ]
    }
    ```
    * h. Click "Review Policy"
    * i. Enter a policy name and click "Create policy"
    * j. Return to the original tab where the new user was being created in the steps prior to step e. in this list.
    * k. Select the checkbox for the newly created policy in the list and click "Next: Review". If you do not see your policy in the list, try refreshing the page
    * l. Review the user information and click "Create user"
    * m. Click "Download .csv" to keep a copy of the new user's Access Key ID and Secret access key

2.	**Set up the push service**
    * a. Setting up a push service is needed for the devices that will be receiving the notifications. For Android, this push service would be Firebase Cloud Messaging (FCM) 
    * b.	Setting up FCM for Android
        * i.	Create an Android application with Android Studio
        * ii.   Sign into the FCM console with a Google account at 	https://console.firebase.google.com/
        * ii.   In the FCM console, click "Add project"
        * iv.	Enter the name of the project, check the checkboxes to accept the terms, and click "Create project"
        * v. Click "Continue" after the project has been created
        * vi. You will be automatically taken to the settings page for the new project. If this does not happen automatically, click the box with your new project name in it.
        * vii. Click the Android icon or "Add Firebase to your Android app" 
        * viii.	Enter the Android package name and click "Register app"
        * ix.	Download the google-services.json file
        * x. Add the JSON file to the root of the app directory in the Android application
        * xi.	Add the following lines to the build.gradle files
        ```
        Project-level build.gradle (<project>/build.gradle):

        buildscript {
            dependencies {
                // Add this line
                classpath 'com.google.gms:google-services:4.0.1'
            }
        }

        App-level build.gradle (<project>/<app-module>/build.gradle):

        // Add to the bottom of the file
        apply plugin: 'com.google.gms.google-services'
        ```
        * xii.	Sync the Gradle files with the project
        * xiii.	Sample code is available which outlines the classes that are needed to use the Cloud Messaging functionality of FCM at https://firebase.google.com/docs/samples/
        * xiv.	In the FCM console, go to the Project Settings of the project that was made.
        * xv.	Click on the "Cloud Messaging" tab
        * xvi.	Take note of the project's Server Key
3.	**Create a Platform Application in AWS**
    * a.	In AWS, go to the Simple Notification Service
    * b.	Click "Applications"
    * c.	Click "Create Platform Application"
    * d.	Enter the Application Name and select the Push Notification Platform that will be used.
        * i.	For FCM, select "Google Cloud Messaging (GCM)"
        * ii.	The API Key is the FCM project's Server Key, which was retrieved in step 2bxvi
    * e.	Click "Create platform application"
    * f.	 Add the following credentials to your application. In the AmazonSNS test app, these credentials should be added to the Constants class:
    ```
    String accessKey = "{Access Key Id from step 1k}"
    String secretKey = "{Secret Access Key from step 1k}"
    String platformApplicationArn = "{The PlatformApplicationArn}"
    ```
4.	**Create a topic**
    * a.	This manual step is optional since this can be done programmatically
    * b.	In AWS, go to the Simple Notification Service
    * c.	From the SNS dashboard click "Create topic" or click "Topics" in the side menu and click "Create new topic"
    * d.	Enter a topic name and click "Create topic"
    * e.	Copy the topic ARN and add it to your application if you would like to only use this topic in your application. The AmazonSNS test project requires a topic ARN to be added to the Constants file only in order to run unit tests. 
    * f.	Subscribe your device or email address to this topic in your app as shown in the AmazonSNS app
5.	**Send a message**
    * a.	From the topics page, click on the Topic ARN of the topic that you would like to send a message on
    * b.	Click "Publish to topic"
    * c.	Enter the subject and message of the message you would like to send and click "Publish message"
    * d.	See that the message is sent to the subscribed devices and email addresses
  
### Project Structure
The Amazon SNS sample project contains the following classes:

* **\main\java\intelligentproduct\solutions\activities\AmazonSNSActivity.java**
This class contains the interactions between the test activity and the AWS functions. Here when a button on the activity is clicked, a new AsyncTask will launch to perform the necessary action, such as creating or subscribing to a topic. These functions are performed in AsyncTasks because they must run in a separate thread than the main thread. After the function has completed, a popup will appear on the screen saying if it was successful or not. If an error occurs, more detail on it can be found in the log messages.
* **\main\java\intelligentproduct\solutions\amazonsns\AmazonSNSHelper.java**
This class contains the calls to the Amazon AWS SNS API. These functions will perform the necessary actions for each of the calls and will throw an exception if an error occurs. 
* **\main\java\intelligentproduct\solutions\amazonsns\Constants.java**
This class will contain the constant values which are used throughout the app. Several of these values will need to be entered by the user prior to running this application. The constants are:
    * samplePlatformApplicationArn - This is the platform application ARN of your platform application that was made in AWS. This is described in the **Create a Platform Application in AWS** section of this document
    * sampleAccessKey and sampleSecretKey - These are the credentials needed to access the project in AWS. These values are also created in the **Create a Platform Application in AWS** section of this document
    * testTopicName - This is the name of the topic that will be created in the unit tests. The unit tests will create this topic, test subscribing and unsubscribing to it and then delete the topic. This can be changed to any topic name that is preferred.
    * testTopicArn - This is the ARN of the topic that will be used in the unit tests. A prefix will need to be appended to the testTopicName. The prefix is the same prefix that starts the platform application ARN. Therefore, the testTopicArn will be set up in the following way:
    ```
    public static String testTopicName = "createTopicTest";
    public static String testTopicArn = "arn:aws:sns:us-east-1:123456789098:" + testTopicName;
    ```
    * emailAddressToTest - If you would like to test email subscriptions, enter an email address that you would like to use in the tests here. However, please note that when running the email unsubscribe unit test, the email address that is used in this test must have been subscribed to the topic and must have clicked the "Confirm Subscription" link in the email it received. This test will fail if the subscription was not confirmed prior to running this test
* **\main\java\intelligentproduct\solutions\amazonsns\FirebaseInstanceIDService.java**
This class is needed to use Firebase to receive notifications. This receives a token ID on the initial run of the application or whenever the token needs to be refreshed, possibly due to the security of the previous token being compromised
* **\main\java\intelligentproduct\solutions\amazonsns\FirebaseService.java**
This class catches messages that are receieved from SNS and Firebase. It runs a `onMessageReceived` listener and will display an Android notification when one is received. The device will also play a sound and/or vibrate, if it is not in silent mode.
* **\main\java\intelligentproduct\solutions\amazonsns\SNSAsyncTaskResponse.java**
This class is used to create custom exceptions in the AmazonSNSActivity class.
* **\androidTest\java\intelligentproduct\solutions\amazonsns\AmazonSNSHelperTest.java**
This class containts the unit tests for this application. They test the functions in the AmazonSNSHelper class, such as creating a platform endpoint, creating a topic, subscribing to it for device and email subscriptions, etc. These tests run in alphabetical order based on their names so that they run in a certain sequence.

### Amazon SNS Test Activity
The AmazonSNS app provides an activity for testing the Amazon SNS sample project. 
1.	**Creating a Topic**
The "Create SNS Topic" button will create a new Amazon SNS topic, who's name will have the current date and time in it to ensure that the name is unique. This will return the topic ARN and the app will save it in its Shared Preferences so that it will remember it if the app is closed. The test app only allows one topic to be created in this way at a time. 
2.	**Subscribing to Amazon SNS**
The app can be used to subscribe the device or an email address to the Amazon SNS topic that was created in step 1. This means that any message that is published to the topic will appear on any devices that are subscribed to it as a push notification message. It will also be sent as an email to any email address that is subscribed. In this app, typing an email address into the text box and clicking the "Subscribe email" button will subscribe the entered email address to the app's test topic.
When a device is subscribed, it will immediately begin receiving notifications whenever one is published to the topic. However, after an email address is subscribed, first it will receive an email asking to confirm the subscription. After the confirmation link in this email is clicked, then this address will receive any emails for any message that is published to the topic.
Also, prior to subscribing the device to receive push notifications, a platform endpoint must be created for the device running the application. This endpoint is created with a token that the application receives from FCM and uses the platform application ARN to associate this device with the application that was created in AWS.
3.	**Send a Message with Amazon SNS**
Clicking the "Send message" button will publish a test message to the app's test topic. Any device or email address that is subscribed to the topic will receive it.
4.	**Unsubscribe from Amazon SNS**
The "Unsubscribe device" button will unsubscribe the device from the Amazon SNS topic if it was previously subscribed. The "Unsubscribe email" button will unsubscribe the email address that was typed into the text box at the top of the screen, if that address was previously subscribed. The device or address that is unsubscribed will no longer receive messages that are sent to the app's test topic.
5.	**Delete the topic**
The last step in this app is to delete the topic that was created in step 1. 
6.	**Delete the application's endpoint**
This deletes the application endpoint that was created when the device was subscribed to SNS. 

#### Amazon SNS Library Functions
The AmazonSNSHelper class in the AmazonSNS project implements the following Amazon SNS functions:
```
CreatePlatformEndpointResult createPlatformEndpoint (CreatePlatformEndpointRequest request)
SubscribeResult subscribe(SubscribeRequest request)
UnsubscribeResult unsubscribe(UnsubscribeRequest request)
PublishResult publish(String topicArn, String message, String subject)
CreateTopicResult createTopic(String name)
DeleteTopicResult deleteTopic(String topicArn)
DeleteEndpointResult deleteEndpoint(DeleteEndpointRequest request)
```
For the full list of Amazon SNS functions, see the [documentation for the AmazonSNSClient API](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/sns/AmazonSNSClient.html)
