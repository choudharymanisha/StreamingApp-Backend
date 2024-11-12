package com.stream.app.spring_stream_backend.services.impl;

import com.stream.app.spring_stream_backend.entities.Video;
import com.stream.app.spring_stream_backend.repositories.VideoRepository;
import com.stream.app.spring_stream_backend.services.VideoService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class VideoServiceImpl implements VideoService {

@Value("${files.video}")
String DIR;
@Value("${file.video.hsl}")
String HSL_DIR;

    private VideoRepository videoRepository;
    public VideoServiceImpl(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }


@PostConstruct
public void init(){

    File file = new File(DIR);
   try{
       Files.createDirectories(Paths.get(HSL_DIR));

   }
   catch(IOException e){
       throw new RuntimeException(e);

    }
    if(!file.exists()){
        file.mkdir();
        System.out.println("folder created");
    } else {
        System.out.println("folder order created");
    }

}
    @Override
    public Video save(Video video, MultipartFile file) {

       try {
           String filename = file.getOriginalFilename();
           String contentType = file.getContentType();
           InputStream inputStream = file.getInputStream();



           String cleanFileName  = StringUtils.cleanPath(filename);
           String cleanFolder = StringUtils.cleanPath(DIR);


           Path path  = Paths.get(cleanFolder,cleanFileName);
           Files.copy(inputStream,path, StandardCopyOption.REPLACE_EXISTING);
           video.setContentType(contentType);
           video.setFilePath(path.toString());
            return videoRepository.save(video);

       } catch(IOException e){
           e.printStackTrace();
           return null;

        }


    }

    @Override
    public Video get(String videoId) {
       Video video =   videoRepository.findById(videoId).orElseThrow(()-> new RuntimeException("video not found"));


        return  video  ;
    }

    @Override
    public Video getByTitle(String title) {
        return null;
    }

    @Override
    public List<Video> getAll() {

        return videoRepository.findAll();
    }
    @Override
    public String processVideo (String videoId ,MultipartFile file){
        Video video = this.get(videoId);
        String filepath = video.getFilePath();
        Path videoPath = Paths.get(filepath);
          String output360p = HSL_DIR+"/360p/";
        String output720p = HSL_DIR+"/720p/";
        String output1080p = HSL_DIR+"/1080p/";
        try{
            Files.createDirectories(Paths.get(output360p));
            Files.createDirectories(Paths.get(output720p));
            Files.createDirectories(Paths.get(output1080p));
        }catch(IOException ex){
            throw new RuntimeException("video processing fail");

        }
        return null;





    }
}
