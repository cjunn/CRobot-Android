package com.crobot.debug.file;

import com.crobot.core.infra.tool.FileOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class FileRefresher {

    private String normalUrl(String path) {
        return path.replace("\\", "/");
    }

    public List<String> invoke(FileOperation operation, List<FileModify> fileList) {
        List<String> removeFiles = new ArrayList<>();
        List<String> addFiles = new ArrayList<>();
        List<FileModify> clientFileList = fileList.stream().map(k -> {
            k.setPath(normalUrl(k.getPath()));
            return k;
        }).collect(Collectors.toList());
        Map<String, FileModify> clientFileCache = clientFileList.stream()
                .collect(Collectors.toMap(k -> k.getPath(), k -> k, (k1, k2) -> k1));
        List<String> serverFileList = operation.listFiles();
        Map<String, String> serverFileCache = serverFileList.stream().collect(Collectors.toMap(k -> k, k -> operation.md5(k), (k1, k2) -> k1));
        serverFileList.forEach(path -> {
            //不在客户端里，需要移除
            FileModify clientFile = clientFileCache.get(path);
            if (clientFile == null) {
                removeFiles.add(path);
                return;
            }
            //MD5不一致，需要移除
            if (!Objects.equals(clientFile.getMd5(), serverFileCache.get(path))) {
                removeFiles.add(path);
                return;
            }
        });
        removeFiles.forEach(path -> operation.remove(path));
        clientFileList.forEach(file -> {
            String md5 = serverFileCache.get(file.getPath());
            //不在服务端,需要新增
            if (md5 == null) {
                addFiles.add(file.getPath());
                return;
            }
            if (!Objects.equals(md5, file.getMd5())) {
                addFiles.add(file.getPath());
                return;
            }
        });
        return addFiles;
    }
}
