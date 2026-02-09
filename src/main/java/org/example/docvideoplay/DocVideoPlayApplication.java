package org.example.docvideoplay;

import org.example.docvideoplay.config.ForeignKeyRemovalMigration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class DocVideoPlayApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(DocVideoPlayApplication.class);
		application.addInitializers(new ForeignKeyRemovalMigration());
		application.run(args);
	}

}
