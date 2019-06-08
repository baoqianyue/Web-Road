package com.barackbao.service;

import com.barackbao.beans.Bean;

/**
 * Created by barackbao on 2019-06-09
 */
@Bean
public class SalaryService {

    public Integer calSalary(Integer experience) {
        return experience * 5000;
    }
}
