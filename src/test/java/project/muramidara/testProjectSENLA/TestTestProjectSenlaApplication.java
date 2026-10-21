package project.muramidara.testProjectSENLA;

import org.springframework.boot.SpringApplication;

public class TestTestProjectSenlaApplication {

	public static void main(String[] args) {
		SpringApplication.from(TestProjectSenlaApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
