/**
 * Component 06: Configuration & Security
 * @author Sithika (it25103442@my.sliit.lk)
 */
package com.rental.sandaru;

import com.rental.nichala.*;
import com.rental.abhishek.*;
import com.rental.rashdeen.*;
import com.rental.nisal.*;
import com.rental.sandaru.*;
import com.rental.sithika.*;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import java.nio.file.Paths;

// ============================================================
// WebConfig — Adds "data/images/" as a runtime static resource
// location so that uploaded vehicle images are served correctly.
//
// Spring Boot only serves classpath:/static/ by default, which
// is resolved at startup from the compiled classpath. Files
// written to disk at runtime (host image uploads) are NOT in
// the classpath and will never be served without this config.
//
// This handler maps:  GET /images/{filename}
//   → checks classpath:/static/images/ first (built-in images)
//   → then checks data/images/             (runtime uploads)
// ============================================================
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Resolve the absolute URI for the upload folder (data/images/)
        String uploadDir = Paths.get("data/images").toAbsolutePath().toUri().toString();
        if (!uploadDir.endsWith("/")) {
            uploadDir = uploadDir + "/";
        }

        // Map /uploads/** to data/images/ on disk.
        // We use /uploads/ (not /images/) to avoid conflicting with Spring Boot's
        // default classpath:/static/images/ handler which takes priority.
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadDir)
                .setCachePeriod(0); // No cache during development
    }
}

// Final configuration checked and verified for viva presentation.
