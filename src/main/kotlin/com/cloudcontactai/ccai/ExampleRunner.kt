package com.cloudcontactai.ccai

import com.cloudcontactai.ccai.common.CCAIConfig
import com.cloudcontactai.ccai.webhook.WebhookRequest

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
            message = "Hello \${firstName}, this is a test message from CCAI JAVA SDK!",
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

        println("\n=== Webhook Examples ===")
        
        val webhookRequest = WebhookRequest(
            url = "https://your-app.com/webhooks/ccai",
            events = listOf("sms.sent", "sms.delivered", "email.opened", "email.clicked"),
            isActive = true
        )
        
        val webhook = ccai.webhook.create(webhookRequest)
        println("Webhook created with ID: ${webhook.id}")

    } catch (e: Exception) {
        println("Error: ${e.message}")
        e.printStackTrace()
    }
    
    ccai.close()
    println("\nExample completed successfully!")
}
