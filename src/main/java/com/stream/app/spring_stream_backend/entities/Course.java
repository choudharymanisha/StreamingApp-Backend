package com.stream.app.spring_stream_backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "yt_courses")
public class Course {
    @Id
    private String id;
    private String title;
   // @OneToOne(mappedBy = "Course")
    //private List<Video> list = new ArrayList<>();


}
