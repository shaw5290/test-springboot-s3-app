package com.example.simple_storage_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Arrays;

@SpringBootApplication
public class SimpleStorageAppApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SimpleStorageAppApplication.class);

		// Add listener to print all environment configurations before the database
		// initializes
		app.addListeners((ApplicationListener<ApplicationEnvironmentPreparedEvent>) event -> {
			ConfigurableEnvironment env = event.getEnvironment();
			System.out.println("\n============================================================================");
			System.out.println("STARTUP: PRINTING ALL ENVIRONMENT CONFIGURATIONS");
			System.out.println("============================================================================");

			for (PropertySource<?> source : env.getPropertySources()) {
				if (source instanceof EnumerablePropertySource) {
					EnumerablePropertySource<?> enumerablePropertySource = (EnumerablePropertySource<?>) source;
					System.out.println("\n>>> Source: " + enumerablePropertySource.getName());

					String[] names = enumerablePropertySource.getPropertyNames();
					Arrays.sort(names); // Sort for readability

					for (String name : names) {
						String value = String.valueOf(env.getProperty(name));

						// Mask sensitive credentials
						String lowerName = name.toLowerCase();
						if (lowerName.contains("password") ||
								lowerName.contains("secret") ||
								lowerName.contains("key") ||
								lowerName.contains("token")) {
							value = "********";
						}

						System.out.println("    " + name + " = " + value);
					}
				}
			}
			System.out.println("\n============================================================================");
		});

		app.run(args);
	}

}
