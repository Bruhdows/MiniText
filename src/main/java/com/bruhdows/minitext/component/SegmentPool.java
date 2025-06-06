package com.bruhdows.minitext.component;

import java.util.ArrayDeque;
import java.util.Queue;

public class SegmentPool {
    private static final ThreadLocal<Queue<TextSegment>> POOL = 
        ThreadLocal.withInitial(ArrayDeque::new);
    private static final int MAX_POOL_SIZE = 10;
    
    public static TextSegment acquire() {
        Queue<TextSegment> pool = POOL.get();
        TextSegment segment = pool.poll();
        return segment != null ? segment.reset() : new TextSegment();
    }
    
    public static void release(TextSegment segment) {
        if (segment != null && POOL.get().size() < MAX_POOL_SIZE) {
            POOL.get().offer(segment);
        }
    }
}
