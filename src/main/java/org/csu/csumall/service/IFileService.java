package org.csu.csumall.service;

import org.springframework.web.multipart.MultipartFile;


public interface IFileService {

    String upLoadFile(MultipartFile file, String path);

}
