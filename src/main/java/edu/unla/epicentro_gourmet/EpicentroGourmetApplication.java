package edu.unla.epicentro_gourmet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class EpicentroGourmetApplication {

	public static void main(String[] args) {
		SpringApplication.run(EpicentroGourmetApplication.class, args);
	}
}
