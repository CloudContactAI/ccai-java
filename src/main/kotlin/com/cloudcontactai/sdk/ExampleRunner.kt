package com.cloudcontactai.sdk

import com.cloudcontactai.sdk.common.CCAIConfig
import com.cloudcontactai.sdk.sms.Account
import com.cloudcontactai.sdk.email.EmailAccount
import com.cloudcontactai.sdk.webhook.WebhookRequest
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
        println("=== SMS Examples ===")
        
        val smsResponse = ccai.sms.sendSingle(
            firstName = "John",
            lastName = "Doe",
            phone = "+15551234567",
            message = "Hello John, this is a test message from CCAI SDK!",
            title = "Test SMS Campaign"
        )
        println("SMS sent with ID: ${smsResponse.id}")
        
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
            """.trimIndent()
        )
        println("Email sent with ID: ${emailResponse.id}")
        
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
                imageFile = imageFile
            )
            println("MMS sent with ID: ${mmsResponse.campaignId}")
        } else {
            println("Skipping MMS example - test-image.jpg not found")
        }
        
        println("\n=== Webhook Examples ===")
        
        val webhookRequest = WebhookRequest(
            url = "https://your-app.com/webhooks/ccai",
            events = listOf("sms.sent", "sms.delivered", "email.opened", "email.clicked"),
            isActive = true,
            secret = "your-webhook-secret"
        )
        
        val webhook = ccai.webhook.create(webhookRequest)
        println("Webhook created with ID: ${webhook.id}")
        
        // Demonstrate signature validation
        val testPayload = """{"eventType":"sms.sent","messageId":"123"}"""
        val testSignature = "test-signature"
        val isValid = ccai.webhook.validateWebhookSignature(testPayload, testSignature, "your-webhook-secret")
        println("Webhook signature validation: $isValid")
        
    } catch (e: Exception) {
        println("Error: ${e.message}")
        e.printStackTrace()
    }
    
    ccai.close()
    println("\nExample completed successfully!")
}
