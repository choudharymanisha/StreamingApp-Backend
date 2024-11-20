package com.stream.app.spring_stream_backend.services.impl;

import com.stream.app.spring_stream_backend.entities.Video;
import com.stream.app.spring_stream_backend.repositories.VideoRepository;
import com.stream.app.spring_stream_backend.services.VideoService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

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
    public void init() {
        File file = new File(DIR);
        try {
            Files.createDirectories(Paths.get(HSL_DIR));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!file.exists()) {
            file.mkdir();
            System.out.println("Folder Created:");
        } else {
            System.out.println("Folder already created");
        }
    }

    @Override
    public Video save(Video video, MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            String contentType = file.getContentType();
            InputStream inputStream = file.getInputStream();

            String cleanFileName = StringUtils.cleanPath(filename);
            String cleanFolder = StringUtils.cleanPath(DIR);
            Path path = Paths.get(cleanFolder, cleanFileName);

            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);

            video.setContentType(contentType);
            video.setFilePath(path.toString());
            Video savedVideo = videoRepository.save(video);

            processVideo(savedVideo.getVideoId(), null);

            return savedVideo;

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error in processing video");
        }
    }

    @Override
    public Video get(String videoId) {
        return videoRepository.findById(videoId).orElseThrow(() -> new RuntimeException("Video not found"));
    }

    @Override
    public Optional<Video> getByTitle(String title) {
        return videoRepository.findByTitle(title);
    }

    @Override
    public List<Video> getAll() {
        return videoRepository.findAll();
    }

    @Override
    public String processVideo(String videoId, Object o) {
        Video video = this.get(videoId);
        String filePath = video.getFilePath();
        Path videoPath = Paths.get(filePath);

        try {
            Path outputPath = Paths.get(HSL_DIR, videoId);
            Files.createDirectories(outputPath);

            String ffmpegCmd = String.format(
                    "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%3d.ts\" \"%s/master.m3u8\" ",
                    videoPath, outputPath, outputPath
            );

            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", ffmpegCmd);
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            int exit = process.waitFor();

            if (exit != 0) {
                throw new RuntimeException("Video processing failed!");
            }

            return videoId;

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Video processing failed!", e);
        }
    }
}
