package cl.rinconsur.sesiones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SesionesApplication {
    public static void main(String[] args) {
        SpringApplication.run(SesionesApplication.class, args);
    }
}
