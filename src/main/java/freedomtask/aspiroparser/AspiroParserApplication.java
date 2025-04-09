package freedomtask.aspiroparser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AspiroParserApplication {

  public static void main(String[] args) {
    SpringApplication.run(AspiroParserApplication.class, args);
  }

}
