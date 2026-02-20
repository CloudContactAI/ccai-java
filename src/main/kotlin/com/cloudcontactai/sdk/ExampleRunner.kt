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
            """.trimIndent(),
            textContent = null
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
                imageFile = imageFile,
                senderPhone = null
            )
            val responseId = mmsResponse.campaignId ?: mmsResponse.id
            println("MMS sent with ID: $responseId")
        } else {
            println("Skipping MMS example - test-image.jpg not found")
        }
    } catch (e: Exception) {
        println("Error: ${e.message}")
        e.printStackTrace()
    }
    
    ccai.close()
    println("\nExample completed successfully!")
}
