package com.cloudcontactai.sdk.examples;

import com.cloudcontactai.sdk.CCAIClient;
import com.cloudcontactai.sdk.campaigns.CampaignRequest;
import com.cloudcontactai.sdk.campaigns.CampaignResponse;
import com.cloudcontactai.sdk.common.CCAIConfig;

import java.util.Arrays;
import java.util.List;

/**
 * Campaign registration example using the CCAI Java SDK
 */
public class BasicCampaignExample {

    public static void main(String[] args) {
        CCAIConfig config = new CCAIConfig(
            System.getenv("CCAI_CLIENT_ID"),
            System.getenv("CCAI_API_KEY"),
            false
        );

        CCAIClient ccai = new CCAIClient(config);

        try {
            // Create a campaign (assumes brand ID 1 exists)
            System.out.println("Creating a campaign...");
            CampaignResponse campaign = ccai.getCampaigns().create(new CampaignRequest(
                1L,                          // brandId
                "MIXED",                     // useCase
                Arrays.asList("CUSTOMER_CARE", "TWO_FACTOR_AUTHENTICATION", "ACCOUNT_NOTIFICATION"),
                "This campaign handles security codes and support for Collect.org.",
                "Users opt-in via our signup form checkbox at https://collect.org/signup",
                "https://collect.org/terms",
                "https://collect.org/privacy",
                true,                        // hasEmbeddedLinks
                false,                       // hasEmbeddedPhone
                false,                       // isAgeGated
                false,                       // isDirectLending
                Arrays.asList("START", "JOIN"),
                "Welcome to Collect.org! Msg&Data rates may apply. Reply STOP to cancel.",
                "https://collect.org/images/opt-in-proof.png",
                Arrays.asList("HELP", "INFO"),
                "Collect.org: For help email support@collect.org. Reply STOP to cancel.",
                Arrays.asList("STOP", "UNSUBSCRIBE"),
                "Collect.org: You have been unsubscribed. STOP received.",
                Arrays.asList(
                    "Your Collect.org security code is 554321. Reply STOP to cancel.",
                    "Hi [Name], your ticket #[ID] has been updated. Reply HELP for more info."
                )
            ));
            System.out.println("Campaign created with ID: " + campaign.getId());

            // Get campaign by ID
            System.out.println("\nFetching campaign by ID...");
            CampaignResponse fetched = ccai.getCampaigns().get(campaign.getId());
            System.out.println("Campaign: " + fetched.getUseCase() + ", Brand: " + fetched.getBrandId());

            // List all campaigns
            System.out.println("\nListing all campaigns...");
            CampaignResponse[] campaigns = ccai.getCampaigns().list();
            System.out.println("Found " + campaigns.length + " campaign(s)");

            // Update a campaign
            System.out.println("\nUpdating campaign...");
            CampaignResponse updated = ccai.getCampaigns().update(campaign.getId(), new CampaignRequest(
                null, null, null,
                "Updated campaign description for Collect.org messaging.",
                null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                Arrays.asList(
                    "Your Collect.org code is 123456. Reply STOP to opt-out.",
                    "Your support ticket has been resolved. Reply HELP for more info.",
                    "Your payment of $50.00 was received. Reply STOP to cancel."
                )
            ));
            System.out.println("Campaign updated: " + updated.getDescription());

            // Delete a campaign
            System.out.println("\nDeleting campaign...");
            ccai.getCampaigns().delete(campaign.getId());
            System.out.println("Campaign deleted successfully");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
