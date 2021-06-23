package com.zhxh.imms.utils;

public class Logger {
    public static void info(String message){
        System.out.println(message);
    }

    public static void debug(Exception error){
        error.printStackTrace();
    }

    public static void debug(String message){
        System.out.println(message);
    }

    public static void error(String message){
        System.out.println(message);
    }

    public static void error(Exception e){
        e.printStackTrace();
    }
}
