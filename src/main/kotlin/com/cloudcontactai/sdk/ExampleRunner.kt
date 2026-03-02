package com.cloudcontactai.sdk

import com.cloudcontactai.sdk.common.CCAIConfig
import com.cloudcontactai.sdk.webhook.WebhookRequest
import com.cloudcontactai.sdk.webhook.WebhookUpdateRequest
import com.cloudcontactai.sdk.mms.Account as MMSAccount
import java.io.File

fun main() {
    val config = CCAIConfig(
        clientId = System.getenv("CCAI_CLIENT_ID") ?: throw IllegalArgumentException("CCAI_CLIENT_ID not found"),
        apiKey = System.getenv("CCAI_API_KEY") ?: throw IllegalArgumentException("CCAI_API_KEY not found"),
        useTestEnvironment = true
    )
    
    val ccai = CCAIClient(config)
    
    try {
        runSampleSMS(ccai)
        runSampleEmail(ccai)
        runSampleMMS(ccai)
        runSampleContactDoNotText(ccai)
        runSampleWebhook(ccai)
    } catch (e: Exception) {
        println("Error: ${e.message}")
        e.printStackTrace()
    }

    ccai.close()
    println("\nExample completed successfully!")
}

fun runSampleSMS(ccai: CCAIClient){
    println("=== SMS Examples ===")

    val smsResponse = ccai.sms.sendSingle(
        firstName = "John",
        lastName = "Doe",
        phone = "+15551234567",
        message = "Hello John, this is a test message from CCAI SDK!",
        title = "Test SMS Campaign",
        customData = "{\"myAppCustomId\": \"3c1344d771eb48f99de6846746b2d4a0\"}"
    )
    println("SMS sent with response ID:${smsResponse.responseId} and campaign ID: ${smsResponse.id}")
}

fun runSampleEmail(ccai: CCAIClient){
    println("\n=== Email Examples ===")

    val emailResponse = ccai.email.sendSingle(
        firstName = "John",
        lastName = "Doe",
        email = "john.doe@example.com",
        subject = "Welcome John!",
        htmlContent = """
                <html>
                    <body>
                        <h1>Hello John Doe!</h1>
                        <p>Welcome to our service. We're excited to have you on board!</p>
                        <p>Best regards,<br>The CCAI Team</p>
                    </body>
                </html>
            """.trimIndent(),
        textContent = null
    )
    println("Email sent with response ID:${emailResponse.responseId} and campaign ID: ${emailResponse.id}")
}

fun runSampleMMS(ccai: CCAIClient){
    println("\n=== MMS Examples ===")

    // Example with image file (if available)
    val imageFile = File("test-image.jpg")
    if (imageFile.exists()) {
        val mmsAccounts = listOf(
            MMSAccount("John", "Doe", "+15551234567")
        )
        val mmsResponse = ccai.mms.sendWithImage(
            accounts = mmsAccounts,
            message = "Check out this image!",
            title = "MMS Test Campaign",
            imageFile = imageFile,
            senderPhone = null
        )
        println("MMS sent with response ID:${mmsResponse.responseId} and campaign ID: ${mmsResponse.id}")
    } else {
        println("Skipping MMS example - test-image.jpg not found")
    }
}

fun runSampleContactDoNotText(ccai: CCAIClient){
    println("\n=== Contact Do Not Text Example ===")
    /*println("\nDo Not Text By contact ID")

    var doNotContactResponse = ccai.contact.setDoNotText(
        contactId = "613b086b5d7d4dee0723f7f6",
        doNotText = true
    )
    println("Contact marked as do not text, phone: ${doNotContactResponse.phone}")
    */
    println("\nDo Not Text by phone")

    val doNotContactResponse = ccai.contact.setDoNotText(
        phone = "+12345678901",
        doNotText = true
    )
    println("Contact marked as do not text, contact ID: ${doNotContactResponse.contactId}")
}

fun runSampleWebhook(ccai: CCAIClient){
    println("\n=== Webhook Examples ===")

    //Create a single webhook
    println("\nCreating single webhook")
    val webhookRequest = WebhookRequest("https://your-app.com/webhooks/ccai")
    val webhookResponse = ccai.webhook.create(webhookRequest)
    println("Webhook created with ID: ${webhookResponse.id}")

    //Create multiple webhooks
    println("\nCreating multiple webhooks")
    val webhookList = listOf(
        WebhookRequest("https://your-second.com/webhooks/ccai"),
        WebhookRequest("https://your-other.com/webhooks/ccai")
    )

    val webhookListResponse = ccai.webhook.create(webhookList)
    println("Webhooks created count: ${webhookListResponse.size}")

    //Update webhook url
    println("\nGetting current webhooks")
    val currentWebhooks = ccai.webhook.getAll()
    println("Current webhooks count ${currentWebhooks.size}")
    if(currentWebhooks.isNotEmpty()){
        val updateRequest = WebhookUpdateRequest(
            currentWebhooks.first().id,
            "https://your-app.com/webhooks/updated-ccai"
        )
        val webhookResponse = ccai.webhook.update(updateRequest)
        println("Webhook updated, new Url: ${webhookResponse.url}")
    }else{
        println("There aren't current webhooks configured")
    }

    //Get webhook by ID
    println("\nGetting webhook by ID")
    val appWebhook = ccai.webhook.get(webhookResponse.id)
    println("Webhook with ID: ${webhookResponse.id} has url ${appWebhook.url}")

    //Delete the webhook
    println("\nDeleting webhook")
    val webhookDeleted = ccai.webhook.delete(webhookResponse.id)
    println("Deleted webhook with ID: ${webhookDeleted.id}")
}
