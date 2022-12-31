package io.github.skunkworks1983.timeclock.controller;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.util.Providers;

import java.io.File;

public class ControllerModule extends AbstractModule
{
    private String awsCredPath = null;
    
    public ControllerModule(String awsCredPath)
    {
        this.awsCredPath = awsCredPath;
    }
    
    @Override
    protected void configure()
    {
        if(awsCredPath != null)
        {
            try
            {
                JsonNode node = new ObjectMapper().readTree(new File(awsCredPath));
                BasicAWSCredentials awsCredentials = new BasicAWSCredentials(node.get("accessKey").asText(),
                                                                             node.get("secretKey").asText());
                bind(AWSCredentials.class).toInstance(awsCredentials);
            }
            catch(Exception e)
            {
                bind(AWSCredentials.class).toProvider(Providers.of(null));
            }
        }
        else
        {
            bind(AWSCredentials.class).toProvider(Providers.of(null));
        }
    }
}
