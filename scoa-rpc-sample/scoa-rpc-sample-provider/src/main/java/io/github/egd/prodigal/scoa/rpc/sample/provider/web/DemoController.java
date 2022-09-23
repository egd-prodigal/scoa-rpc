package io.github.egd.prodigal.scoa.rpc.sample.provider.web;

import com.google.gson.Gson;
import io.github.egd.prodigal.scoa.rpc.dto.User;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HeaderParam;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

@RestController
public class DemoController {


    @RequestMapping("/demo")
    public Map<String, Object> demo() {
        return Collections.singletonMap("greetings", "hello");
    }

    @RequestMapping("/scoa-rpc")
    public User scoaRpc(HttpServletRequest request) throws IOException {
        String scoaProvider = request.getHeader("scoa-provider");
        System.out.println(scoaProvider);
        ServletInputStream inputStream = request.getInputStream();
        String s = StreamUtils.copyToString(inputStream, Charset.defaultCharset());
        Gson gson = new Gson();
        Map map = gson.fromJson(s, Map.class);

        User user = new User();
        user.setUsername("yeemin");
        user.setEmail("yeeminshon@outlook.com");
        return user;
    }

}
