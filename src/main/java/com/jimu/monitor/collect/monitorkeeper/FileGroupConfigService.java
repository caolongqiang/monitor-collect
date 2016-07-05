package com.jimu.monitor.collect.monitorkeeper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.jimu.monitor.Configs;
import com.jimu.monitor.collect.bean.Group;
import com.jimu.monitor.utils.JsonUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.io.Resources.getResource;
import static com.jimu.monitor.Configs.config;

/**
 * Created by yue.liu on 16/5/22.
 *
 * 现在改成主要从远程接口读取所有需要拉的数据了, 只有极少的配置(主要是kvm的配置),需要从文件里读取配置
 */
@Slf4j
@Service
public class FileGroupConfigService implements MonitorGroupKeeper {

    private final static String SUFFIX = ".json";

    @Getter
    private volatile List<Group> groupList;

    private Path path;

    @PostConstruct
    public void init() throws URISyntaxException {
        path = Paths.get(config.getMonitorPath());

        reload();
    }

    /**
     * 重新reload文件夹下的文件
     */
    public void reload() {
        final List<File> monitorFiles = Lists.newArrayList();
        SimpleFileVisitor<Path> fileVisitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                monitorFiles.add(file.toFile());
                return super.visitFile(file, attrs);

            }
        };
        List<Group> groups = Lists.newArrayList();
        try {
            Files.walkFileTree(path, fileVisitor);
            groups = monitorFiles.stream().filter(file -> file.getName().endsWith(SUFFIX) && !file.isHidden())
                    .map(file -> fillGroupList(file)).filter(group -> group != null).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        groupList = groups;
    }

    /**
     * 读取文件里的值, 返回一个group
     *
     * @param file
     */
    private Group fillGroupList(File file) {
        return JsonUtils.readValueAsFile(file, new TypeReference<Group>() {
        });
    }

}
