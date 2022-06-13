package kr.co.abby.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	// localhost:8000/img로 요청이들어오면 
	private String connectPath = "/img/**";
	// 아래 경로로 보낸다
    private String resourcePath = "file:///D:/devfolder/images/cjgourmet/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(connectPath)
                .addResourceLocations(resourcePath);
    }

}
