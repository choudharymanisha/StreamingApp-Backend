package com.stream.app.spring_stream_backend.controller;

import com.stream.app.spring_stream_backend.entities.Video;
import com.stream.app.spring_stream_backend.playload.CustomMessage;
import com.stream.app.spring_stream_backend.services.VideoService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/videos")
@CrossOrigin("*")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestParam("file") MultipartFile file,
                                    @RequestParam("title") String title,
                                    @RequestParam("description") String description) {

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setVideoId(UUID.randomUUID().toString());
        Video savedVideo = videoService.save(video, file);

        if (savedVideo != null) {
            return ResponseEntity.status(HttpStatus.OK).body(video);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CustomMessage.builder().message("Video not uploaded").success(false).build());
        }
    }

    @GetMapping
    public List<Video> getAll() {
        return videoService.getAll();
    }

    @GetMapping("/stream/{videoId}")
    public ResponseEntity<Resource> stream(@PathVariable String videoId) {
        Video video = videoService.get(videoId);
        String contentType = video.getContentType();
        String filePath = video.getFilePath();

        Resource resource = new FileSystemResource(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
    @GetMapping("/stream/range/{videoId}")
    public ResponseEntity<Resource> streamVideoRange(
            @PathVariable String videoId,
            @RequestHeader(value =  "Range", required = false) String range
    ){
        System.out.println(range);
        Video video = videoService.get(videoId);
        Path path  =  Paths.get(video.getFilePath());
        Resource resource = new FileSystemResource(path);
        String contentType = video.getContentType();
        if(contentType==null){
            contentType = "application/octet-stream";


        }
        long fileLength = path.toFile().length();
        if(range == null){
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);

        }

    }


}
