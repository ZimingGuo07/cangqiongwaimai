package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Dish;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    Integer judgeSetmeal(Long id);

    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    void insertBatch(List<SetmealDish> setmealDishes);

    void deleteBatch(List<Long> setmealIds);
    @Select("select * from sky_take_out.setmeal_dish where setmeal_id=#{id}")
    List<SetmealDish> selectById(Long id);

    List<Dish> selectDishesBySetmealId(Long id);
}
