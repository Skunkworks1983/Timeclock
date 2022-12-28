package io.github.skunkworks1983.timeclock.ui;

public class AlertMessage
{
    private boolean success;
    private String message;
    private Runnable okButtonCallback;
    
    public AlertMessage(boolean success, String message)
    {
        this(success, message, null);
    }
    
    public AlertMessage(boolean success, String message, Runnable okButtonCallback)
    {
        this.success = success;
        this.message = message;
        this.okButtonCallback = okButtonCallback;
    }
    
    public boolean isSuccess()
    {
        return success;
    }
    
    public String getMessage()
    {
        return message;
    }
    
    public Runnable getOkButtonCallback()
    {
        return okButtonCallback;
    }
}
