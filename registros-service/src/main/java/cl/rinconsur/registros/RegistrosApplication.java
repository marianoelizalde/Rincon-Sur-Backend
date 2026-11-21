package cl.rinconsur.registros;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class RegistrosApplication {
    public static void main(String[] args) {
        SpringApplication.run(RegistrosApplication.class, args);
    }
}
