package com.jimu.monitor.collect;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.jimu.monitor.collect.utils.JsonUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/**
 * Created by yue.liu on 16/5/22.
 */
@Service
public class MonitorFileLoader {

    @Resource
    private Configs configs;

    public List<Group> load() {
        String monitorPath = configs.getMonitorPath();
        final List<File> monitorFiles = Lists.newArrayList();
        SimpleFileVisitor<Path> fileVisitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                monitorFiles.add(file.toFile());
                return super.visitFile(file, attrs);

            }
        };

        Path path = Paths.get(monitorPath);
        List<Group> groupList = Lists.newArrayList();
        try {
            Files.walkFileTree(path, fileVisitor);
            monitorFiles.forEach(file -> loadMonitor(file, groupList));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return groupList;
    }

    private void loadMonitor(File file, List<Group> groupList) {
        if (file.isHidden()) {
            return;
        }
        Group group = JsonUtils.readValueAsFile(file, new TypeReference<Group>() {
        });
        if (group == null) {
            return;
        }
        groupList.add(group);
    }
}
