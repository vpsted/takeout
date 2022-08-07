package com.fang.takeout.controller;

import com.fang.takeout.common.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;


@RestController
@RequestMapping("/common")
public class CommonController {
  //通过配置文件赋值
  @Value("${takeout.path}")
  private String PATH;

  /**
   * 文件上传
   *
   * @param file 函数参数名称必须要和请求数据里的name的名称相同
   * @return
   */

  @PostMapping("/upload")
  public R<String> upload(MultipartFile file) {
    String originalFilename = file.getOriginalFilename();//原始文件名
    String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));//获取文件后缀
    //使用UUID重新生成文件名，避免因为文件重复而被覆盖
    String fileName = UUID.randomUUID().toString() + suffix;

    File dir = new File(PATH);
    if (!dir.exists()) {
      //目录不存在
      dir.mkdirs();
    }
    try {
      file.transferTo(new File(PATH + fileName));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return R.success(fileName);
  }

  @GetMapping("/download")
  public void download(HttpServletResponse response, String name) {
    try {
      FileInputStream fileInputStream = new FileInputStream(new File(PATH + name));
      ServletOutputStream outputStream = response.getOutputStream();
      response.setContentType("image/jpeg");
      int len = 0;
      byte[] bytes = new byte[1024];
      while ((len = fileInputStream.read(bytes)) != -1) {
        outputStream.write(bytes, 0, len);
        outputStream.flush();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
