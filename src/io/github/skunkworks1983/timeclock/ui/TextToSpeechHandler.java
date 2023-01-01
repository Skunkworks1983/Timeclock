package io.github.skunkworks1983.timeclock.ui;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

import java.util.Arrays;

@Singleton
public class TextToSpeechHandler
{
    private final Object voiceLock;
    
    private Thread speechThread;
    private Voice voice;
    
    @Inject
    public TextToSpeechHandler()
    {
        voiceLock = new Object();
        
        System.setProperty("freetts.voices",
                           "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
    }
    
    public void speak(String text)
    {
        if(speechThread != null && speechThread.isAlive())
        {
            try
            {
                speechThread.join(2000);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
            if(speechThread.isAlive())
            {
                speechThread.interrupt();
                voice.deallocate();
            }
        }
        
        speechThread = new Thread(() -> {
            synchronized(voiceLock)
            {
                VoiceManager voiceManager = VoiceManager.getInstance();
                voice = voiceManager.getVoice("kevin16");
    
                /* Allocates the resources for the voice.
                 */
                voice.allocate();
    
                /* Synthesize speech.
                 */
                voice.speak(text);
    
                /* Clean up and leave.
                 */
                voice.deallocate();
            }
        });
        speechThread.start();
    }
}
