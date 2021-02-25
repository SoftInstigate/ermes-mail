package com.softinstigate.ermes.mail;

public class Sender {

    public static void main(String[] args) {
        String from = args[0];
        String to = args[1];
        String message = args[2];

        System.out.format("from='%s',  to='%s', message='%s'\n", from, to, message);
    }
}
