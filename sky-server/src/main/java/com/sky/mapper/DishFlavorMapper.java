package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface DishFlavorMapper {
    /*
    @Insert("INSERT into sky_take_out.dish_flavor(dish_id, name, value) VALUE (#{dishId},#{name},#{value})")
    void save(DishFlavor dishFlavors);
     */

    void insertBatch(List<DishFlavor> dishFlavorsList);


    void deleteByDishId(Long id);

    void deleteBatchByDishIds(List<Long> dishIds);

    List<DishFlavor> selectById(Long id);
}
