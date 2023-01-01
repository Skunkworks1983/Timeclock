package io.github.skunkworks1983.timeclock.controller;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.util.Providers;
import org.apache.commons.io.IOUtils;

import java.nio.charset.Charset;

public class ControllerModule extends AbstractModule
{
    
    public ControllerModule()
    {
    }
    
    @Override
    protected void configure()
    {
        try
        {
            JsonNode node = new ObjectMapper().readTree(IOUtils.resourceToString("awsCreds.json",
                                                                                 Charset.defaultCharset(),
                                                                                 ClassLoader.getSystemClassLoader()));
            BasicAWSCredentials awsCredentials = new BasicAWSCredentials(node.get("accessKey").asText(),
                                                                         node.get("secretKey").asText());
            bind(AWSCredentials.class).toInstance(awsCredentials);
        }
        catch(Exception e)
        {
            System.err.println("Failed to deserialize awsCreds.json from resources: " + e.getMessage());
            bind(AWSCredentials.class).toProvider(Providers.of(null));
        }
    }
}
