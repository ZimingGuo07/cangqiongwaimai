package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    Integer judgeSetmeal(Long id);

    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);
}
