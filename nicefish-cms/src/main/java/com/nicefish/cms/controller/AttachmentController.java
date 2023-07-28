package com.nicefish.cms.controller;

import com.nicefish.cms.jpa.entity.AttachmentEntity;
import com.nicefish.cms.service.IAttachmentService;
import com.nicefish.core.utils.AjaxResult;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Api("cms-附件上传")
@RestController
@RequestMapping("/nicefish/attachment")
public class AttachmentController {

    private static final String FILE_DOWNLOAD_HEADER_NAME = "Content-Disposition";
    private static final String FILE_DOWNLOAD_HEADER_VALUE = "attachment;filename=";
    @Autowired
    private IAttachmentService attachmentService;

    /**
     * 文件上传
     * @param multipartFile
     * @return
     */
    @PostMapping(value = "/upload")
    public AjaxResult upLoad(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            return AjaxResult.failure("请上传附件");
        }
        //上传文件
        return AjaxResult.success(attachmentService.upload(multipartFile));
    }

    @GetMapping(value = "/download/{id}")
    public AjaxResult download(@PathVariable("id") Integer id, HttpServletResponse resp) throws UnsupportedEncodingException {
        AttachmentEntity attachmentEntity = attachmentService.getAttachmentById(id);
        File storedFile = new File(attachmentEntity.getPath());
        if (storedFile.exists()) {
            String fileNameEncode = URLEncoder.encode(storedFile.getName(), StandardCharsets.UTF_8.name());
            // 设置下载后文件名
            resp.addHeader(FILE_DOWNLOAD_HEADER_NAME, FILE_DOWNLOAD_HEADER_VALUE + fileNameEncode);
            byte[] buffer = new byte[1024];
            FileInputStream fileInputStream = null;
            BufferedInputStream bufferedInputStream = null;
            try {
                fileInputStream = new FileInputStream(storedFile);
                bufferedInputStream = new BufferedInputStream(fileInputStream);
                OutputStream os = resp.getOutputStream();
                int i = bufferedInputStream.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bufferedInputStream.read(buffer);
                }
                return AjaxResult.success();
            } catch (Exception e) {
                return AjaxResult.failure();
            } finally {
                if (bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (IOException e) {
                        return AjaxResult.failure();
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        return AjaxResult.failure();
                    }
                }
            }
        } else {
            return AjaxResult.failure("文件不存在");
        }
    }

}
