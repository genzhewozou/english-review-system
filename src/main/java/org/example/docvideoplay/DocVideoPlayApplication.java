package org.example.docvideoplay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class DocVideoPlayApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocVideoPlayApplication.class, args);
	}

}
