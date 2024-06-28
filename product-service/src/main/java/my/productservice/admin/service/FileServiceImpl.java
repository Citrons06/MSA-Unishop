package my.productservice.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Override
    public String uploadFile(String uploadPath, String originalFileName, byte[] fileData) throws IOException {
        UUID uuid = UUID.randomUUID();

        String ext = originalFileName.substring(originalFileName.lastIndexOf("."));
        String savedFileName = uuid.toString() + ext;
        String fileUploadPath = uploadPath + "/" + savedFileName;

        FileOutputStream fileOutputStream = new FileOutputStream(fileUploadPath);
        fileOutputStream.write(fileData);
        fileOutputStream.close();

        return savedFileName;
    }
}
