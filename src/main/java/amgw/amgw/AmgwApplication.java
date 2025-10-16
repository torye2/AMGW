package amgw.amgw;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("amgw.amgw.mapper")
public class AmgwApplication {

    public static void main(String[] args) {
        SpringApplication.run(AmgwApplication.class, args);
    }

}
