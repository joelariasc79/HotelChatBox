package com.hotel.chatbox.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class I18nConfig {

 @Bean
 public MessageSource messageSource() {
     ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
     messageSource.setBasenames("i18n/messages"); // points to src/main/resources/i18n/messages.properties
     messageSource.setDefaultEncoding("UTF-8");
     return messageSource;
 }
}
