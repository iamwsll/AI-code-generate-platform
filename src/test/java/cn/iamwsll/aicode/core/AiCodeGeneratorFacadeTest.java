package cn.iamwsll.aicode.core;

import cn.iamwsll.aicode.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;
    @Test
    void generateAndSaveCode() {
        File file = aiCodeGeneratorFacade.generateAndSaveCode("生成一个登录页面", CodeGenTypeEnum.MULTI_FILE,1L);
        Assertions.assertTrue(file.exists());
    }

    @Test
    void generateAndSaveCodeStream() {
        Flux<String> result = aiCodeGeneratorFacade.generateAndSaveCodeStream("生成一个登录页面,尽可能短", CodeGenTypeEnum.MULTI_FILE,1L);
        //阻塞等待所有数据输出完成
//        result.subscribe(System.out::println);
//        Assertions.assertNotNull(result);
        List<String> list = result.collectList().block();
        Assertions.assertNotNull(list);
        String completeContent = String.join(" ", list);
        Assertions.assertNotNull(completeContent);
    }
}