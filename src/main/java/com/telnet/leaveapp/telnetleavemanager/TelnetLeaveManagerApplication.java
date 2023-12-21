package com.telnet.leaveapp.telnetleavemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TelnetLeaveManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TelnetLeaveManagerApplication.class, args);
	}

}
