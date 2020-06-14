package org.csu.csumall.service.impl;

import com.google.common.collect.Lists;
import org.csu.csumall.service.IFileService;
import org.csu.csumall.utils.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@Service("fileService")
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public String upLoadFile(MultipartFile file, String path) {
        String filename = file.getOriginalFilename();
        //获取扩展名
        String fileExtensionName = filename.substring(filename.lastIndexOf(".")+1);
        String uploadFilename = UUID.randomUUID().toString() + "." + fileExtensionName;
        logger.info("开始上传文件，上传文件名为{}，上传路径为{}，新文件名为{}",filename, path, uploadFilename);

        File fileDir = new File(path);
        if(!fileDir.exists()){
            //获取写入权限
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(path,uploadFilename);

        try{
            file.transferTo(targetFile);
            //上传到服务器
            FTPUtil.uploadFile( Lists.newArrayList(targetFile) );
            // 删除本地文件
            targetFile.delete();
        }catch (Exception e){
            logger.error("文件上传失败",e);
            return null;
        }
        return targetFile.getName();
    }

}
