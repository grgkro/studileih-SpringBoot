package com.example.studileih.Dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

@Data
@NoArgsConstructor
public class ProductDto {
    private static final SimpleDateFormat dateFormat
            = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private Long id;
    private Long userId;
    private String name;
    private String title;
    private double price = 0;
    private int views = 0; //How often was the product viewed?
    private boolean available = true; //is it available?
    private String createdAt;
    private String updatedAt;
    private ArrayList<String> picPaths;

    public Date getCreatedAtConverted(String timezone) throws ParseException {
        dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
        return dateFormat.parse(this.createdAt);
    }

    public void setCreatedAt(Date createdAt, String timezone) {
        dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
        this.createdAt = dateFormat.format(createdAt);
    }

    public Date getUpdatedAtConverted(String timezone) throws ParseException {
        dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
        return dateFormat.parse(this.updatedAt);
    }

    public void setUpdatedAt(Date updatedAt, String timezone) {
        dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
        this.updatedAt = dateFormat.format(updatedAt);
    }
}
