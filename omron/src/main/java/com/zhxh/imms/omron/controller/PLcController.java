package com.zhxh.imms.omron.controller;

import com.zhxh.imms.omron.domain.PLcInitor;
import com.zhxh.imms.omron.domain.Plc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/plc")
public class PLcController {
    @Autowired
    private PLcInitor pLcInitor;

    @RequestMapping("getPlcList")
    public Plc[] getPlcList(){
        return pLcInitor.getPlcList();
    }

    @PostMapping("start")
    public int start(){
        return 0;
    }

    @PostMapping("stop")
    public int stop(){
        return 0;
    }
}
