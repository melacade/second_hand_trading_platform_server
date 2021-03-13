package com.second_hand_trading_platform.second_hand_trading_platform.controller;

import com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.output.ApiResult;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.UserBaseInfo;
import com.second_hand_trading_platform.second_hand_trading_platform.service.UserService;
import com.second_hand_trading_platform.second_hand_trading_platform.utils.FilePathProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.security.sasl.AuthenticationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController()
@RequestMapping("/file")
public class FileController {
    @Autowired
    UserService suerService;


    @PostMapping("/upload/{type}/{action}")
    public ApiResult fileUpload(@RequestParam(value = "files") MultipartFile[] files, @PathVariable String type, @PathVariable String action) throws IOException {
        //验证用户
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        if(authentication == null){
            throw new AuthenticationException("请求被服务器拒绝");
        }
        List<String> res = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                log.debug("文件不存在");
                throw new IOException("上传文件不合法");
            }
            String fileName = file.getOriginalFilename();  // 文件名
            String suffixName = fileName.substring(fileName.lastIndexOf("."));  // 后缀名
            boolean valid = false;
            //验证文件
            if("img".equals(type)){
                valid =  suffixName.equalsIgnoreCase(".png") ||
                        suffixName.equalsIgnoreCase(".jpg") || suffixName.equalsIgnoreCase(".jpeg");
            }
            if(!valid){
                log.debug("上传文件不合法");
                throw new IOException("上传文件不合法");
            }

            // 处理文件
            String filePath = FilePathProvider.getImgPath(); // 上传后的路径
            fileName = UUID.randomUUID().toString().replaceAll("-","") + suffixName; // 新文件名
            File dest = new File(filePath + fileName);
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            try {
                file.transferTo(dest);
            } catch (IOException e) {
                log.debug(e.getMessage());
                return ApiResult.fail("上传失败");
            }
            String filename = "/img/" + fileName;
            res.add(filename);

        }
        if(res.size() == 0){
            return ApiResult.fail("上传失败！");
        }
        if("avatar".equals(action)){
            String filename = res.get(0);

            UserBaseInfo userBaseInfo = suerService.updateAvatar(filename, authentication);
            if (userBaseInfo == null) {
                return ApiResult.fail("头像更新失败！");
            }
            return ApiResult.ok("更新成功",userBaseInfo);
        }
        return ApiResult.ok("上传成功",res);
    }
}
