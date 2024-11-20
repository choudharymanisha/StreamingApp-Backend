package com.stream.app.spring_stream_backend.controller;

import com.stream.app.spring_stream_backend.entities.Video;
import com.stream.app.spring_stream_backend.playload.CustomMessage;
import com.stream.app.spring_stream_backend.services.VideoService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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
            @RequestHeader(value = "Range", required = false) String range
    ) {
        System.out.println(range);
        Video video = videoService.get(videoId);
        Path path = Paths.get(video.getFilePath());
        Resource resource = new FileSystemResource(path);
        String contentType = video.getContentType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        long fileLength = path.toFile().length();
        if (range == null || !range.startsWith("bytes=")) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        }

        long rangeStart = 0;
        long rangeEnd = fileLength - 1;

        try {
            String[] ranges = range.substring("bytes=".length()).split("-");
            rangeStart = Long.parseLong(ranges[0].trim());
            if (ranges.length > 1 && !ranges[1].isEmpty()) {
                rangeEnd = Long.parseLong(ranges[1].trim());
            }
            if (rangeEnd > fileLength - 1) {
                rangeEnd = fileLength - 1;
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid range format: " + range);
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build();
        }

        try (InputStream inputStream = Files.newInputStream(path)) {
            inputStream.skip(rangeStart);
            long contentLength = rangeEnd - rangeStart + 1;
            byte[] data = new byte[(int) contentLength];
            int read = inputStream.read(data, 0, data.length);
            System.out.println("read(number of bytes): " + read);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            headers.add("X-Content-Type-Options", "nosniff");
            headers.setContentLength(contentLength);

            return ResponseEntity
                    .status(HttpStatus.PARTIAL_CONTENT)
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new ByteArrayResource(data));
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}