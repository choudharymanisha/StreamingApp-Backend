package com.stream.app.spring_stream_backend.services;

import com.stream.app.spring_stream_backend.entities.Video;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface VideoService {

    Video save (Video video, MultipartFile file);
    Video get(String videoId);
    Optional<Video> getByTitle(String title);
    List<Video> getAll();
     String processVideo(String videoId, Object o);


}
