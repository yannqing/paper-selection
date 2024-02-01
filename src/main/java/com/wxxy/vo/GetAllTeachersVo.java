package com.wxxy.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAllTeachersVo {
    private List<StudentGetTeachersVo> teachers;
    private long total;
}
