package Esame.Back_End.Esame.Back_End;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EsameBackEndApplication {

	public static void main(String[] args) {
		SpringApplication.run(EsameBackEndApplication.class, args);
	}

}
