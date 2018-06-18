package com.eventViewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@SpringBootApplication
public class EventViewerAPIApplication {

	public static void main(String args[]) {
		SpringApplication.run(EventViewerAPIApplication.class);
	}

	
	@Bean
	public JpaVendorAdapter jpaVendorAdapter(){
	    HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
	    jpaVendorAdapter.setGenerateDdl(true);
	    jpaVendorAdapter.setShowSql(true);

	    return jpaVendorAdapter;
	}
}
