package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品和对应口味
     * @param dishDTO
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        //Dish dish = new Dish(null,dishDTO.getName(),dishDTO.getCategoryId(),dishDTO.getPrice(),dishDTO.getImage(),dishDTO.getDescription(),dishDTO.getStatus(),null,null,null,null);
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);
        Long id=dish.getId();//注意这里，插入数据后获得对应的id

        List<DishFlavor> dishFlavorsList=dishDTO.getFlavors();
        if(dishFlavorsList!=null&&dishFlavorsList.size()>0){
            /*
            for(DishFlavor dishFlavor:dishFlavorsList){
                dishFlavor.setDishId(id);
                dishFlavorMapper.save(dishFlavor);
            }
             */
            dishFlavorsList.forEach(dishFlavor -> dishFlavor.setDishId(id));
            dishFlavorMapper.insertBatch(dishFlavorsList);
        }


    }
}
