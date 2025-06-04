package com.bruhdows.minitext.processor;

@FunctionalInterface
public interface ComponentProcessor {
    String process(String tag, String content, Object context);
}
