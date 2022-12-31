package io.github.skunkworks1983.timeclock.ui;

import com.google.inject.AbstractModule;

public class UiModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(MainListRefresher.class).to(MainWindow.class);
    }
}
