package com.hrams;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HramsWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(HramsWebApplication.class, args);
        System.out.println("\n==========================================================================");
        System.out.println("  Hostel Room Allocation Management System (HRAMS) Web Portal Ready!      ");
        System.out.println("  Open Web Browser at: http://localhost:8080                               ");
        System.out.println("==========================================================================\n");
    }
}
