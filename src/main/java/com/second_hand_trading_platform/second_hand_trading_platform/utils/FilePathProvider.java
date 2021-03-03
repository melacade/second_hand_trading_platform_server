package com.second_hand_trading_platform.second_hand_trading_platform.utils;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilePathProvider {
    private static String imgPath;
    public static String getImgPath(){
        if(imgPath == null){
            FileSystem fileSystem = FileSystems.getDefault();
            Iterable<Path> rootDirectories = fileSystem.getRootDirectories();
            List<String> list = new ArrayList<>();
            for (Path rootDirectory : rootDirectories) {
                File file = rootDirectory.toFile();
                if(file.canWrite()){
                    list.add(file.getAbsolutePath());
                }
            }
            Collections.sort(list);
            imgPath = list.get(list.size()-1).substring(0, list.size()-1)+"/server/images/";
        }
        return imgPath;
    }
}
