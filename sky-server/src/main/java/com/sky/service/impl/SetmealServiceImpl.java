package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Override
    @Transactional
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.insert(setmeal);
        //感觉接口文档有问题，里面写的套餐ID是Integer，数据库和函数写的都是Long
        Long setmealId=setmeal.getId();

        List<SetmealDish>  setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish-> setmealDish.setSetmealId(setmealId));
        setmealDishMapper.insertBatch(setmealDishes);

    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page=setmealMapper.pageQuery(setmealPageQueryDTO);
        PageResult pageResult=new PageResult(page.getTotal(),page.getResult());
        return pageResult;
    }

    @Override
    public void deleteBatchSetmeal(List<Long> setmealIds) {
        List<Integer> setmealStatus=setmealMapper.selectStatusByIds(setmealIds);
        setmealStatus.forEach(setmealStatu -> {if(setmealStatu==StatusConstant.ENABLE) throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);});

        setmealMapper.deleteBatch(setmealIds);
        setmealDishMapper.deleteBatch(setmealIds);



    }

    @Override
    public SetmealVO selectSetmealById(Long id) {
        //说是查询的时候前端发送了一条查询分类名称语句，疑似是com.sky.controller.admin.CategoryController.list
        Setmeal setmeal=setmealMapper.selectById(id);
        List<SetmealDish> setmealDishes=setmealDishMapper.selectById(id);

        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);

        return setmealVO;
    }

    @Override
    @Transactional
    public void updateSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);
        Long setmealId=setmeal.getId();
        List<Long> setmealIdList=new ArrayList<>();
        setmealIdList.add(setmealId);
        setmealDishMapper.deleteBatch(setmealIdList);
        List<SetmealDish> setmealDishes=setmealDTO.getSetmealDishes();
        for(SetmealDish setmealDish:setmealDishes){
            setmealDish.setSetmealId(setmealId);
        }
        setmealDishMapper.insertBatch(setmealDishes);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        if(status == StatusConstant.ENABLE){
            List<Dish> dishList=setmealDishMapper.selectDishesBySetmealId(id);
            for(Dish dish:dishList){
                if (dish.getStatus()==StatusConstant.DISABLE) {
                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            }
        }

        Setmeal setmeal=Setmeal.builder().status(status).id(id).build();
        setmealMapper.update(setmeal);


    }
}
