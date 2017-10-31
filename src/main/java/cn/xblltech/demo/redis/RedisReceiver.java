package cn.xblltech.demo.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;



@Component
public class RedisReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisReceiver.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void receiveMessage(String fileMessagess) throws IOException {

        int start = fileMessagess.indexOf("{");
        String substring = fileMessagess.substring(start);
        ObjectMapper mapper = new ObjectMapper();
        FileMessage fileMessages = mapper.readValue(substring, FileMessage.class);
        LOGGER.info(fileMessages.toString());
        MergeThred mergeThred=new MergeThred(fileMessages,redisTemplate);
        new Thread(mergeThred).start();
    }
}

