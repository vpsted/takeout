package com.fang.takeout.dto;

import com.fang.takeout.entity.Setmeal;
import com.fang.takeout.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
