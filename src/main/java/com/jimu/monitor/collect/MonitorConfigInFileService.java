package com.jimu.monitor.collect;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.jimu.monitor.collect.bean.Group;
import com.jimu.monitor.utils.JsonUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.io.Resources.getResource;
import static com.jimu.monitor.Configs.config;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/**
 * Created by yue.liu on 16/5/22.
 * 现在改成从远程接口读取所有需要拉的数据了, 不再从文件里读取配置
 */
@Slf4j
@Service
@Deprecated
public class MonitorConfigInFileService implements MonitorGroupKeeper {

    private static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);

    private final static String SUFFIX = ".json";

    // groupList需要是一个cow list哦. 别用错了
    @Getter
    private List<Group> groupList = new CopyOnWriteArrayList<>();

    // key: groupKey value: group
    private ConcurrentHashMap<String, Group> keyGroupMap = new ConcurrentHashMap<>();

    // key: filePath value: groupKey
    private ConcurrentHashMap<String, String> fileKeyMap = new ConcurrentHashMap<>();

    // key: groupKey value: filePath
    private ConcurrentHashMap<String, String> keyFileMap = new ConcurrentHashMap<>();

    private WatchService watchService;

    private SimpleFileVisitor<Path> fileVisitor;

    /**
     * 注册一个定时任务, 定期扫描.
     * <p>
     *
     * @throws Exception
     */
    // @PostConstruct
    public void init() throws Exception {

        watchService = FileSystems.getDefault().newWatchService();
        fileVisitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                File file = path.toFile();
                log.info(file.getAbsolutePath());
                if (file.getName().endsWith(SUFFIX) && !file.isHidden()) {
                    Group group = buildGroup(file);
                    if (group != null) {
                        keyGroupMap.put(group.groupKey(), group);
                        keyFileMap.put(group.groupKey(), file.getAbsolutePath());
                        fileKeyMap.put(file.getAbsolutePath(), group.groupKey());
                    }
                }
                return super.visitFile(path, attrs);
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                return FileVisitResult.CONTINUE;
            }
        };

        if (config.isMonitorAutoRefresh()) {
            log.info("monitor auto refresh. register watchService");
            fixedThreadPool.execute(new FileChangeListener(watchService));
        }

        calcLatestGroups();
        log.info("end init monitorFileAutoLoaderService");
    }

    private void calcLatestGroups() throws IOException {
        log.info("start to calculate latest groups. path:{}", config.getMonitorPath());

        Path path = Paths.get(config.getMonitorPath());
        try {
            Files.walkFileTree(path, fileVisitor);
        } catch (IOException e) {
            log.error("init monitorFileAutoLoaderService error", e);
        }

        updateGroupList();

        log.info("finish calculating latest groups, size:{}", groupList.size());
    }

    public void reload() throws IOException {
        keyGroupMap = new ConcurrentHashMap<>();
        fileKeyMap = new ConcurrentHashMap<>();
        keyFileMap = new ConcurrentHashMap<>();

        calcLatestGroups();
    }

    /**
     * 响应文件变化
     *
     * @param file 文件
     * @param kind ENTRY_CREATE,ENTRY_DELETE,ENTRY_MODIFY
     */
    public void notifyFileChange(File file, WatchEvent.Kind<?> kind) {
        String path = file.getAbsolutePath();
        if (file == null || !file.getName().endsWith(SUFFIX) || file.isHidden()) {
            return;
        }
        if (OVERFLOW == kind) {
            log.warn("current file kind is OVERFLOW, file: {}", path);
            return;
        }
        log.info("file change, file: {}, kind: {}", path, kind);
        if (ENTRY_CREATE == kind) {
            handleFileOperation(file, Operation.ADD);
        } else if (ENTRY_MODIFY == kind) {
            handleFileOperation(file, Operation.MODIFY);
        } else {
            handleFileOperation(file, Operation.DEL);
        }
    }

    /**
     * 处理文件操作
     * 
     * @param file 文件
     * @param operation
     * @throws RuntimeException
     */
    private void handleFileOperation(File file, Operation operation) throws RuntimeException {
        if (file == null) {
            return;
        }

        String filePath = file.getAbsolutePath();
        String groupKey = "";
        Group group = null;
        if (Operation.DEL != operation) {
            group = buildGroup(file);
            if (group == null) {
                log.warn("group is null, file:{}, kind:{}", filePath, operation.name());
                return;
            }
            groupKey = group.groupKey();
        }

        switch (operation) {
        case ADD: // 新增文件
            if (keyFileMap.contains(groupKey)) {
                throw new RuntimeException("group key has existed, cannot create!");
            }
            log.info("add file, file:{}, groupKey:{}", filePath, groupKey);
            keyFileMap.put(groupKey, filePath);
            keyGroupMap.put(groupKey, group);
            fileKeyMap.put(filePath, groupKey);
            break;
        case MODIFY: // 修改文件
            String originGroupKey = fileKeyMap.get(filePath);
            if (originGroupKey != null && !originGroupKey.equals(groupKey)) {
                // groupKey变了， 添加新的(同时新的key不能重复), 并删除原有的
                if (keyFileMap.contains(groupKey)) {
                    throw new RuntimeException("group key has existed, cannot modify!");
                }
                removeByFile(filePath);
            }
            log.info("modify file, file:{}, groupKey:{}", filePath, groupKey);
            keyFileMap.put(groupKey, filePath);
            keyGroupMap.put(groupKey, group);
            fileKeyMap.put(filePath, groupKey);
            break;
        case DEL: // 删除文件
            log.info("delete file, file:{}", filePath);
            removeByFile(filePath);
            fileKeyMap.remove(filePath);
            break;
        default:
            ;
        }
    }

    private void removeByFile(String filePath) {
        String originKey = fileKeyMap.get(filePath);
        if (StringUtils.isBlank(originKey)) {
            return;
        }
        log.info("remove key {}", originKey);
        keyGroupMap.remove(originKey);
        keyFileMap.remove(originKey);
    }

    /**
     * 读取文件里的值, 返回一个group
     *
     * @param file
     */
    private Group buildGroup(File file) {
        return JsonUtils.readValueAsFile(file, new TypeReference<Group>() {
        });
    }

    /**
     * 更新最新的group list
     */
    private void updateGroupList() {
        if (MapUtils.isEmpty(keyGroupMap)) {
            groupList = Lists.newCopyOnWriteArrayList();
        }
        groupList = new CopyOnWriteArrayList<>(keyGroupMap.values());
    }

    enum Operation {
        ADD, MODIFY, DEL
    }

    class FileChangeListener implements Runnable {
        private WatchService service;

        public FileChangeListener(WatchService service) {
            this.service = service;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    WatchKey key = service.take();
                    if (key != null) {
                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();
                            Path folder = (Path) key.watchable();
                            Path filename = (Path) event.context();
                            Path child = folder.resolve(filename);
                            notifyFileChange(child.toFile(), kind);
                        }
                        updateGroupList();
                        if (!key.reset()) {
                            break;
                        }
                    }
                }
                service.close();
            } catch (InterruptedException | IOException e) {
                log.error("file change listener meet exception", e);
                Thread.currentThread().interrupt();
            }
        }
    }

}
