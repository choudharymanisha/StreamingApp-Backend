package com.stream.app.spring_stream_backend;

import com.stream.app.spring_stream_backend.services.VideoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringStreamBackendApplicationTests {
	@Autowired
VideoService videoService;
	@Test
	void contextLoads() {
		videoService.processVideo("9d246f6f-e16c-408d-bd06-95a2da088c2d", null);
	}

}
