package com.cloudcontactai.sdk

import com.cloudcontactai.sdk.common.CCAIConfig
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
            title = "Test SMS Campaign",
            customData = "{\"myAppCustomId\": \"3c1344d771eb48f99de6846746b2d4a0\"}"
        )
        println("SMS sent with response ID:${smsResponse.responseId} and campaign ID: ${smsResponse.id}")
        
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
    } catch (e: Exception) {
        println("Error: ${e.message}")
        e.printStackTrace()
    }
    
    ccai.close()
    println("\nExample completed successfully!")
}
