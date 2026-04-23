package com.smartcampus;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

// sets the base path for all our REST endpoints
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {
    // no need to override anything, Jersey handles scanning in Main
}
