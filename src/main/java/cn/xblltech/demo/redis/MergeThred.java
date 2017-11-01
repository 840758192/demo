package cn.xblltech.demo.redis;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class MergeThred implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MergeThred.class);
    private FileMessage fileMessages;
    private RedisTemplate<String, Object> redisTemplate;
    public MergeThred(FileMessage fileMessages,RedisTemplate<String, Object> redisTemplate) {
        this.fileMessages = fileMessages;
        this.redisTemplate=redisTemplate;
    }
    @Override
    public synchronized void run() {
        try {
            File newFile = new File(fileMessages.getName());
            long size = 0;
            int i = 1;
            for (; i <= fileMessages.getTotalChunks(); i++) {
                Set<Object> objects = redisTemplate.boundZSetOps(fileMessages.getIdentifier()).rangeByScore(i, i);
                if (!objects.isEmpty()) {
                    redisTemplate.boundZSetOps(fileMessages.getIdentifier()).removeRangeByScore(i, i);
                    Iterator<Object> iterator = objects.iterator();
                    while (iterator.hasNext()) {
                        Object next = iterator.next();
                        if (next instanceof byte[]) {
                            FileUtils.writeByteArrayToFile(newFile, (byte[]) next, true);
                            size += ((byte[]) next).length;
                            LOGGER.info("文件大小：" + size + ",每块数据大小:" + ((byte[]) next).length + ",文件块数:" + i);
                        }
                    }
                } else {
                    i--;
                }
            }
        } catch (Exception e) {
            redisTemplate.delete(fileMessages.getIdentifier());
            e.printStackTrace();
        }
    }
}
