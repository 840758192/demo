package cn.xblltech.demo;


import cn.xblltech.demo.redis.FileMessage;
import cn.xblltech.demo.redis.RedisReceiver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author
 */
@SpringBootApplication
@RestController
public class UploadApplication {

    public static void main(String[] args) {
        SpringApplication.run(UploadApplication.class);
        /*try {
            byte[] bytes = FileUtils.readFileToByteArray(new File("1.jpg"));
            String s = Base64.getEncoder().encodeToString(bytes);
            byte[] digest = DigestUtils.getSha1Digest().digest(bytes);
            String str = Hex.encodeHexString(digest);
            System.out.println(str);
            byte[] byte2 = FileUtils.readFileToByteArray(new File("2.txt"));
            System.out.println(Hex.encodeHexString(DigestUtils.getSha1Digest().digest(Base64.getEncoder().encode(bytes))));

            System.out.println(s);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory redisConnectionFactory)
            throws UnknownHostException {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            MessageListenerAdapter listenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("chat"));
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(RedisReceiver redisReceiver) {
        return new MessageListenerAdapter(redisReceiver, "receiveMessage");
    }
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisReceiver.class);
    @RequestMapping("/upload")
    @CrossOrigin
    @PostMapping
    public void upload(@RequestParam("file") MultipartFile file, @RequestParam("chunkNumber") Double chunkNumber,
                       @RequestParam("chunkSize") String chunkSize, @RequestParam("identifier") String identifier,
                       @RequestParam("totalChunks") Long totalChunks) {
        LOGGER.info("文件名:"+file.getOriginalFilename()+",文件块"+chunkNumber,"文件大小:"+chunkSize+",redis文件名:"+identifier+",一共"+totalChunks+"块");
        try {
            redisTemplate.boundZSetOps(identifier).add(file.getBytes(), chunkNumber);
            if(chunkNumber.intValue()==1){
                    ObjectMapper mapper = new ObjectMapper();
                    FileMessage fileMessage = new FileMessage();
                    fileMessage.setName(file.getOriginalFilename());
                    fileMessage.setTotalChunks(totalChunks);
                    fileMessage.setIdentifier(identifier);
                    fileMessage.setChunkNumber(chunkNumber.longValue());
                    fileMessage.setChunkSize(chunkSize);
                    String fileMessages = mapper.writeValueAsString(fileMessage);
                    redisTemplate.convertAndSend("chat",fileMessages);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
