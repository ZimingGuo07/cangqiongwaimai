package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper  setmealDishMapper;

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

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> p=dishMapper.pageQuery(dishPageQueryDTO);
        PageResult pageResult=new PageResult(p.getTotal(),p.getResult());
        return pageResult;
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        /*
        for(Long id:ids){
            Integer judgeStatus= dishMapper.judgeStatus(id);
            Integer judgeSetmeal=setmealDishMapper.judgeSetmeal(id);
            if(judgeStatus ==0 && judgeSetmeal ==0){
                dishMapper.deleteById(id);
                dishFlavorMapper.deleteByDishId  (id);
            }else {
                log.info("当前id对应菜品不能删除：{}",id);
                throw new DeletionNotAllowedException("不能删除");
            }
        }

        上面是我写的代码，这样写是因为理解错业务逻辑了，要求的应该是如果批量删除里有一个不能删，那么则会那所有的就都不能删，我这个是循环判断哪个能删就删除那个
        但是因为我不懂抛出异常会终止循环，所以在我的代码中，如果出现了一个不符合删除规则的，那么就会抛出异常，循环终止，并且Spring事务会恢复已经删除的数据
         */
        for(Long id:ids){
            //判断是否起售
            Dish dish=dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }

            //判断是否关联套餐
            List<Long> setmealIds =setmealDishMapper.getSetmealIdsByDishIds(ids);
            if(setmealIds!=null&&setmealIds.size()>0){
                throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
            }
            dishMapper.deleteBatchByIds(ids);
            dishFlavorMapper.deleteBatchByDishIds(ids);
        }


    }

}
